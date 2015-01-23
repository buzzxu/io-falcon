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

import io.falcon.Falcon;
import io.falcon.SkyException;
import io.falcon.Result;
import io.falcon.controller.ViewFactory;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author buzz.xux@gmail.com
 * @创建时间：2012-7-31 下午05:25:35
 * @TODO
 * @version 0.0.1
 */
public enum ReturnType {
	JSON(1), TEMPLATE(2),HTML(3),JSON_STRING(4);

	private static final int json = 1;
    private static final int template = 2;
    private static final int html = 3;
    private static final int json_string = 4;
	private int type;
	public static final String RESULT_TYPE_KEY = "fly-rt";
	private ReturnType(int type) {
		this.type = type;
	}

	public Result process(HttpServletResponse res,Object result) throws IOException {
		res.setHeader("Cache-Control", "no-cache");
		res.setHeader("Pragma", "no-cache");
		res.setHeader("Expires", "-1");
		OutputStream os = res.getOutputStream();
		switch (this.type) {
            case json:
                res.setHeader(RESULT_TYPE_KEY, "json");
                res.setContentType("text/javascript; charset=utf-8");
                os.write((result == null ? "null" : io.falcon.utils.JSON.json(result)).getBytes("utf-8"));
                break;
            case template:
                if(result instanceof String){
                    String view = String.class.cast(result);
                    Falcon.instance.injector().getInstance(ViewFactory.class).create(view);
                }else {
                    SkyException.newBuilder("If the return type is a template, the return value must be a string");
                }
                break;
            case html:
                res.setHeader(RESULT_TYPE_KEY, "html");
                res.setContentType("text/html; charset=utf-8");
                os.write((result == null ? "" : result.toString())
                        .getBytes("utf-8"));
                break;
            case json_string:
            res.setHeader(RESULT_TYPE_KEY, "json");
            res.setContentType("text/javascript; charset=utf-8");
            os.write((result == null ? "null" : result.toString())
                        .getBytes("utf-8"));
            break;
		}
		os.flush();
        return null;
	}

}



