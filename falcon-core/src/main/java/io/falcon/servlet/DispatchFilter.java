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
package io.falcon.servlet;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 利用Filter来实行调度
 * User: xux
 * Date: 13-10-17
 * Time: 下午9:50
 * To change this template use File | Settings | File Templates.
 */
//@WebFilter(urlPatterns = {"/*"},
//        dispatcherTypes = {DispatcherType.REQUEST},
//        initParams = {@WebInitParam(name = "encoding", value = "UTF-8")}
//)
//conventionBinder 组织名称
//spring.configLocation Spring配置文件
//controller.package Controller包路径
//fly.stage 运行级别
public class DispatchFilter implements Filter {
    private Dispatcher dispatcher;
    private String encoding;
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ServletContext servletContext = filterConfig.getServletContext();
        encoding = filterConfig.getInitParameter("encoding");
        try {
            dispatcher = DispatcherFactory.create(servletContext);
            dispatcher.init(servletContext);
        } catch (Exception e) {
            servletContext.log("failed to Falcon initialize, system exit!!!", e);
            System.exit(1);

        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;
        request.setCharacterEncoding(encoding);
        response.setCharacterEncoding(encoding);
        dispatcher.service(httpReq, httpResp);
    }

    @Override
    public void destroy() {
        dispatcher.destroy();
    }

    private String getRequestPath(HttpServletRequest request) {
        String ret = request.getServletPath();
        if (request.getPathInfo() != null)
            ret = ret + request.getPathInfo();
        if (!ret.startsWith("/"))
            ret = "/" + ret;
        return ret;
    }
}
