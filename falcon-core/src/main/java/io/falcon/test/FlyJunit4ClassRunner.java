/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.falcon.test;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import io.falcon.spring.SpringIntegration;
import io.falcon.test.inner.Annotations;
import io.falcon.test.inner.Instances;
import io.falcon.utils.ResourceUtils;
import io.falcon.utils.StringUtils;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * 
 * @author java2enterprise@gmail.com (James Wang)
 * @since Nov 20, 2009
 * 
 */
public class FlyJunit4ClassRunner extends BlockJUnit4ClassRunner {
	
	private Injector injector;
	
	private Collection<TestInterceptor> interceptors = new ArrayList<TestInterceptor>();

	private Object testInstance;
    private boolean userModule;
	
	public FlyJunit4ClassRunner(Class<?> clazz) throws InitializationError {
		super(clazz);
        retrieve(clazz);
		retrieveInterceptors(clazz);
	}

    private void retrieve(Class<?> clazz ){
        ModuleWith moduleWith = Annotations.findInherited(clazz, ModuleWith.class);
        if(moduleWith != null){
            List<Module> modules = Lists.newArrayList();
            for(Class<? extends Module> module : moduleWith.value()){
                modules.add(Instances.create(module));
            }
            //读取配置
            retrieveContextConf(modules,clazz);
            injector = Guice.createInjector(modules);
        }else{
            InjectWith injectWith = Annotations.findInherited(clazz, InjectWith.class);
            Preconditions.checkNotNull(injectWith, "Annotation " + InjectWith.class.getSimpleName() + " needed to present on " + clazz + " or it's super class or interfaces.");
            injector = Instances.create(injectWith.value()).get();
        }
    }
    private void retrieveContextConf(List<Module> modules,Class<?> clazz ){
        ContextConfiguration contextConf = Annotations.findInherited(clazz, ContextConfiguration.class);
        if(contextConf != null){
            if(StringUtils.isNotBlank(contextConf.spring())){
                modules.add(SpringIntegration.makeModule(contextConf.spring()));
            }
            for(final String property : contextConf.properties()){

                modules.add(new Module() {
                    @Override
                    public void configure(Binder binder) {
                        Properties prop = new Properties();
                        InputStream in = null;
                        try {
                            in = new FileInputStream(ResourceUtils.getFile(property));
                            prop.load(in);
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.exit(1);
                        }finally {
                            try {
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.exit(1);
                            }
                        }
                        Names.bindProperties(binder, prop);
                    }
                });
            }
        }
    }


	private void retrieveInterceptors(Class<?> clazz) {
		/** TODO process cumulative */
		InterceptWith testInterceptors = Annotations.findInherited(clazz, InterceptWith.class);
		if (testInterceptors == null) {
			return;
		}
		
		Class<? extends TestInterceptor>[] interceptorTypes = testInterceptors.value();
		
		for (Class<? extends TestInterceptor> type : interceptorTypes) {
			interceptors.add(Instances.create(type));
		}
	}
	
	@Override
	protected Object createTest() throws Exception {
		Object instance = super.createTest();
		this.injector.injectMembers(instance);
		//TODO is this way safe ?
		this.testInstance = instance;
		return instance;
	}

    private EachTestNotifier guiceMakeNotifier(FrameworkMethod method, RunNotifier notifier){
        Description description = describeChild(method);
        return new EachTestNotifier(notifier, description);
    }
    @Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier) {

		EachTestNotifier eachNotifier= guiceMakeNotifier(method, notifier);
		TestContext testContext = new TestContext(injector, testInstance, getTestClass().getJavaClass(), method.getMethod());
		
		for (TestInterceptor interceptor : interceptors) {
			try {
				interceptor.before(testContext);
			} catch (Exception e) {
				eachNotifier.addFailure(e);
			}
		}
		
		try {
			super.runChild(method, notifier);
		} finally {
			for (TestInterceptor interceptor : interceptors) {
				try {
					interceptor.after(testContext);
				} catch (Exception e) {
					eachNotifier.addFailure(e);
				}
			}	
		}
	}
	
	
	
	
	
	
}
