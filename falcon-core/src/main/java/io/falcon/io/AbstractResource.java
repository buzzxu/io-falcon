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
package io.falcon.io;

import java.io.IOException;
import java.io.InputStream;


/** 
 *
 * @author  buzz.xux@gmail.com
 * @创建时间：2012-7-31 下午03:36:24 
 * @TODO 
 * @version 0.0.1
 */
public abstract class AbstractResource implements Resource {
	private String mName;
	private String mContentType;

	public AbstractResource() {
	}

	public AbstractResource(String name) {
		this.mName = name;
	}

	public AbstractResource(String name, String contentType) {
		this.mName = name;
		this.mContentType = contentType;
	}

	public String getName() {
		return this.mName;
	}

	public String getContentType() {
		return this.mContentType;
	}

	public Object read(InputStreamCallback isc) throws IOException {
		InputStream is = getInputStream();
		Object localObject1;
		try {
			localObject1 = isc.callbackWithInputStream(is);
		} finally {
			
			is.close();
		}
		return localObject1;
	}

	protected void setName(String name) {
		this.mName = name;
	}

	protected void setContentType(String contentType) {
		this.mContentType = contentType;
	}

	protected abstract InputStream getInputStream() throws IOException;
}
 
