/**
 * Copyright (C) 2013 Phoenix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.falcon.internal.action;

import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.reflect.Reflection;
import io.falcon.Falcon;
import io.falcon.SkyException;
import io.falcon.RequestCycle;
import io.falcon.annotations.*;
import io.falcon.interceptor.PostInterceptor;
import io.falcon.interceptor.PreInterceptor;
import io.falcon.internal.ControllerInfo;
import io.falcon.internal.ControllerInfo2;
import io.falcon.route.RouteBag;
import io.falcon.utils.*;
import io.falcon.utils.converter.ConverterFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 使用注解的Controller类的操作
 * User: xux
 * Date: 13-10-25
 * Time: 下午4:34
 * To change this template use File | Settings | File Templates.
 */
public class ActionInfo2 implements ActionInfo {

    public Class<?> controller;

    protected ControllerInfo controllerInfo;

    protected final Method method;
    protected final Falcon fly;

    protected static final Object[] EMPTY_ARGS = new Object[0];
    /**
     * path匹配模式，联合了Controller上path，并去除后置"/",
     */
    protected final String pathPattern;

    /**
     * http method POST/GET/DELETE/PUT
     */
    protected final String httpMethod;

    /**
     * 方法上所有参数名，按顺序排列
     */
    protected final List<String> paramNames;

    /**
     * 方法上所有参数类型，按顺序排列
     */
    protected final List<Class<?>> paramTypes;

    /**
     * 所有annotation，包括并覆盖controller上的annotation，
     */
    protected final Set<Annotation> annotations;

    /**
     * 所有前置拦截器,按拦截器的order升序排列
     */
    protected final List<PreInterceptor> preInterceptors;

    /**
     * 所有后置拦截器，按拦截器的order降序排列
     */
    protected final List<PostInterceptor> postInterceptors;

    /**
     * 匹配的优先级
     */
    protected final int order;

    /**
     * 是否是模版匹配
     */
    protected final boolean isPattern;

    /**
     * 利用Ant匹配模型处理url
     */
    protected final PathMatcher pathMatcher = new AntPathMatcher();
    protected final ConverterFactory converter = new ConverterFactory();

    protected MethodInvoke methodInvoke;

    public ActionInfo2(ControllerInfo controllerInfo,Method method, Falcon fly){

        this.controllerInfo = controllerInfo;
        this.method = method;
        this.fly = fly;

        Path path = AnnotationUtils.findAnnotation(method, Path.class);
        this.order = path.order();

        this.pathPattern = simplyPathPattern(controllerInfo, path);

        this.paramTypes = ImmutableList.copyOf(method.getParameterTypes());
        this.paramNames = ImmutableList.copyOf(ClassUtils.getMethodParamNames(controllerInfo.getClazz(), method));

        // 计算匹配的优先级,精确匹配还是模版匹配
        isPattern = pathMatcher.isPattern(pathPattern)
                || paramTypes.size() > 0;

        httpMethod = pickupHttpMethod(controllerInfo, method);


        annotations = collectAnnotations(controllerInfo, method);

        // 拦截器
        List<InterceptorInfo> interceptorInfoList = findInterceptors();
        preInterceptors = getPreInterceptorList(interceptorInfoList);
        postInterceptors = getPostInterceptorList(interceptorInfoList);


        decideMethodInvoke(path);

    }

