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

import io.falcon.Result;

/**
 * 路由处理的结果
 * User: xux
 * Date: 13-10-17
 * Time: 下午6:18
 * To change this template use File | Settings | File Templates.
 */
public class RouteResult {
    public static RouteResult unMatch() {
        return new RouteResult(false, Result.NULL);
    }

    public static RouteResult invoked(Result result) {
        return new RouteResult(Result.NULL != result, result);
    }

    private final boolean success;
    private final Result result;

    private RouteResult(boolean success, Result result) {
        this.success = success;
        this.result = result;
    }

    public boolean isSuccess() {
        return success;
    }

    public Result getResult() {
        return result;
    }
}
