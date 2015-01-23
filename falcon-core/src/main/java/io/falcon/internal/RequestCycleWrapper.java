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
package io.falcon.internal;

import io.falcon.Model;
import io.falcon.RequestCycle;
import io.falcon.client.ClientContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-17
 * Time: 下午9:06
 * To change this template use File | Settings | File Templates.
 */
public class RequestCycleWrapper implements RequestCycle{

    RequestCycle cycle;
    boolean isFlyInvoke = false;
    public RequestCycleWrapper(RequestCycle cycle){
        this.cycle = cycle;
    }
    @Override
    public Model getModel() {
        return this.cycle.getModel();
    }

    /**
     * 返回本次调用的 {@link javax.servlet.http.HttpServletRequest}对象
     *
     * @return 当前请求
     */
    @Override
    public HttpServletRequest getRequest() {
        return this.cycle.getRequest();
    }

    /**
     * 返回本次调用的 {@link javax.servlet.http.HttpServletResponse}对象
     *
     * @return 当前response
     */
    @Override
    public HttpServletResponse getResponse() {
        return this.cycle.getResponse();
    }

    /**
     * 得到ServletContext信息
     *
     * @return 当前ServletContext
     */
    @Override
    public ServletContext getServletContext() {
        return this.cycle.getServletContext();
    }

    /**
     * 获得客户端的信息
     *
     * @return 客户端信息
     */
    @Override
    public ClientContext getClient() {
        return this.cycle.getClient();
    }

    @Override
    public boolean isFlyInvoke() {
        return isFlyInvoke;
    }

    @Override
    public void flyinvoke() {
        isFlyInvoke = true;
    }
}
