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


package io.falcon.test.inner;

import com.google.common.base.Preconditions;

import java.lang.annotation.Annotation;


/**
 * 
 * @author java2enterprise@gmail.com (James Wang)
 * @since Nov 20, 2009
 * 
 */
public abstract class Annotations {

	public static <A extends Annotation> A findInherited(Class<?> clazz, Class<A> annotationType) {
		Preconditions.checkNotNull(clazz, "Class must not be null");
		A annotation = clazz.getAnnotation(annotationType);
		if (annotation != null) {
			return annotation;
		}
		for (Class<?> ifc : clazz.getInterfaces()) {
			annotation = findInherited(ifc, annotationType);
			if (annotation != null) {
				return annotation;
			}
		}
		Class<?> superClass = clazz.getSuperclass();
		if (superClass == null || superClass == Object.class) {
			return null;
		}
		return findInherited(superClass, annotationType);
	}

}
