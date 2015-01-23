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



import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;


public class Log {
	private static final String FQCN = Log.class.getName();
	private Logger logger;
	private PerfTracker perf = null;

	private Log(String category) {
		this.logger = LogManager.getLogger(category);
	}

	public static Log getLogger(Object o) {
		String category = null;
		if ((o instanceof String))
			category = (String) o;
		else if ((o instanceof Class))
			category = ((Class) o).getName();
		else if (o != null) {
			category = o.toString();
		}
		return new Log(category);
	}

	protected static String format(String format, Object... args) {
		return MessageFormat.format(format, args);
	}

	public void log(Level level, Object message, Throwable t) {
		this.logger.log(level,message,t);

	}

	public PerfTracker perf() {
		if (this.perf == null)
			this.perf = new PerfTracker(this);
		return this.perf;
	}

	public void debug(Object message) {
		debug(message, null);
	}

	public void debug(String format, Object... messages) {
		debug(format(format, messages));
	}

	public void debug(Object message, Throwable t) {
		log(Level.DEBUG, message, t);
	}

	public void info(Object message) {
		info(message, null);
	}

	public void info(String format, Object... messages) {
		info(format(format, messages));
	}

	public void info(Object message, Throwable t) {
		log(Level.INFO, message, t);
	}

	public void warn(Object message) {
		warn(message, null);
	}

	public void warn(String format, Object... messages) {
		warn(format(format, messages));
	}

	public void warn(Object message, Throwable t) {
		log(Level.WARN, message, t);
	}

	public void error(Object message) {
		error(message, null);
	}

	public void error(String format, Object... messages) {
		error(format(format, messages));
	}

	public void error(Object message, Throwable t) {
		log(Level.ERROR, message, t);
	}



}
