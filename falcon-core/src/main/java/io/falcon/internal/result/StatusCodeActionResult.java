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


import io.falcon.RequestCycle;
import io.falcon.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-17
 * Time: 下午5:39
 * To change this template use File | Settings | File Templates.
 */
@Singleton
public class StatusCodeActionResult {
    static Logger logger = LogManager.getLogger(StatusCodeActionResult.class);
    public final static Result defaultSc404 = new Result() {
        @Override
        public void render(RequestCycle cycle)throws Throwable {
            HttpServletResponse response = cycle.getResponse();
            try{
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }catch (IOException e) {
                logger.error(e.getMessage(),e);
            }
        }
    };

    public final static Result defaultSc405 = new Result() {
        @Override
        public void render(RequestCycle cycle)throws Throwable {
            HttpServletResponse response = cycle.getResponse();
            try{
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }catch (IOException e) {
                logger.error(e.getMessage(),e);
            }
        }
    };
    @Inject
    @Named("HTTP_STATUS=404")
    Result sc404;

    @Inject
    @Named("HTTP_STATUS=405")
    Result sc405;

    public void render404(RequestCycle cycle) {
        try {
            sc404.render(cycle);
        } catch (Throwable throwable) {

        }
    }

    public void render405(RequestCycle cycle){
        try {
            sc405.render(cycle);
        } catch (Throwable throwable) {

        }
    }

    public Result getSc404(){
        return sc404;
    }
    public Result getSc405(){
        return sc405;
    }
}
