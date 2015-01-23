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
package io.falcon.inject;

import static io.falcon.internal.SkyScopes.REQUEST;
import static io.falcon.internal.SkyScopes.SESSION;
import static io.falcon.internal.SkyScopes.THREAD;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import io.falcon.*;
import io.falcon.Controller;
import io.falcon.annotations.*;
import io.falcon.client.ClientContext;
import io.falcon.controller.ViewFactory;
import io.falcon.convention.GroupConvention;
import io.falcon.internal.*;
import io.falcon.internal.result.StatusCodeActionResult;
import io.falcon.route.Action;

import javax.inject.Singleton;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;

/**
 * 默认注入，所有注入项都可以被project级及group级注入覆盖
 * User: xux
 * Date: 13-10-17
 * Time: 下午3:45
 * To change this template use File | Settings | File Templates.
 */
public class SkyModule extends AbstractModule {
    private final Falcon fly;

    public SkyModule(Falcon fly){
        this.fly = fly;
    }
    /**
     * Configures a {@link com.google.inject.Binder} via the exposed methods.
     */
    @Override
    protected void configure() {

        bindScope(RequestScoped.class, REQUEST);
        bindScope(SessionScoped.class, SESSION);
        bindScope(ThreadScoped.class,THREAD);
        bind(ServletRequest.class).to(HttpServletRequest.class);
        bind(ServletResponse.class).to(HttpServletResponse.class);

        bind(RequestCycle.class).annotatedWith(SkySystem.class)
                .to(DefaultCycle.class);
        bind(Result.class)
                .annotatedWith(Names.named("HTTP_STATUS=404"))
                .toInstance(StatusCodeActionResult.defaultSc404);
        bind(Result.class)
                .annotatedWith(Names.named("HTTP_STATUS=405"))
                .toInstance(StatusCodeActionResult.defaultSc405);

        bind(Action.class).annotatedWith(StaticActionAnnotation.class)
                .to(StaticFilesAction.class);

        bind(ClientContext.class).to(ClientContext.DefaultClientContext.class);

        bind(Model.class).to(DefaultModel.class);

        bind(MultipartConfigElement.class)
                .toProvider(DefaultMultipartConfigElementProvider.class)
                .in(Singleton.class);

        //判断使用的模版类型
        switch (fly.getTemplateName().toLowerCase()){
            case "freemarker":
                //Freemarker模版
                bind(ViewFactory.class).annotatedWith(Names.named(fly.getTemplateName()))
                        .to(FreeMarkerViewFactory.class).in(Singleton.class);
                break;
            case "velocity":
                //Freemarker模版
                bind(ViewFactory.class).annotatedWith(Names.named(fly.getTemplateName()))
                        .to(VelocityViewFactory.class).in(Singleton.class);
                break;
        }

        //对外提供Falcon资源
//        bind(RequestCycle.class).annotatedWith(FalconResource.class)
//                .toProvider(new RequestCycleProvider(fly));

        // bind all controllers.
        for (Class<? extends Controller> clazz : fly.getControllerClasses()) {
            bind(clazz).in(Singleton.class);
        }
    }

    @Provides
    private HttpServletRequest provideReuqest() {
        return fly.currentRequest();
    }

    @Provides
    private  HttpServletResponse provideResponse() {
        return fly.currentResponse();
    }

    @Provides
    @SkySystem
    @Singleton
    private Set<Class<? extends Controller>> provideControllerClasses() {
        return fly.getControllerClasses();
    }


    @Provides
    @Singleton
    private GroupConvention provideGroupConvention() {
        return fly.groupConvention();
    }

    @Provides
    @Singleton
    private Falcon provideFly() {
        return fly;
    }

    @Provides
    @Singleton
    private ServletContext provideServletContext() {
        return fly.servletContext();
    }

    @Provides
    private RequestCycle provideRequestCycle() {
        return fly.requestCycle();
    }

    @Provides
    @SkySystem
    @Singleton
    private Executor provideExecutor() {
        return Executors.newCachedThreadPool();
    }


    @Provides
    @Singleton
    private GroupConvention.GroupConfig provideGroupConfig() {
        return fly.groupConvention().group();
    }

    @Provides
    @Singleton
    private GroupConvention.ProjectConfig provideProjectConfig() {
        return fly.groupConvention().currentProject();
    }


    /**
     * 暂时不用
     */
    private class RequestCycleProvider implements Provider<RequestCycle>{
        Falcon fly;
        RequestCycleProvider(Falcon fly){
            this.fly = fly;
        }
        @Override
        public RequestCycle get() {
            return fly.requestCycle();
        }
    }
}