    private void decideMethodInvoke(Path path){
        Pattern urlPattern = Pattern.compile("\\{\\w+\\}$");
        Matcher urlMatcher = urlPattern.matcher(path.value());
        //如果URL中含有 如{name}的参数，则生成 UrlParamsMethodInvoke
        if(urlMatcher.find()){
            methodInvoke = new UrlParamsMethodInvoke(this);
        }else{
            WebMethod webMethod = AnnotationUtils.findAnnotation(method,WebMethod.class);
            //如果当前方法有  JSONParams 注解，表示需要json化参数，若没有，则设置URL参数
            JSONParams json = AnnotationUtils.findAnnotation(method,JSONParams.class);
            //如果参数只有一个并且类型是RequestCycle 则执行 ，否则 ...
            if(paramTypes.size() >= 1 && RequestCycle.class == paramTypes.get(0)){
                methodInvoke = new RequestCycleOnceParamsMethodInvoke(this);
            }else if(webMethod != null) {
                methodInvoke = new WebMethodParamsMethodInvoke(this);
            }else if(json != null){
                methodInvoke = new JSONParamsMethodInvoke(this);
            }else{
                //默认
                methodInvoke = new URLNonParamsMethodInvoke(this);
                //
//                methodInvoke = new JSONFormParams1MethodInvoke(this);
            }
        }

    }
    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public String getPathPattern() {
        return pathPattern;
    }

    @Override
    public boolean isPattern() {
        return isPattern;
    }

    @Override
    public boolean matchHttpMethod(RouteBag bag) {
        if(httpMethod == null){
            return true;
        }
        return StringUtils.equals(httpMethod.toString(),bag.getMethod());
    }

    @Override
    public boolean match(RouteBag bag, Map<String, String> uriTemplateVariables) {
        return getPathMatcher().doMatch(getPathPattern(), bag.getSimplyPath(), true, uriTemplateVariables);
    }

    @Override
    public List<PreInterceptor> getPreInterceptors() {
        return preInterceptors;
    }

    @Override
    public List<PostInterceptor> getPostInterceptors() {
        return postInterceptors;
    }

    @Override
    public Method method() {
        return method;
    }

    @Override
    public Object invoke(Map<String, String> urlParams) throws Throwable {
        return methodInvoke.invoke(urlParams);
    }

    ConverterFactory getConverter() {
        return converter;
    }
    @Override
    public   Object controller(){
        //由Guice创建对象
        return fly().injector().getInstance(controllerInfo.getClazz());
    }

    @Override
    public List<Class<?>> getParamTypes() {
        return paramTypes;
    }
    @Override
    public List<String> getParamNames() {
        return paramNames;
    }
    public Set<Annotation> annotations() {
        return annotations;
    }



    @Override
    public Falcon fly() {
        return fly;
    }

    List<PreInterceptor> getPreInterceptorList( List<InterceptorInfo> interceptorInfoList) {

        ImmutableList.Builder<PreInterceptor> builder = ImmutableList.builder();

        for(InterceptorInfo interceptorInfo : interceptorInfoList) {
            PreInterceptor preInterceptor = interceptorInfo.getPreInterceptor();
            if (preInterceptor != null)
                builder.add(preInterceptor);
        }

        return builder.build();
    }

    List<PostInterceptor> getPostInterceptorList( List<InterceptorInfo> interceptorInfoList) {

        ImmutableList.Builder<PostInterceptor> builder = ImmutableList.builder();

        for(InterceptorInfo interceptorInfo : interceptorInfoList) {
            PostInterceptor postInterceptor = interceptorInfo.getPostInterceptor();
            if (postInterceptor != null)
                builder.add(postInterceptor);
        }

        //反转，对于post先执行排序高的，再执行排序低的
        return builder.build().reverse();
    }

    private List<InterceptorInfo> merge(List<InterceptorInfo> interceptorInfoList, InterceptorInfo interceptorInfo) {

        int position = interceptorInfoList.size();

        for (int index = 0; index < interceptorInfoList.size(); index++) {
            InterceptorInfo item = interceptorInfoList.get(index);
            // 如果annotation已存在，则忽略（先处理方法的Annotation，再处理类的Annotation）
            if (item.sample(interceptorInfo))
                return interceptorInfoList;

            if(item.getOrder() > interceptorInfo.getOrder()) {
                position = index;
            }
        }

        interceptorInfoList.add(position, interceptorInfo);
        return interceptorInfoList;
    }

