package io.falcon.inject;

import com.google.inject.AbstractModule;
import io.falcon.annotations.Module;
import io.falcon.annotations.Plugin;
import io.falcon.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import javax.servlet.ServletContext;
import java.util.Set;

/**
 * Created by Administrator on 14-1-6.
 */
public class DefaultModule extends AbstractModule {
    protected Logger logger = LogManager.getLogger("Fly");
    private final ServletContext servletContext;
    public DefaultModule(ServletContext servletContext){
        this.servletContext = servletContext;
    }
    @Override
    protected void configure() {
        String str = servletContext.getInitParameter("package");
        if(StringUtils.isBlank(str)){
            return ;
        }
        String[] pkgs = str.split("\\s+");
        try {
            for(String pkg : pkgs){
                Reflections reflections = new Reflections(new ConfigurationBuilder()
                        .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(pkg)))
                        .setUrls(ClasspathHelper.forPackage(pkg))
                        .setScanners(
                                new SubTypesScanner(),
    //                            new ResourcesScanner(),
                                new TypeAnnotationsScanner()
                        ));
                //普通bean注入
//                Set<Class<?>> $beans = reflections.getTypesAnnotatedWith(Named.class);
//                logger.info("finding $beans in [{}], got {} classes", new Object[] { pkg,
//                        $beans.size() });
//                for(Class<?> bean : $beans){
//                    bind(bean);
//                }
                //Plugin插件
                Set<Class<?>> $plugins = reflections.getTypesAnnotatedWith(Plugin.class);

                //Module
                Set<Class<?>> $moules = reflections.getTypesAnnotatedWith(Module.class);
                logger.info("finding module in [{}], got {} classes", new Object[] { pkg,
                        $moules.size() });
                for(Class<?> moule : $moules){
                    logger.info("loading module [{}]",new Object[]{moule.getName()});
                    install(AbstractModule.class.cast(moule.newInstance()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
