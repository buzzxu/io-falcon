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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;



/** 
 *
 * @author  buzz.xux@gmail.com
 * @创建时间：2012-7-31 下午03:24:21 
 * @TODO 
 * @version 0.0.1
 */
public class FileResource implements Resource {

	private static final Timer TIMER = new Timer(true);

	private static Map<String, String> CONTENT_TYPE_MAP = new HashMap();
	private File mFile;
	private String mContentType;

	public FileResource(String file) {
		this(new File(file));
	}

	public FileResource(File file) {
		this.mFile = file;
	}

	public String getName() {
		return this.mFile.getName();
	}

	public String getContentType() {
		if (this.mContentType == null) {
			String tmp = getName();
			int ix = tmp.lastIndexOf('.');
			if (ix > 0)
				this.mContentType = ((String) CONTENT_TYPE_MAP.get(tmp
						.substring(ix + 1).toLowerCase()));
		}
		return this.mContentType;
	}

	public Object read(InputStreamCallback isc) throws IOException {
		if (!canRead())
			throw new IOException("Can not be read");
		InputStream is = new FileInputStream(this.mFile);
		Object localObject1;
		try {
			localObject1 = isc.callbackWithInputStream(is);
		} finally {
			
			is.close();
		}
		return localObject1;
	}

	public boolean canRead() {
		return this.mFile.canRead();
	}

	public boolean canWrite() {
		return this.mFile.canWrite();
	}

	public long getLength() {
		return this.mFile.length();
	}

	public long getLastModified() {
		return this.mFile.lastModified();
	}

	static {
		CONTENT_TYPE_MAP.put("txt", "text/plain");
		CONTENT_TYPE_MAP.put("htm", "text/html");
		CONTENT_TYPE_MAP.put("html", "text/html");
		CONTENT_TYPE_MAP.put("js", "text/javascript");
		CONTENT_TYPE_MAP.put("css", "text/css");
		CONTENT_TYPE_MAP.put("xml", "text/xml");
		CONTENT_TYPE_MAP.put("xsl", "text/xml");
		CONTENT_TYPE_MAP.put("bmp", "image/bmp");
		CONTENT_TYPE_MAP.put("gif", "image/gif");
		CONTENT_TYPE_MAP.put("jpg", "image/jpeg");
		CONTENT_TYPE_MAP.put("jpeg", "image/jpeg");
		CONTENT_TYPE_MAP.put("png", "image/png");
		CONTENT_TYPE_MAP.put("tiff", "image/tiff");
		CONTENT_TYPE_MAP.put("doc", "application/msword");
		CONTENT_TYPE_MAP.put("mdb", "application/msaccess");
		CONTENT_TYPE_MAP.put("xls", "application/vnd.ms-excel");
		CONTENT_TYPE_MAP.put("mpp", "application/vnd.ms-project");
		CONTENT_TYPE_MAP.put("ppt", "application/vnd.ms-powerpoint");
		CONTENT_TYPE_MAP.put("pdf", "application/pdf");
		CONTENT_TYPE_MAP.put("zip", "application/zip");
		CONTENT_TYPE_MAP.put("gzip", "application/x-gzip");
		CONTENT_TYPE_MAP.put("jar", "application/x-java-archive");
		CONTENT_TYPE_MAP.put("class", "application/x-java-vm");
	}
}
 
