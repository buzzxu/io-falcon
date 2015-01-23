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
package io.falcon.route;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-17
 * Time: 下午6:18
 * To change this template use File | Settings | File Templates.
 */
public interface Action {
    /**
     * 确定优先级，路由时根据优先级进行匹配
     * @return 优先级
     */
    double order();

    /**
     * 匹配并且执行
     * @param bag 当前路由信息
     * @return 匹配或执行的结果
     */
    RouteResult matchAndInvoke(RouteBag bag);
}
