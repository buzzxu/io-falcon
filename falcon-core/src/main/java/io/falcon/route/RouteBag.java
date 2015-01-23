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

import io.falcon.RequestCycle;
import io.falcon.utils.PathUtils;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-17
 * Time: 下午6:21
 * To change this template use File | Settings | File Templates.
 */
public class RouteBag {
    public static RouteBag create(RequestCycle cycle) {
        return new RouteBag(cycle);
    }

    private final RequestCycle cycle;
    private final String method;
    private final boolean isGet;
    private final boolean isPost;
    private final String path;
    private final String simplyPath;

    private RouteBag(RequestCycle cycle) {
        this.cycle = cycle;

        path = cycle.getClient().getRelativeUrl();
        simplyPath = PathUtils.simplyWithoutSuffix(path);

        String requestMethod = cycle.getRequest().getMethod().toUpperCase();
        this.method = requestMethod;
        isPost = "POST".equals(requestMethod);
        isGet = !isPost;
    }

    public RequestCycle getCycle() {
        return cycle;
    }

    public boolean isGet() {
        return isGet;
    }

    public boolean isPost() {
        return isPost;
    }

    public String getPath() {
        return path;
    }

    public String getSimplyPath() {
        return simplyPath;
    }

    public String getMethod() {
        return method;
    }
}
