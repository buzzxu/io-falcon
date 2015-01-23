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

import io.falcon.Falcon;
import io.falcon.SkyException;
import io.falcon.RequestCycle;
import io.falcon.Result;
import io.falcon.controller.ViewFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.io.VelocityWriter;

import org.apache.velocity.runtime.RuntimeInstance;
/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-17
 * Time: 下午9:29
 * To change this template use File | Settings | File Templates.
 */
public class VelocityViewFactory implements ViewFactory {
    private final RuntimeInstance rtInstance;

    private final String suffix = ".html";

    @Inject
    public VelocityViewFactory(Falcon fly) {
        String viewFolder = viewFolderPath(fly);

        Properties ps = new Properties();
        ps.setProperty("resource.loader", "file");
        ps.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        ps.setProperty("file.resource.loader.path", viewFolder);
        ps.setProperty("file.resource.loader.cache", "false");
        ps.setProperty("file.resource.loader.modificationCheckInterval", "2");
        ps.setProperty("input.encoding", "UTF-8");
        ps.setProperty("output.encoding", "UTF-8");
        ps.setProperty("default.contentType", "text/html; charset=UTF-8");
        ps.setProperty("velocimarco.library.autoreload", "true");
        ps.setProperty("runtime.log.error.stacktrace", "false");
        ps.setProperty("runtime.log.warn.stacktrace", "false");
        ps.setProperty("runtime.log.info.stacktrace", "false");
        ps.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
        ps.setProperty("runtime.log.logsystem.log4j.category", "velocity_log");

        rtInstance = new RuntimeInstance();

        try {
            rtInstance.init(ps);
        } catch (Exception e) {
            throw SkyException.raise(e);
        }
    }

    private String viewFolderPath(Falcon fly) {
        File parent = fly.currentFolder();
        return new File(parent, "views").getAbsolutePath();
    }
    @Override
    public Result create(String viewName) {
        return new VelocityViewResult(this, viewName);
    }
    Template getTemplate(String viewName) {
        return rtInstance.getTemplate(viewName + suffix);
    }

    private static class VelocityViewResult implements Result {
        private final VelocityViewFactory factory;

        private final String viewName;

        private VelocityViewResult(VelocityViewFactory factory, String viewName) {
            this.factory = factory;
            this.viewName = viewName;
        }

        @Override
        public void render(RequestCycle cycle) {

            Template template =  factory.getTemplate(viewName);

            HttpServletResponse response = cycle.getResponse();
            response.setContentType("text/html;charset=\"UTF-8\"");
            response.setCharacterEncoding("UTF-8");
            // init context:
            Context context = new VelocityContext(cycle.getModel().getModel());
            // render:
            VelocityWriter vw = null;
            try {
                vw = new VelocityWriter(response.getWriter());
                template.merge(context, vw);
                vw.flush();
            } catch (IOException e) {
                throw SkyException.raise(e);
            }
            finally {
                if (vw != null)
                    vw.recycle(null);
            }
        }
    }
}
