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
package io.falcon.interceptor;

import io.falcon.RequestCycle;
import io.falcon.Result;

/**
 * 方法执行完成后的拦截处理,可以与
 * User: xux
 * Date: 13-10-17
 * Time: 下午4:22
 * To change this template use File | Settings | File Templates.
 */
public interface PostInterceptor {

    /**
     * 拦截当前请求
     * @param cycle    当前请求的上下文
     * @param result
     * @return
     * null，进入下一个拦截或执行Action
     * <BR/>
     * 非空，直接显示，不进入下一个拦截或执行Action
     */
    Result invoke(RequestCycle cycle, Result result);
}
