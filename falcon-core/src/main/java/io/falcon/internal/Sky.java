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


import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;

import io.falcon.annotations.Controller;
import io.falcon.controller.AbstractController;
import io.falcon.convention.EmptyModule;
import io.falcon.spring.SpringIntegration;
import io.falcon.utils.PerfTracker;
import io.falcon.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;


import javax.inject.Singleton;
import javax.servlet.ServletContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-21
 * Time: 下午2:40
 * To change this template use File | Settings | File Templates.
 */
public abstract class Sky {
    protected static Logger logger = LogManager.getLogger("Falcon");
    protected PerfTracker perf = new PerfTracker("Fly");

    /**
     * 读取Spring配置
     * @param servletContext
     * @return
     */
    protected Module fromSpring(ServletContext servletContext){
        String str = servletContext.getInitParameter("spring.configLocation");
        if (str != null && str.length() > 0) {
            String modules[] = str.split("\\s+");
            try {
                for (int i = 0; i < modules.length; i++) {
                    String tmp[] = modules[i].split("[=]");
                    String ctx;
                    String module;
                    if (tmp.length == 1) {
                        ctx = null;
                        module = modules[i];
                    } else {
                        ctx = tmp[0];
                        module = tmp[1];
                    }
                    URL url;
                    if ((url = servletContext.getResource(module)) == null) {
                        logger.info("Spring module definition [] not found.",module);
                    }else{
                        try {
                            perf.start();
                            return SpringIntegration.makeModule(url);
                        } finally {
                            perf.stop("Loading Spring module definition {0} . ",module);
                        }
                    }
                }
            } catch (MalformedURLException e) {
                logger.error(e.getMessage(),e);
                System.exit(1);
            }
        }
        return new EmptyModule();
    }

    protected Module fromControllerPackage(ServletContext servletContext){
        String str = servletContext.getInitParameter("controller.package");
        if(StringUtils.isNotBlank(str)){
            return parseControllers(str.split("\\s+"));
        }
        return new EmptyModule();
    }



    private Module parseControllers(final String[] pkgs){
        return new AbstractModule() {
            private Set<Class<?>> controllers = Sets.newHashSet();

            @Override
            protected void configure() {
                try {
                    System.out.println("****************************************************");
                    for(String pkg : pkgs){

                        Reflections reflections = new Reflections(new ConfigurationBuilder()
                                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(pkg)))
                                .setUrls(ClasspathHelper.forPackage(pkg))
                                .setScanners(
                                        new SubTypesScanner(),
                                        //                            new ResourcesScanner(),
                                        new TypeAnnotationsScanner()
                                ));

                        //加载Module
                        Set<Class<?>> $moules = reflections.getTypesAnnotatedWith(io.falcon.annotations.Module.class);
                        StringUtils.println("**\n**\n**\tfinding module in [%s], got %s classes", pkg, $moules.size());
                        for(Class<?> moule : $moules){
                            if(logger.isDebugEnabled()){
                                StringUtils.println("**\tloading module [%s]", new Object[]{moule.getName()});
                            }
                            install(AbstractModule.class.cast(moule.newInstance()));
                        }
                        System.out.println("**");
                        Set<Class<?>> annotatedClzs = reflections.getTypesAnnotatedWith(Controller.class);
                        StringUtils.println("**\t@Controller\t++[%s]+++++",annotatedClzs.size());
                        System.out.println("**");
                        for (Class<?> controller : annotatedClzs){
                            bind(controller);
                            controllers.add(controller);
                            if(logger.isDebugEnabled()){
                                StringUtils.println("**\t%s", controller.getName());
                            }
                        }

                        Set<Class<? extends io.falcon.Controller>> imClzs = reflections
                                .getSubTypesOf(io.falcon.Controller.class);
                        StringUtils.println("**\tSubClasses\t++[%s]+++++",imClzs.size());
                        System.out.println("**");
                        for (Class<?> controller : imClzs){
                            //如果注解中已包含则跳过
                            if(annotatedClzs.contains(controller)){
                                continue;
                            }
                            bind(controller);
                            controllers.add(controller);
                            if(logger.isDebugEnabled()){
                                StringUtils.println("**\t%s", controller.getName());
                            }
                        }



                    }
                    System.out.println("**\n****************************************************");
                }catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }

            }

            @Provides
            @SkySystem
            @Singleton
            private Set<Class<?>> getControllers(){
                return controllers;
            }

        };
    }

}
