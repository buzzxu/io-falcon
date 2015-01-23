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
package io.falcon;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.*;
import com.google.inject.name.Names;
import io.falcon.controller.ViewFactory;
import io.falcon.convention.GroupConvention;
import io.falcon.inject.SkyModule;
import io.falcon.internal.Sky;
import io.falcon.servlet.Dispatcher;
import io.falcon.utils.OnlyOnceCondition;
import io.falcon.utils.PerfTracker;
import io.falcon.utils.StringUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

public class Falcon extends Sky {


    public static final Falcon instance = new Falcon();
    public static String TEMPLATE;
    private Stage CurrentStage;
	private Injector injector;
    protected Set<Class<? extends Controller>> controllerClasses;
	private ServletContext servletContext;
    private GroupConvention groupConvention;
    private File currentFolder;
    private final OnlyOnceCondition onlyOnce = OnlyOnceCondition
            .create("Falcon has been initialized.");
    private String ip;
    private String templateName,templateFolder;
    private Falcon(){
        try {
            ip = InetAddress
                    .getByName(InetAddress.getLocalHost().getHostName())
                    .getHostAddress();
        } catch (UnknownHostException e) {
            ip = "UNKNOWN";
        }
    }


    public Dispatcher init(ServletContext servletContext, GroupConvention groupConvention){
        onlyOnce.check();
        logger.info("initializing Falcon...");
        this.servletContext = servletContext;
        this.currentFolder = innerCurrentFolder();
        String stage = Strings.nullToEmpty(servletContext.getInitParameter("sky.stage"));

        if (StringUtils.equals(stage,"devel"))
            CurrentStage = Stage.DEVELOPMENT;
        else
            CurrentStage = Stage.PRODUCTION;


        List<Module> modules = Lists.newArrayList();
        if(groupConvention != null){
            this.groupConvention = groupConvention;
            this.controllerClasses = groupConvention.currentProject().controllerClasses();
            Module groupModule = groupConvention.group().module();
            if (null != groupModule)
                modules.add(groupModule);
            Module projectModule = groupConvention.currentProject().module();
            if (null != projectModule)
                modules.add(projectModule);
        }else{
            this.controllerClasses = Sets.newHashSet();
        }
        //选择模版    freemarker 和默认模版
        templateName = servletContext.getInitParameter("template.name");
        if(Strings.isNullOrEmpty(templateName)){
            throw new RuntimeException("template name is null.(freemarker or velocity ?)");
        }
        TEMPLATE = templateName;
        //模版位置
        templateFolder = Strings.nullToEmpty(servletContext.getInitParameter("template.folder"));

        modules.add(new SkyModule(this));

        //TODO 暂不作处理
//        modules.add(new DefaultModule(servletContext));
        //SpringMoudle
        modules.add(fromSpring(servletContext));
        //Controllor
        modules.add(fromControllerPackage(servletContext));



        logger.info("preparing an injector");
        this.injector = buildInjector(modules);
        logger.info("injector completed");


        logger.info("preparing an Falcon dispatcher");
        this.dispatcher = instance(Dispatcher.class);
        logger.info("the Falcon dispatcher completed");
        logger.info("Falcon initialized");
        return dispatcher;
    }


    private Injector buildInjector(List<Module> modules) {
        return Guice.createInjector(modules);
    }

    /**
     * Returns the appropriate instance for the given injection type.
     *
     * @param type the given injection type
     * @return the appropriate instance
     */
    public <T> T instance(Class<T> type) {
        return injector().getInstance(type);
    }
    /**
     * Injects dependencies into the fields and methods of {@code instance}.
     * Ignores the presence of absence of an injectable constructor.
     *
     * @param instance to inject members on
     */
    public <T> T injectMembers(T instance) {
        injector().injectMembers(instance);
        return instance;
    }

    /**
     * an injector singleton instance.
     *
     * @return 注入器
     */
    public Injector injector() {
        return this.injector;
    }
    /**
     * 公用线程池
     *
     * @return 公用线程池
     */
    public Executor commonExecutor() {
        return instance(Executor.class);
    }

    /**
     * 项目的ServletContext
     *
     * @return 项目的ServletContext
     */
    public ServletContext servletContext() {
        return this.servletContext;
    }

    private volatile Dispatcher dispatcher;
    /**
     * Servlet的适配器，负责url转发
     *
     * @return ArgoDispatcher
     */
    public Dispatcher dispatcher() {
        return dispatcher;
    }

    /**
     * 当前的Request
     *
     * @return 当前的Request
     */
    public HttpServletRequest currentRequest() {
        return dispatcher().currentRequest();
    }

    public HttpServletResponse currentResponse() {
        return dispatcher().currentResponse();
    }

    /**
     * 当前请求的上下文
     *
     * @return 当前请求的上下文
     */
    public RequestCycle requestCycle() {
        return dispatcher().currentCycle();
    }

    /**
     * 组织级策略
     *
     * @return 组织级策略
     */
    public GroupConvention groupConvention() {
        return this.groupConvention;
    }

    /**
     * 项目所管理的所有Controller类集合
     *
     * @return 所有Controller类集合
     */
    public Set<Class<? extends Controller>> getControllerClasses() {
        return controllerClasses;
    }

    public Logger logger(){
        return logger;
    }

    public PerfTracker getPerfTracker(){
        return perf;
    }
    /**
     * Classloader所在文件夹
     * @return 启动文件夹 classloader
     */
    public File currentFolder() {
        return currentFolder;
    }

    public String getServerIP(){
        return this.ip;
    }

    public Stage currentStage(){
        return CurrentStage;
    }

    /**
     * 获取模版的文件夹
     * @return
     */
    public String getTemplateFolder(){
        return templateFolder;
    }
    /**
     * 获取使用模版的名称
     * @return
     */
    public String getTemplateName(){
        return templateName;
    }
    /**
     * 获取模版工厂
     * @return
     */
    public ViewFactory getViewFactory(){
        return injector.getInstance(Key.get(ViewFactory.class, Names.named(templateName)));
    }
    private File innerCurrentFolder() {

        return new File(getClass().getResource("/").getFile());
    }
}