    List<InterceptorInfo> findInterceptors() {

        List<InterceptorInfo> interceptorInfoList = Lists.newArrayList();

        for(Annotation ann : this.annotations()) {

            InterceptorInfo interceptorInfo = findInterceptorInfo(ann);
            if (interceptorInfo == null)
                continue;

            interceptorInfoList = merge(interceptorInfoList, interceptorInfo);
        }

        return interceptorInfoList;
    }
    private InterceptorInfo findInterceptorInfo(Annotation ann) {
        PreInterceptorAnnotation preA = AnnotationUtils.findAnnotation(ann.getClass(), PreInterceptorAnnotation.class);
        PostInterceptorAnnotation postA = AnnotationUtils.findAnnotation(ann.getClass(), PostInterceptorAnnotation.class);
        if (preA == null && postA == null)
            return null;

        Object orderObject = AnnotationUtils.getValue(ann, "order");

        int order = orderObject == null ? 100
                : (Integer)orderObject;   // xxx: maybe throw exception.

        PreInterceptor preInterceptor = (preA == null ? null : fly().instance(preA.value()));
        PostInterceptor postInterceptor = (postA == null ? null : fly().instance(postA.value()));

        return new InterceptorInfo(ann, order, preInterceptor, postInterceptor);
    }

    String  pickupHttpMethod(ControllerInfo controllerInfo,Method method) {
        //如果controller类有设置 httpMethod，则controller所有method都设置统一
        if(Strings.isNullOrEmpty(controllerInfo.getHttpMethod())){
            List<Class<? extends Annotation>> list = Lists.newArrayList(GET.class,POST.class,PUT.class,DELETE.class);
            try{
                for(Class<? extends Annotation> ann : list){
                    Annotation $0 = AnnotationUtils.findAnnotation(method, ann);
                    if($0 != null){
                        return  AnnotationUtils.getValue($0).toString();
                    }
                }
            }finally {
                list = null;
            }
            return null;
        }else{
            return controllerInfo.getHttpMethod();
        }

    }

    String simplyPathPattern(ControllerInfo controllerInfo, Path path) {
        String originPathPattern = combinePathPattern(controllerInfo, path);
        return simplyPathPattern(originPathPattern);
    }
    private String simplyPathPattern(String combinedPattern) {
        if (combinedPattern.length() > 1 && combinedPattern.endsWith("/"))
            combinedPattern = combinedPattern.substring(0, combinedPattern.length() - 2);
        return combinedPattern;
    }
    /**
     *收集方法上所有Annotation，包括Controller上标志
     * @param controllerInfo controller信息
     * @param method 方法
     * @return 方法上所有Annotation，包括Controller
     */
    ImmutableSet<Annotation> collectAnnotations(ControllerInfo controllerInfo, Method method) {
        return ImmutableSet.<Annotation>builder()
                .add(method.getAnnotations())
                .addAll(controllerInfo.getAnnotations())
                .build();

    }

    private String combinePathPattern(ControllerInfo controllerInfo, Path path) {
        String pathPattern = path.value();

        String controllerPattern = controllerInfo.getPathUrl();
        return getPathMatcher().combine(controllerPattern, pathPattern);
    }

    PathMatcher getPathMatcher() {
        return pathMatcher;
    }







    private class InterceptorInfo {
        private final Annotation annotation;
        private final PreInterceptor preInterceptor;
        private final PostInterceptor postInterceptor;
        private final int order;

        private InterceptorInfo(Annotation annotation, int order, PreInterceptor preInterceptor, PostInterceptor postInterceptor) {
            this.annotation = annotation;
            this.order = order;
            this.preInterceptor = preInterceptor;
            this.postInterceptor = postInterceptor;
        }

        private Annotation getAnnotation() {
            return annotation;
        }

        public PreInterceptor getPreInterceptor() {
            return preInterceptor;
        }

        public PostInterceptor getPostInterceptor() {
            return postInterceptor;
        }

        public int getOrder() {
            return order;
        }

        public boolean sample(InterceptorInfo other) {
            return this.getAnnotation().annotationType() == other.getAnnotation().annotationType();
        }
    }
}
