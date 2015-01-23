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
package io.falcon.internal.result;

import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import io.falcon.SkyException;
import io.falcon.RequestCycle;
import io.falcon.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-17
 * Time: 下午5:07
 * To change this template use File | Settings | File Templates.
 */
public class StaticActionResult {

    @ImplementedBy(DefaultFactory.class)
    public static interface Factory {
        Result create(String patch);
    }
    private static class DefaultFactory implements Factory {
        @Inject
        public DefaultFactory(){}

        @Override
        public Result create(String path) {
            return new DefaultStaticResult(path);
        }
    }

    private static class DefaultStaticResult implements Result{
        private final String path;

        public DefaultStaticResult(String path) {
            this.path = path;

        }

        @Override
        public void render(RequestCycle cycle) {

            HttpServletRequest request = cycle.getRequest();
            HttpServletResponse response = cycle.getResponse();

            try {
                // 交给web容器处理
                request.getRequestDispatcher(path).forward(request, response);
            } catch (Throwable e) {
                throw SkyException
                        .newBuilder(e)
                        .addContextVariable("File", path)
                        .build();
            }

        }
    }
}
