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
package io.falcon;

import java.util.Map;

import com.google.common.collect.Maps;

public class SkyException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3937891321310249697L;

	public static SkyExceptionBuilder newBuilder(String message) {
        return newBuilder(message, null);
    }

    public static SkyExceptionBuilder newBuilder(Throwable cause) {
        return newBuilder("", cause);
    }

    public static SkyExceptionBuilder newBuilder() {
        return newBuilder("", null);
    }

    public static SkyExceptionBuilder newBuilder(String message, Throwable cause) {
        return new SkyExceptionBuilder(message, cause);
    }



    SkyException() {
        super();
    }

    SkyException(String message) {
        super(message);
    }

    SkyException(Throwable cause) {
        super(cause);
    }

    SkyException(String message, Throwable cause) {
        super(message, cause);
    }

    public static SkyException raise(String message) {
        return new SkyException(message);
    }

    public static SkyException raise(Throwable cause) {
        return new SkyException(cause);
    }

    public static SkyException raise(String message, Throwable cause) {
        return new SkyException(message, cause);
    }

    public static class SkyExceptionBuilder {
        private final Map<String, Object> contextInfos = Maps.newLinkedHashMap();

        private final Throwable cause;

        private final String currentMessage;

        SkyExceptionBuilder(String message, Throwable cause) {
            this.currentMessage = message;
            this.cause = cause;
        }

        SkyExceptionBuilder(Throwable cause) {
            this("", cause);
        }

        SkyExceptionBuilder(String message) {
            this(message, null);
        }

        /**
         * 给异常增加上下文变量信息。
         * @param name 变量名
         * @param value 变量值
         * @return 自身
         */
        public SkyExceptionBuilder addContextVariable(String name, Object value) {
            contextInfos.put(name, value);
            return this;
        }

        public SkyExceptionBuilder addContextVariables(Map<?, ?> variables) {
            for (Map.Entry entry : variables.entrySet())
                addContextVariable(entry.toString(), entry.getValue());

            return this;
        }

        /**
         * 创建一个SkyException
         */
        public SkyException build() {
            return new SkyException(getContextInfo(), cause);
        }

        /**
         * throw
         * @param clazz
         * @param <T>
         * @return
         */
        public <T> T raise(Class<T> clazz) {
            throw build();
        }

        private String getContextInfo() {
            return this.currentMessage +
                    (contextInfos.size() > 0  ?  "\ncontext: "  + contextInfos.toString()
                            : "");
        }
    }
}
