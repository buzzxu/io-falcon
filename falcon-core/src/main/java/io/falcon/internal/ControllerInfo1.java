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
import io.falcon.Result;
import io.falcon.annotations.GET;
import io.falcon.annotations.POST;
import io.falcon.annotations.Path;
import io.falcon.internal.action.ActionInfo;
import io.falcon.internal.action.ActionInfo1;
import io.falcon.utils.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-17
 * Time: 下午5:48
 * To change this template use File | Settings | File Templates.
 */
public class ControllerInfo1 implements ControllerInfo{
    final Controller controller;
    final Class<?> clazz;
    final Path path;
    final boolean isGet;
    final boolean isPost;
    final String pathUrl;

    final Set<Annotation> annotations;
    public ControllerInfo1(Controller controller) {
        this.controller = controller;
        clazz = controller.getClass();
        this.path = AnnotationUtils.findAnnotation(clazz, Path.class);

        boolean isGet = AnnotationUtils.findAnnotation(clazz, GET.class) != null;
        boolean isPost = AnnotationUtils.findAnnotation(clazz, POST.class) != null;

        if (!isGet && !isPost) {
            isGet = true;
            isPost = true;
        }

        this.isGet = isGet;
        this.isPost = isPost;

        this.annotations = ImmutableSet.copyOf(clazz.getAnnotations());

        String pathUrl = path == null ? "/" : path.value();

        if (pathUrl.length() == 0 || pathUrl.charAt(0) != '/')
            pathUrl = '/' + pathUrl;

        this.pathUrl = pathUrl;
    }

    public List<ActionInfo> analyze() {
        List<ActionInfo> actions = Lists.newArrayList();


        Set<Method> sets = Sets.filter(
                Sets.newHashSet(clazz.getDeclaredMethods())
                , methodFilter);
//TODO : checkMe
        for(Method method : sets){

            //todo: Falcon.instance
            actions.add(new ActionInfo1(this, method, Falcon.instance));
        }


        return actions;
    }


    public Controller getController() {
        return controller;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Path getPath() {
        return path;
    }

    public boolean isGet() {
        return isGet;
    }

    public boolean isPost() {
        return isPost;
    }

    public String getPathUrl() {
        return pathUrl;
    }

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
                    && Result.class.isAssignableFrom(returnType)
                    && (!method.isBridge()  //TODO: 是否需要处理
                    && method.getDeclaringClass() != Object.class
                    && Modifier.isPublic(method.getModifiers()));
        }
    };
}
