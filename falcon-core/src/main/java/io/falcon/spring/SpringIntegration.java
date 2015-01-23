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
package io.falcon.spring;

import com.google.common.collect.Maps;
import com.google.inject.*;
import com.google.inject.name.Names;
import io.falcon.annotations.SkyResource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.ClassUtils;

import java.net.URL;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Integrates Guice with Spring.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class SpringIntegration {
  private SpringIntegration() {}

    private static Map<String,Key> names = Maps.newHashMap();

    public static Module makeModule(URL url) {
        Resource res = new UrlResource(url);
        return makeModule(new XmlApplicationContext(res));
    }

    public static Module makeModule(String filename) {
        return makeModule(new FileSystemXmlApplicationContext(filename));
    }
    public static Module makeModule(final ListableBeanFactory fact) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                SpringIntegration.bindAll(binder(), fact);
                //添加ApplicationContext
                bind(BeanFactory.class).annotatedWith(SkyResource.class)
                        .toInstance(fact);
            }
        };
    }

  /**
   * Creates a provider which looks up objects from Spring using the given name.
   * Expects a binding to {@link
   * org.springframework.beans.factory.BeanFactory}. Example usage:
   *
   * <pre>
   * bind(DataSource.class)
   *   .toProvider(fromSpring(DataSource.class, "dataSource"));
   * </pre>
   */
  public static <T> Provider<T> fromSpring(Class<T> type, String name) {
    return new InjectableSpringProvider<T>(type, name);
  }

  /**
   * Binds all Spring beans from the given factory by name. For a Spring bean
   * named "foo", this method creates a binding to the bean's type and
   * {@code @Named("foo")}.
   *
   * @see com.google.inject.name.Named
   * @see com.google.inject.name.Names#named(String) 
   */
  public static void bindAll(Binder binder, ListableBeanFactory beanFactory) {
    binder = binder.skipSources(SpringIntegration.class);

    for (String name : beanFactory.getBeanDefinitionNames()) {
      Class<?> type = beanFactory.getType(name);
      bindBean(binder, beanFactory, name, type);
    }
  }

  static <T> void bindBean(Binder binder, ListableBeanFactory beanFactory,
      String name, Class<T> type) {
    SpringProvider<T> provider
        = SpringProvider.newInstance(type, name);
    try {
      provider.initialize(beanFactory);
    }
    catch (Exception e) {
      binder.addError(e);
      return;
    }
      //如果是代理类
    if(ClassUtils.isCglibProxyClass(type)){
      try {
          type = (Class<T>)Class.forName(type.getSuperclass().getName());
      } catch (Exception e) {
          e.printStackTrace();
          System.exit(1);
      }
    }
    binder.bind(type)
        .annotatedWith(Names.named(name))
        .toProvider(provider);
      //Key
      names.put(name,Key.get(type,Names.named(name)));
  }

    public static Map<String,Key> Names(){
        return  names;
    }
  static class SpringProvider<T> implements Provider<T> {

    BeanFactory beanFactory;
    boolean singleton;
    final Class<T> type;
    final String name;

    public SpringProvider(Class<T> type, String name) {
      this.type = checkNotNull(type, "type");
      this.name = checkNotNull(name, "name");
    }

    static <T> SpringProvider<T> newInstance(Class<T> type, String name) {
      return new SpringProvider<T>(type, name);
    }

    void initialize(BeanFactory beanFactory) {
      this.beanFactory = beanFactory;
      if (!beanFactory.isTypeMatch(name, type)) {
        throw new ClassCastException("Spring bean named '" + name
            + "' does not implement " + type.getName() + ".");
      }
      singleton = beanFactory.isSingleton(name);
    }

    public T get() {
      return singleton ? getSingleton() : type.cast(beanFactory.getBean(name));
    }

    volatile T instance;

    private T getSingleton() {
      if (instance == null) {
        instance = type.cast(beanFactory.getBean(name));
      }
      return instance;
    }
  }

  static class InjectableSpringProvider<T> extends SpringProvider<T> {

    InjectableSpringProvider(Class<T> type, String name) {
      super(type, name);
    }

    @Inject
    @Override
    void initialize(BeanFactory beanFactory) {
      super.initialize(beanFactory);
    }
  }


    static class XmlApplicationContext extends
            AbstractXmlApplicationContext {
        private String[] mConfigLocations;
        private Resource[] mConfigResources;

        XmlApplicationContext(Resource res) {
            super();
            this.mConfigLocations = new String[] { res.getFilename() };
            this.mConfigResources = new Resource[] { res };
            refresh();
        }

        protected String[] getConfigLocations() {
            return this.mConfigLocations;
        }

        protected Resource[] getConfigResources() {
            return this.mConfigResources;
        }

        protected Resource getResourceByPath(String path) {
            return this.mConfigResources[0];
        }
    }
}
