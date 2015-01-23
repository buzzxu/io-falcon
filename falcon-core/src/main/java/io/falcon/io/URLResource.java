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
import java.net.URL;


/** 
 *
 * @author  buzz.xux@gmail.com
 * @创建时间：2012-7-31 下午03:36:45 
 * @TODO 
 * @version 0.0.1
 */
public class URLResource extends AbstractResource{
	private URL mURL;

	  public URLResource(URL url)
	  {
	    this.mURL = url;
	  }

	  protected InputStream getInputStream() throws IOException
	  {
	    return this.mURL.openStream();
	  }
}
 
