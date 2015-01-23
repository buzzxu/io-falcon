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

import com.google.inject.ImplementedBy;
import io.falcon.RequestCycle;
import io.falcon.Result;
import io.falcon.internal.DefaultRouter;

/**
 * 路由器，根据每个请求的url进行匹配找到合适的
 * @see Action
 * 来执行
 * User: xux
 * Date: 13-10-17
 * Time: 下午6:19
 * To change this template use File | Settings | File Templates.
 */
@ImplementedBy(DefaultRouter.class)
public interface Router {

    Result route(RequestCycle cycle);
}
