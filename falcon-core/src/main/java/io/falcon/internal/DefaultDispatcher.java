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

import com.google.common.io.Closeables;
import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Stage;
import io.falcon.*;
import io.falcon.internal.result.StatusCodeActionResult;
import io.falcon.io.Resource;
import io.falcon.io.ResourceLoader;
import io.falcon.route.Router;
import io.falcon.servlet.Dispatcher;
import io.falcon.servlet.Request;
import io.falcon.utils.OnlyOnceCondition;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * 用于处理Rest请求调度的核心类
 * User: xux
 * Date: 13-10-17
 * Time: 下午9:08
 * To change this template use File | Settings | File Templates.
 */
@Singleton
public class DefaultDispatcher implements Dispatcher {

    public  final Logger logger;

    private static Stage CurrentStage;  //当前阶段
    private String RootPath;
    private static String IP;
    private ResourceLoader ResourceLoader;

    private final Falcon fly;

    private final Router router;

    private final StatusCodeActionResult statusCodeActionResult;

    private final Key<RequestCycle> defaultRequestCycletKey = Key.get(RequestCycle.class, SkySystem.class);

    private final MultipartConfigElement config;


    @Inject
    public DefaultDispatcher(Falcon fly, Router router, StatusCodeActionResult statusCodeActionResult, MultipartConfigElement config) {
        this.fly = fly;
        this.router = router;
        this.statusCodeActionResult = statusCodeActionResult;
        this.config = config;
        this.logger = fly.logger();
        logger.debug("constructed. {}",this.getClass());
    }

    @Override
    public void init(ServletContext sc) throws Exception{
        StringBuilder info = (new StringBuilder("Falcon (")).append(
                "version:").append(Version.getVersion());
        // TODO 监控 tracking
        info.append("; stage:").append(fly.currentStage().toString())
                .append("; trace:").append(" noworking");

        logger.info((new StringBuilder()).append("Starting ")
                .append(info.toString()).append(" ...)").toString());
        RootPath = sc.getContextPath();
//        ResourceLoader = new ServletContextResourceLoader(sc);

    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) {

        Request flyRequest = new Request(request, config);
        try {
            RequestCycle requestCycle = bindRequestCycle(flyRequest, response);

            route(requestCycle);
        } finally {
            try {
                Closeables.close(flyRequest, false);
            } catch (IOException e) {
                logger.error(e.getMessage(),e);
            }
        }
    }

    private RequestCycle bindRequestCycle(HttpServletRequest request, HttpServletResponse response) {
        Cycle cycle = new Cycle(request, response);
        localContext.set(cycle);

        RequestCycle requestCycle = fly.injector().getInstance(defaultRequestCycletKey);
        // 增加默认参数到model
        requestCycle.getModel().add("__cycle", requestCycle);

        cycle.setCycle(requestCycle);
        return requestCycle;
    }



    private void route(RequestCycle cycle) {
        try {
            Result result = router.route(cycle);

            if (Result.NULL == result)
                result = statusCodeActionResult.getSc404();

            result.render(cycle);

        } catch (Throwable e) {

            statusCodeActionResult.render405(cycle);

//            e.printStackTrace();

            logger.error(String.format("fail to route. url:%s", cycle.getClient().getRelativeUrl()), e);

            //TODO: catch any exceptions.

        } finally {
            localContext.remove();
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public HttpServletRequest currentRequest() {
        return getCycle().getRequest();
    }
    @Override
    public HttpServletResponse currentResponse() {
        return getCycle().getResponse();
    }
    @Override
    public RequestCycle currentCycle() {
        return getCycle().getCycle();
    }

    @Override
    public Resource getResource(String location) throws IOException {
        if (!location.startsWith("/") && !location.startsWith("\\\\")
                && !location.matches("^[a-zA-Z]+:.*$"))
            location = (new StringBuilder()).append("/")
                    .append(location).toString();
        return ResourceLoader.getResource(location);
    }

    private Cycle getCycle() {
        Cycle context = localContext.get();
        if (context == null) {
            throw new OutOfScopeException("Cannot access scoped object. Either we"
                    + " are not currently inside an HTTP Servlet currentRequest, or you may"
                    + " have forgotten to apply " + DefaultDispatcher.class.getName()
                    + " as a servlet filter for this currentRequest.");
        }
        return context;
    }

    final ThreadLocal<Cycle> localContext = new ThreadLocal<Cycle>();

    private static class Cycle {

        final HttpServletRequest request;
        final HttpServletResponse response;

        RequestCycle cycle;

        OnlyOnceCondition onlyOnce = OnlyOnceCondition.create("The current beat has been created.");

        Cycle(HttpServletRequest request, HttpServletResponse response) {
            this.request = request;
            this.response = response;
        }

        HttpServletRequest getRequest() {
            return request;
        }

        HttpServletResponse getResponse() {
            return response;
        }

        RequestCycle getCycle() {
            return cycle;
        }

        void setCycle(RequestCycle cycle) {
            onlyOnce.check();
            this.cycle = cycle;
        }
    }
}
