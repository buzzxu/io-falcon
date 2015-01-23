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
package io.falcon.annotations;

import io.falcon.interceptor.PostInterceptor;

import java.lang.annotation.*;
/**
 * 设置POST拦截器
 * User: xux
 * Date: 13-10-17
 * Time: 下午4:24
 * To change this template use File | Settings | File Templates.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PostInterceptorAnnotation {
    Class<? extends PostInterceptor> value();
}
