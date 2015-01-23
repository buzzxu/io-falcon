package io.falcon.internal;

import com.google.common.base.Strings;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import io.falcon.Falcon;
import io.falcon.Model;
import io.falcon.RequestCycle;
import io.falcon.Result;
import io.falcon.controller.ViewFactory;
import io.falcon.utils.StringUtils;
import freemarker.cache.*;
import freemarker.ext.servlet.*;
import freemarker.template.*;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-11-10
 * Time: 下午1:38
 * To change this template use File | Settings | File Templates.
 */
@Singleton
public class FreeMarkerViewFactory implements ViewFactory {
    private Logger logger;
    private Configuration config;
    private ObjectWrapper wrapper;
    private final String suffix = ".html";
    private String contentType;
    private static final String EXPIRATION_DATE;
    private int updateDelay = 0; //更新模版 开发环境设置0
    private boolean nocache = true;    //TODO 生产时改为false

    public static final String KEY_REQUEST = "Request";
    public static final String KEY_INCLUDE = "include_page";
    public static final String KEY_REQUEST_PRIVATE = "__FreeMarkerServlet.Request__";
    public static final String KEY_REQUEST_PARAMETERS = "RequestParameters";
    public static final String KEY_SESSION = "Session";

    // Note these names start with dot, so they're essentially invisible from
    // a freemarker script.
    private static final String ATTR_REQUEST_MODEL = ".freemarker.Request";
    private static final String ATTR_REQUEST_PARAMETERS_MODEL =
            ".freemarker.RequestParameters";
    private static final String ATTR_SESSION_MODEL = ".freemarker.Session";
    static {
        // Generate expiration date that is one year from now in the past
        GregorianCalendar expiration = new GregorianCalendar();
        expiration.roll(Calendar.YEAR, -1);
        SimpleDateFormat httpDate =
                new SimpleDateFormat(
                        "EEE, dd MMM yyyy HH:mm:ss z",
                        Locale.US);
        EXPIRATION_DATE = httpDate.format(expiration.getTime());
    }
    @Inject
    public FreeMarkerViewFactory(Falcon fly){
        logger = fly.logger();
        config = new Configuration(Configuration.VERSION_2_3_21);

        contentType = "text/html;charset=\"UTF-8\"";
        wrapper = Configuration.getDefaultObjectWrapper(Configuration.VERSION_2_3_21);
        logger.debug("Using object wrapper of class " + wrapper.getClass().getName());
        config.setObjectWrapper(wrapper);
        try {
            //配置模版文件路径地址
            config.setTemplateLoader(createTemplateLoader(fly));
            if(fly.currentStage() == Stage.DEVELOPMENT){
                //Execute Hander
//            config.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);
                config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
            }else{
                //更新模版 生产改为 updateDelay = 18000
                updateDelay = 18000;
            }

            //编码
//            config.setEncoding(Locale.CHINA,"UTF-8");
            config.setDefaultEncoding("UTF-8");
            config.setOutputEncoding("UTF-8");
            config.setTemplateUpdateDelay(updateDelay);
            config.setTagSyntax(0);
            config.setLocale(Locale.CHINA);
            //缓存 最近最少使用策略
            config.setCacheStorage(new MruCacheStorage(20,100));
            settings();
        } catch (Exception e) {
           throw new RuntimeException(e);
        }
    }
    private void settings() throws TemplateException {
        config.setSetting("number_format","0.##########");
        config.setSetting("date_format","yyyy-MM-dd");
        config.setSetting("time_format","HH:mm:ss");
        config.setSetting("datetime_format","yyyy-MM-dd HH:mm:ss");
    }
    private TemplateLoader createTemplateLoader(Falcon fly) throws IOException
    {
        String templatePath = fly.getTemplateFolder();
        if(Strings.isNullOrEmpty(templatePath)){
            templatePath = "class://views/";
        }
        if (templatePath.startsWith("class://")) {
            // substring(7) is intentional as we "reuse" the last slash
            return new ClassTemplateLoader(getClass(), templatePath.substring(7));
        } else {
            if (templatePath.startsWith("file://")) {
                templatePath = templatePath.substring(7);
                return new FileTemplateLoader(new File(templatePath));
            } else {
                return new WebappTemplateLoader(fly.servletContext(), templatePath);
            }
        }
    }

    private Template getTemplate(String viewName) throws IOException {
        return config.getTemplate(viewName+ suffix);
    }
    @Override
    public Result create(String viewName) {
        return new ViewResult(this,viewName);
    }


    private static class ViewResult implements Result {

