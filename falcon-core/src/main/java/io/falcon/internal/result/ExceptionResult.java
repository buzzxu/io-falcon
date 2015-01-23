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

import io.falcon.RequestCycle;
import io.falcon.Result;
import io.falcon.utils.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-25
 * Time: 下午3:26
 * To change this template use File | Settings | File Templates.
 */
public class ExceptionResult implements Result {
    Throwable th;
    public ExceptionResult(Throwable th){
        this.th = th;
    }
    @Override
    public void render(RequestCycle cycle)throws Throwable {
        if(cycle.isFlyInvoke()){
            HttpServletResponse res = cycle.getResponse();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            try { th.printStackTrace(pw); } finally { pw.close(); sw.close(); }
            StringBuilder json = new StringBuilder("{'name':'").append(th.getClass());
            json.append("','message':").append(StringUtils.quote(th.getMessage()));
            json.append(",'trace':").append(StringUtils.quote(sw.toString())).append("}");
            res.setHeader("Cache-Control", "no-cache");
            res.setHeader("Pragma", "no-cache");
            res.setHeader("Expires", "-1");
            res.setHeader("fly-rt", "error");
            res.setStatus(500);	//设置异常代码
            res.setContentType("text/javascript; charset=utf-8");
            res.getOutputStream().write(json.toString().getBytes("utf-8"));
        }else{
            throw th;
        }
    }

}
