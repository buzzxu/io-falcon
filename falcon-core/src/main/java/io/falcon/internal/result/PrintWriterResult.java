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
import io.falcon.RequestCycle;
import io.falcon.Result;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-26
 * Time: 下午4:50
 * To change this template use File | Settings | File Templates.
 */
public class PrintWriterResult implements Result {

    private final HttpServletResponse response;
    private PrintWriter writer = null;

    public PrintWriterResult(HttpServletResponse response) {
        this.response = response;
    }
    public PrintWriterResult() {
        this.response = Falcon.instance.currentResponse();
    }
    private PrintWriter getPrintWriter() {
        if (writer != null)
            return writer;
        try {
            writer = response.getWriter();
            return writer;
        } catch (IOException e) {
            throw SkyException.raise(e);
        }
    }

    public PrintWriterResult setStatus(int sc) {
        response.setStatus(sc);
        return this;
    }

    public PrintWriterResult setContentType(String type) {
        response.setContentType(type);
        return this;
    }

    public PrintWriterResult setDateHeader(String name, long date) {
        response.setDateHeader(name, date);
        return this;
    }

    public PrintWriterResult addDateHeader(String name, long date) {
        response.addDateHeader(name, date);
        return this;
    }

    public PrintWriterResult setHeader(String name, String value) {
        response.setHeader(name, value);
        return this;
    }

    public PrintWriterResult addHeader(String name, String value) {
        response.addHeader(name, value);
        return this;
    }

    public PrintWriterResult setIntHeader(String name, int value) {
        response.setIntHeader(name, value);
        return this;
    }

    public PrintWriterResult addIntHeader(String name, int value) {
        response.addIntHeader(name, value);
        return this;
    }

    public PrintWriterResult write(int c) {
        getPrintWriter().write(c);
        return this;
    }

    public PrintWriterResult write(String s) {
        getPrintWriter().write(s);
        return this;
    }

    public PrintWriterResult write(String format, Object ... args) {
        getPrintWriter().format(format, args);
        return this;
    }


    @Override
    public void render(RequestCycle cycle) {

        if (writer != null) {
            writer.flush();
            writer.close();
        }

    }
}
