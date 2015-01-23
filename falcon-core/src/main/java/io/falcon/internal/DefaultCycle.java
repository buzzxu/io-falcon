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

import com.google.inject.Inject;
import io.falcon.Model;
import io.falcon.RequestCycle;
import io.falcon.client.ClientContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 默认周期实现
 * User: xux
 * Date: 13-10-17
 * Time: 下午4:15
 * To change this template use File | Settings | File Templates.
 */
@SkySystem
public class DefaultCycle implements RequestCycle {
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final Model model;
    private final ClientContext clientContext;
    private final ServletContext servletContext;
    private boolean flyInvoke;
    @Inject
    public DefaultCycle(HttpServletRequest request, HttpServletResponse response, Model model,
                        ClientContext clientContext, ServletContext servletContext){
        this.request = request;
        this.response = response;
        this.model = model;
        this.clientContext = clientContext;
        this.servletContext = servletContext;
        this.flyInvoke = isAjaxRequest(request);
    }
    private boolean isAjaxRequest(HttpServletRequest request){
        String header = request.getHeader("X-Requested-With");
        if (header != null && "XMLHttpRequest".equals(header))
            return true;
        else
            return false;
    }
    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public HttpServletRequest getRequest() {
        return request;
    }

    @Override
    public HttpServletResponse getResponse() {
        return response;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public ClientContext getClient() {
        return clientContext;
    }

    @Override
    public boolean isFlyInvoke() {
        return this.flyInvoke;
    }

    @Override
    public void flyinvoke() {
        this.flyInvoke = true;
    }
}
