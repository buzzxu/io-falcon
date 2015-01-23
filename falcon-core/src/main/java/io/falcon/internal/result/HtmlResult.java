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

import io.falcon.SkyException;
import io.falcon.RequestCycle;
import io.falcon.Result;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * HTML返回
 * User: xux
 * Date: 13-10-24
 * Time: 下午9:11
 * To change this template use File | Settings | File Templates.
 */
public class HtmlResult implements Result {

    Object result;
    public HtmlResult(Object result){
        this.result = result;
    }
    @Override
    public void render(RequestCycle cycle)throws Throwable {
        HttpServletResponse res = cycle.getResponse();
        try {
            OutputStream os = res.getOutputStream();
            res.setHeader(RESULT_TYPE_KEY, "html");
            res.setContentType("text/html; charset=\"UTF-8\"");
            res.setCharacterEncoding("UTF-8");
            os.write((result == null ? "" : result.toString())
                    .getBytes("utf-8"));
            os.close();
        } catch (IOException e) {
            throw SkyException.raise(e);
        }
    }
}