        private final FreeMarkerViewFactory factory;

        private final String viewName;

        public ViewResult(FreeMarkerViewFactory factory, String viewName) {
            this.factory = factory;
            this.viewName = viewName;
        }

        @Override
        public void render(RequestCycle cycle) throws Throwable {
            process(cycle);
        }

        private void process(RequestCycle cycle) throws IOException {
            HttpServletResponse response = cycle.getResponse();
            Template template = null;
            try {
                template = factory.getTemplate(viewName);
            } catch (FileNotFoundException e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            // Set cache policy
			setBrowserCachingPolicy(response);

            // template.process(ContextHolder.getRequestCycle().getModel(),response.getWriter());
            try {
                //暂时不添加TemplateModel
//                TemplateModel model = createModel(cycle.getModel(),factory.wrapper,cycle.getServletContext(), cycle.getRequest(), response);
                response.setContentType("text/html;charset=\"UTF-8\"");
                response.setCharacterEncoding("UTF-8");
                template.process(cycle.getModel().getModel(), response.getWriter(),factory.wrapper);
                response.getWriter().flush();
            } catch (TemplateException te) {
                if (factory.config.getTemplateExceptionHandler().getClass().getName()
                        .indexOf("Debug") != -1) {
                    Falcon.instance.logger().error("Error executing FreeMarker template", te);
                } else {
                    throw new RuntimeException(te);
                }
            }
        }


        private TemplateModel createModel(Model model,ObjectWrapper wrapper,
                                            ServletContext servletContext,
                                            final HttpServletRequest request,
                                            final HttpServletResponse response)
                throws TemplateModelException {
            try {
                AllHttpScopesHashModel params = new AllHttpScopesHashModel(
                        wrapper, servletContext, request);
                // Create hash model wrapper for session
                HttpSessionHashModel sessionModel;
                HttpSession session = request.getSession(false);
                if (session != null) {
                    sessionModel = (HttpSessionHashModel) session
                            .getAttribute(ATTR_SESSION_MODEL);
                    if (sessionModel == null) {
                        sessionModel = new HttpSessionHashModel(session,
                                wrapper);
                        initializeSessionAndInstallModel(request, response,
                                sessionModel, session);
                    }
                } else {
                    sessionModel = new HttpSessionHashModel(request.getSession(true), wrapper);
                }
                params.putUnlistedModel(KEY_SESSION, sessionModel);

                // Create hash model wrapper for request
                HttpRequestHashModel requestModel = (HttpRequestHashModel) request
                        .getAttribute(ATTR_REQUEST_MODEL);
                if (requestModel == null
                        || requestModel.getRequest() != request) {
                    requestModel = new HttpRequestHashModel(request, response,
                            wrapper);
                    request.setAttribute(ATTR_REQUEST_MODEL, requestModel);
                    request.setAttribute(ATTR_REQUEST_PARAMETERS_MODEL,
                            new HttpRequestParametersHashModel(request));
                }
                params.putUnlistedModel(KEY_REQUEST, requestModel);
                params.putUnlistedModel(KEY_INCLUDE, new IncludePage(request,
                        response));
                params.putUnlistedModel(KEY_REQUEST_PRIVATE, requestModel);

                // Create hash model wrapper for request parameters
                HttpRequestParametersHashModel requestParametersModel = (HttpRequestParametersHashModel) request
                        .getAttribute(ATTR_REQUEST_PARAMETERS_MODEL);
                params.putUnlistedModel(KEY_REQUEST_PARAMETERS,
                        requestParametersModel);
                // 添加数据
                params.putAll(model.getModel());
                return params;
            } catch (ServletException e) {
                throw new TemplateModelException(e);
            } catch (IOException e) {
                throw new TemplateModelException(e);
            }
        }

        void initializeSessionAndInstallModel(HttpServletRequest request,
                                              HttpServletResponse response,
                                              HttpSessionHashModel sessionModel, HttpSession session)
                throws ServletException, IOException {
            session.setAttribute(ATTR_SESSION_MODEL, sessionModel);
        }
        private void setBrowserCachingPolicy(HttpServletResponse res) {
            if (true) {
                // HTTP/1.1 + IE extensions
                res.setHeader("Cache-Control",
                        "no-store, no-cache, must-revalidate, "
                                + "post-check=0, pre-check=0");
                // HTTP/1.0
                res.setHeader("Pragma", "no-cache");
                // Last resort for those that ignore all of the above
                res.setHeader("Expires", EXPIRATION_DATE);
            }
        }
    }
}
