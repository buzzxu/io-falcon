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
package io.falcon.convention;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import io.falcon.annotations.GroupConventionAnnotation;
import io.falcon.internal.DefaultGroupConvention;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * 获得GroupConvention的工厂
 * User: xux
 * Date: 13-10-17
 * Time: 下午9:24
 * To change this template use File | Settings | File Templates.
 */
public class GroupConventionFactory {
    public static GroupConvention getGroupConvention(String className ) {
        if (Strings.isNullOrEmpty(className)){
            //无定义 就返回 读取 spring或controllers.packages配置
            return null;
//            className = GroupConvention.annotatedGroupConventionBinder;
        }
        Class<?> clazz = null;
        GroupConvention groupConvention;
        try {
            clazz = GroupConventionFactory.class.getClassLoader().loadClass(className);
            groupConvention = GroupConvention.class.cast(clazz);
        } catch (Exception e) {
            groupConvention = null;
        }

        if (groupConvention != null)
            return groupConvention;

        if (clazz == null || clazz.getAnnotation(GroupConventionAnnotation.class) == null)
            clazz = DefaultGroupConvention.class;

        GroupConventionAnnotation conventionAnnotation = clazz.getAnnotation(GroupConventionAnnotation.class);


        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        URL url = cl.getResource(".");

        File folder = null;
        try {
            folder = new File(url.toURI());
        } catch (URISyntaxException e) {
            Throwables.propagate(e);
        }

        return new DefaultGroupConvention(conventionAnnotation, folder);

    }
}
