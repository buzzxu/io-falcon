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
package io.falcon.internal.result.statuscode;

import io.falcon.SkyException;
import io.falcon.RequestCycle;
import io.falcon.Result;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-17
 * Time: 下午5:46
 * To change this template use File | Settings | File Templates.
 */
public class ActionResults {
    private ActionResults() {}

    public static Result redirect(final String url) {
        return new Result() {
            @Override
            public void render(RequestCycle RequestCycle) {
                try {
                    RequestCycle.getResponse().sendRedirect(url);
                } catch (IOException e) {

                    throw SkyException.newBuilder(e)
                            .addContextVariable("redirect url:", url)
                            .build();
                }
            }
        };
    }

    public static Result redirect301(final String url) {
        return new Result() {
            @Override
            public void render(RequestCycle RequestCycle) {
                try {
                    //fixMe: 需要判断是否是同一个schema等因素
                    HttpServletResponse response = RequestCycle.getResponse();
                    response.setStatus(301);
                    response.sendRedirect(url);
                } catch (IOException e) {
                    throw SkyException.newBuilder(e)
                            .addContextVariable("redirect url:", url)
                            .build();
                }
            }
        };
    }
}
