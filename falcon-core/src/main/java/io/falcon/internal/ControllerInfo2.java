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
package io.falcon.internal;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.falcon.Controller;
import io.falcon.Falcon;
import io.falcon.SkyException;
import io.falcon.Result;
import io.falcon.annotations.*;
import io.falcon.internal.action.ActionInfo;
import io.falcon.internal.action.ActionInfo1;
import io.falcon.internal.action.ActionInfo2;
import io.falcon.utils.AnnotationUtils;
import io.falcon.utils.JSON;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 支持注解的ControllerInfo类
 * User: xux
 * Date: 13-10-25
 * Time: 下午4:53
 * To change this template use File | Settings | File Templates.
 */
public class ControllerInfo2 implements ControllerInfo{
    final Class<?> controller;

    final Path path;
    String httpMethod;
    final String pathUrl;
    final Set<Annotation> annotations;

    public ControllerInfo2(Class<?> controller){
        this.controller = controller;
        this.path = AnnotationUtils.findAnnotation(controller, Path.class);

        httpMethod();

        this.annotations = ImmutableSet.copyOf(controller.getAnnotations());
        String pathUrl = path == null ? "/" : path.value();

        if (pathUrl.length() == 0 || pathUrl.charAt(0) != '/')
            pathUrl = '/' + pathUrl;

        this.pathUrl = pathUrl;
    }

    private void httpMethod(){
        List<Class<? extends Annotation>> list = Lists.newArrayList(GET.class,POST.class,PUT.class,DELETE.class);
        for(Class<? extends Annotation> ann : list){
            Annotation $0 = AnnotationUtils.findAnnotation(controller, ann);
            if($0 != null){
                httpMethod = AnnotationUtils.getValue($0).toString();
                break;
            }
        }
    }
    @Override
    public List<ActionInfo> analyze() {
        List<ActionInfo> actions = Lists.newArrayList();
        Set<Method> sets = Sets.filter(
                Sets.newHashSet(controller.getDeclaredMethods())
                , methodFilter);
        for(Method method : sets){
            actions.add(new ActionInfo2(this, method, Falcon.instance));
        }
        return actions;
    }


    @Override
    public Controller getController() {
        return null;
    }

    @Override
    public Class<?> getClazz() {
        return controller;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public String getHttpMethod() {
        return httpMethod;
    }

    @Override
    public String getPathUrl() {
        return pathUrl;
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return annotations;
    }


    public static Predicate<Method> getMethodFilter() {
        return methodFilter;
    }
    private final static Predicate<Method> methodFilter = new Predicate<Method>() {
        @Override
        public boolean apply(Method method) {
//                if (AnnotationUtils.findAnnotation(method, Ignored.class) != null) return false;

            //TODO : 新增类别校验，如果不包含Path则不加载到ActionInfo 中
            if (AnnotationUtils.findAnnotation(method, Path.class) == null) return false;
            Class<?> returnType = method.getReturnType();
            return returnType != null
//                    && Result.class.isAssignableFrom(returnType)
                    && (!method.isBridge()  //TODO: 是否需要处理
                    && method.getDeclaringClass() != Object.class
                    && Modifier.isPublic(method.getModifiers()));
        }
    };


    /**
     * json化方法参数
     */
    protected static class ActionJSONParamsInfo extends ActionInfo2{
        public ActionJSONParamsInfo(ControllerInfo controllerInfo,Method method, Falcon fly){
            super(controllerInfo,method,fly);
        }

        @Override
        public Object invoke(Map<String, String> urlParams) throws Throwable {
            Object[] param = new Object[getParamTypes().size()];
            for(int index = 0; index < getParamNames().size(); index++){
                String paramName = getParamNames().get(index);
                Class<?> clazz = getParamTypes().get(index);

                String v = urlParams.get(paramName);
                if (v == null)
                    throw SkyException.newBuilder("Invoke exception:")
                            .addContextVariable(paramName, "null")
                            .build();
                param[index] = JSON.parse(v, clazz);
            }
            try {
                return  method().invoke(controller(), param);

            } catch (Exception e) {
                throw SkyException.newBuilder("invoke exception.", e)
                        .addContextVariables(urlParams)
                        .build();
            }
        }
    }
}
