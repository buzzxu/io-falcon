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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import io.falcon.Controller;
import io.falcon.Falcon;
import io.falcon.SkyException;
import io.falcon.annotations.*;
import io.falcon.interceptor.PostInterceptor;
import io.falcon.interceptor.PreInterceptor;
import io.falcon.internal.ControllerInfo;
import io.falcon.route.RouteBag;
import io.falcon.utils.*;
import io.falcon.utils.converter.ConverterFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-17
 * Time: 下午5:05
 * To change this template use File | Settings | File Templates.
 */
public class ActionInfo1 implements ActionInfo{
    private final ControllerInfo controllerInfo;
    private final Method method;
    private final Falcon falcon;
    /**
     * path匹配模式，联合了Controller上path，并去除后置"/",
     */
    private final String pathPattern;

    /**
     * Http Method GET
     */
    private final boolean isGet;

    /**
     * http method POST
     */
    private final boolean isPost;

    /**
     * 方法上所有参数名，按顺序排列
     */
    private final List<String> paramNames;

    /**
     * 方法上所有参数类型，按顺序排列
     */
    private final List<Class<?>> paramTypes;

    /**
     * 所有annotation，包括并覆盖controller上的annotation，
     */
    private final Set<Annotation> annotations;

    /**
     * 所有前置拦截器,按拦截器的order升序排列
     */
    private final List<PreInterceptor> preInterceptors;

    /**
     * 所有后置拦截器，按拦截器的order降序排列
     */
    private final List<PostInterceptor> postInterceptors;

    /**
     * 匹配的优先级
     */
    private final int order;

    /**
     * 是否是模版匹配
     */
    private final boolean isPattern;

    /**
     * 利用Ant匹配模型处理url
     */
    private final PathMatcher pathMatcher = new AntPathMatcher();

    private final ConverterFactory converter = new ConverterFactory();

    public ActionInfo1(ControllerInfo controllerInfo, Method method, Falcon falcon) {
        this.controllerInfo = controllerInfo;
        this.method = method;
        this.falcon = falcon;

        Path path = AnnotationUtils.findAnnotation(method, Path.class);
        this.order = path.order();

        this.pathPattern = simplyPathPattern(controllerInfo, path);

        this.paramTypes = ImmutableList.copyOf(method.getParameterTypes());
        this.paramNames = ImmutableList.copyOf(ClassUtils.getMethodParamNames(controllerInfo.getClazz(), method));

        // 计算匹配的优先级,精确匹配还是模版匹配
        isPattern = pathMatcher.isPattern(pathPattern)
                || paramTypes.size() > 0;

        Pair<Boolean, Boolean> httpMethodPair = pickupHttpMethod(controllerInfo, method);
        this.isGet = httpMethodPair.getKey();
        this.isPost = httpMethodPair.getValue();

        annotations = collectAnnotations(controllerInfo, method);

        // 拦截器
        List<InterceptorInfo> interceptorInfoList = findInterceptors();
        preInterceptors = getPreInterceptorList(interceptorInfoList);
        postInterceptors = getPostInterceptorList(interceptorInfoList);

    }

    public String getPathPattern() {
        return pathPattern;
    }


    public Controller controller() {
        return controllerInfo.getController();
    }

    public Method method() {
        return method;
    }

    public boolean isGet() {
        return isGet;
    }

    public boolean isPost() {
        return isPost;
    }

    public List<Class<?>> getParamTypes() {
        return paramTypes;
    }

    public List<String> getParamNames() {
        return paramNames;
    }

    public Set<Annotation> annotations() {
        return annotations;
    }

    public boolean isPattern() {
        return isPattern;
    }

    public int getOrder() {
        return order;
    }

    public List<PreInterceptor> getPreInterceptors() {
        return preInterceptors;
    }

    public List<PostInterceptor> getPostInterceptors() {
        return postInterceptors;
    }

    PathMatcher getPathMatcher() {
        return pathMatcher;
    }
    @Override
    public Falcon fly() {
        return falcon;
    }

    public ConverterFactory getConverter() {
        return converter;
    }

    public boolean match(RouteBag bag, Map<String, String> uriTemplateVariables) {
        return getPathMatcher().doMatch(getPathPattern(), bag.getSimplyPath(), true, uriTemplateVariables);
    }

    public boolean matchHttpMethod(RouteBag bag) {

        return (bag.isGet() && isGet())
                || (bag.isPost() && isPost());
    }

    public Object invoke(Map<String, String> urlParams)throws Throwable {
        Object[] param = new Object[getParamTypes().size()];
        for(int index = 0; index < getParamNames().size(); index++){
            String paramName = getParamNames().get(index);
            Class<?> clazz = getParamTypes().get(index);

            String v = urlParams.get(paramName);

            if (v == null)
                throw SkyException.newBuilder("Invoke exception:")
                        .addContextVariable(paramName, "null")
                        .build();

            // fixMe: move to init
            if(!getConverter().canConvert(clazz))
                throw SkyException.newBuilder("Invoke cannot convert parameter.")
                        .addContextVariable(paramName, "expect " + clazz.getName() + " but value is " + v)
                        .build();

            param[index] = getConverter().convert(clazz, v);
        }

        try {
            return  method().invoke(controller(), param);

        } catch (Exception e) {
            throw SkyException.newBuilder("invoke exception.", e)
                    .addContextVariables(urlParams)
                    .build();
        }
    }


    String simplyPathPattern(ControllerInfo controllerInfo, Path path) {
        String originPathPattern = combinePathPattern(controllerInfo, path);
        return simplyPathPattern(originPathPattern);
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

    Pair<Boolean, Boolean> pickupHttpMethod(ControllerInfo controllerInfo, Method method) {
        boolean isGet = AnnotationUtils.findAnnotation(method, GET.class) != null;
        boolean isPost = AnnotationUtils.findAnnotation(method, POST.class) != null;

        if (!isGet && !isPost) {
            isGet = controllerInfo.isGet();
            isPost = controllerInfo.isPost();
        }

        return Pair.build(isGet, isPost);

    }

    private String simplyPathPattern(String combinedPattern) {
        if (combinedPattern.length() > 1 && combinedPattern.endsWith("/"))
            combinedPattern = combinedPattern.substring(0, combinedPattern.length() - 2);
        return combinedPattern;
    }

    private String combinePathPattern(ControllerInfo controllerInfo, Path path) {
        String pathPattern = path.value();

        String controllerPattern = controllerInfo.getPathUrl();
        return getPathMatcher().combine(controllerPattern, pathPattern);
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
