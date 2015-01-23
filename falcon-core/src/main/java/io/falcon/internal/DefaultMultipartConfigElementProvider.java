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

import com.google.inject.Provider;
import io.falcon.SkyException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-17
 * Time: 下午8:25
 * To change this template use File | Settings | File Templates.
 */
@Singleton
public class DefaultMultipartConfigElementProvider implements Provider<MultipartConfigElement> {

    private final MultipartConfigElement config;

    @Inject
    public DefaultMultipartConfigElementProvider(ServletContext servletContext){
        try {
            File tempDir = (File)servletContext.getAttribute("javax.servlet.context.tempdir");
            config = new MultipartConfigElement(tempDir.getCanonicalPath());
        } catch (IOException e) {
            throw SkyException.raise(e);
        }
    }
    /**
     * Provides an instance of {@code T}. Must never return {@code null}.
     *
     * @throws com.google.inject.OutOfScopeException
     *          when an attempt is made to access a scoped object while the scope
     *          in question is not currently active
     * @throws com.google.inject.ProvisionException
     *          if an instance cannot be provided. Such exceptions include messages
     *          and throwables to describe why provision failed.
     */
    @Override
    public MultipartConfigElement get() {
        return config;
    }
}
