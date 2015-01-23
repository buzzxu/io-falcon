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

import com.google.inject.Injector;

import java.lang.reflect.Method;

/**
 * 
 * @author java2enterprise@gmail.com (James Wang)
 * @since Nov 20, 2009
 * 
 */
public class TestContext {

	private Method testMethod;
	private Class<?> testClass;
	private Injector injector;
	private Object testInstance;

	public TestContext(Injector injector, Object testInstance, Class<?> testClass, Method testMethod) {
		this.testMethod = testMethod;
		this.testClass = testClass;
		this.injector = injector;
		this.testInstance = testInstance;
	}

	public Method getTestMethod() {
		return testMethod;
	}

	public Class<?> getTestClass() {
		return testClass;
	}
	
	public Injector getInjector() {
		return injector;
	}

	public Object getTestInstance() {
		return testInstance;
	}



}
