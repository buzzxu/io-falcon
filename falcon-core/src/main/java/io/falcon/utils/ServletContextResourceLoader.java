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
package io.falcon.utils;

import io.falcon.io.FileResource;
import io.falcon.io.Resource;
import io.falcon.io.ResourceLoader;
import io.falcon.io.URLResource;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletContext;




/**
 * 
 * @author buzz.xux@gmail.com
 * @创建时间：2012-7-31 下午03:21:29
 * @TODO
 * @version 0.0.1
 */
public class ServletContextResourceLoader implements ResourceLoader {

	private ServletContext servletContext;

	public ServletContextResourceLoader(ServletContext sc) {
		this(sc, null);
	}

	public ServletContextResourceLoader(ServletContext sc, ClassLoader loader) {
		if ((loader == null)
				&& ((loader = Thread.currentThread().getContextClassLoader()) == null))
			loader = getClass().getClassLoader();
		this.servletContext = sc;
	}

	@Override
	public Resource getResource(String paramString) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	private Resource fromFileSystem(String path) throws IOException {
		File file = new File(path);
		if (file.exists())
			return new FileResource(file);
		return null;
	}

	private Resource fromClassLoader(String path) throws IOException {
		URL url = null;
		ClassLoader tcl = Thread.currentThread().getContextClassLoader();
		if (tcl != null)
			url = tcl.getResource(path);
		if (url == null)
			url = ServletContextResourceLoader.class.getClassLoader()
					.getResource(path);
		return toResource(url);
	}
	
	private Resource toResource(URL url)
	  {
	    if (url == null)
	      return null;
	    if ("file".equals(url.getProtocol()))
	    {
	      File file = new File(url.getFile());
	      if (file.exists())
	        return new FileResource(file);
	    }
	    return new URLResource(url);
	  }

}
