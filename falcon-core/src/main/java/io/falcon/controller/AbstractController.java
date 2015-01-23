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
package io.falcon.controller;



import io.falcon.*;
import io.falcon.internal.result.PrintWriterResult;
import io.falcon.internal.result.TemplateResult;
import io.falcon.internal.result.statuscode.ActionResults;
import org.apache.logging.log4j.Logger;


import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-17
 * Time: 下午9:26
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractController implements Controller {
    private static final Logger logger = Falcon.instance.logger();

    @Inject
    private Falcon fly;

    @Override
    public void init() {

        logger().info("initialize");
    }

    /**
     * 返回一个view的ActionResult
     * 系统默认采用velocity实现。<br/>
     * viewName + .html存放的目录在 maven项目的resources/views下
     * 编译后存放在classes/views下
     *
     * @param viewName view的名字
     * @return 合适ActionResult
     */
    protected Result view(String viewName) {
        return new TemplateResult(fly,viewName);
    }

    /**
     * 跳转到一个新页面
     *
     * @param redirectUrl 调整页面的url
     * @return Http 302 跳转
     */
    protected Result redirect(String redirectUrl) {
        return ActionResults.redirect(redirectUrl);
    }

    /**
     * 301永久跳转到一个新页面
     *
     * @param redirectUrl 调整页面的url
     * @return Http 1 跳转
     */
    protected Result redirect301(String redirectUrl) {
        return ActionResults.redirect301(redirectUrl);
    }

    /**
     * 得到model
     *
     * @return 当前请求的model
     */
    protected Model model() {

        return  cycle().getModel();
    }

    /**
     * 获得当前的上下文信息
     *
     * @return 当前beat
     */
    protected RequestCycle cycle() {

        return fly.requestCycle();
    }

    /**
     * 获得当前的request
     *
     * @return 获得当前的request
     */
    protected HttpServletRequest request() {

        return  cycle().getRequest();
    }

    /**
     * 获得当前的response
     *
     * @return 获得当前的response
     */
    protected HttpServletResponse response() {

        return  cycle().getResponse();
    }

    /**
     * 获得当前类的logger
     * @return 当前类的logger
     */
    protected Logger logger() {
        return logger;
    }



    /**
     * 获得 Fly
     * @return
     */
    protected Falcon fly() {
        return this.fly;
    }

    /**
     * 以原始的方式提供写response的方法
     * @return 写Response
     */
    protected InnerPrintWriter writer() {
        return new InnerPrintWriter(cycle().getResponse());
    }

    protected static class InnerPrintWriter extends PrintWriterResult{
        InnerPrintWriter(HttpServletResponse response){
            super(response);
        }
    }
}
