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
package io.falcon.servlet.multi;

import java.nio.charset.Charset;
/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-16
 * Time: 下午5:20
 * To change this template use File | Settings | File Templates.
 */
public class StringUtil {
    private final static StringMap<String> CHARSETS= new StringMap<String>(true);

    public static final String ALL_INTERFACES="0.0.0.0";
    public static final String CRLF="\015\012";
    public static final String __LINE_SEPARATOR=
            System.getProperty("line.separator","\n");

    public static final String __ISO_8859_1="ISO-8859-1";
    public final static String __UTF8="UTF-8";
    public final static String __UTF16="UTF-16";

    public final static Charset __UTF8_CHARSET;
    public final static Charset __ISO_8859_1_CHARSET;
    public final static Charset __UTF16_CHARSET;

    static
    {
        __UTF8_CHARSET=Charset.forName(__UTF8);
        __ISO_8859_1_CHARSET=Charset.forName(__ISO_8859_1);
        __UTF16_CHARSET=Charset.forName(__UTF16);

        CHARSETS.put("UTF-8",__UTF8);
        CHARSETS.put("UTF8",__UTF8);
        CHARSETS.put("UTF-16",__UTF16);
        CHARSETS.put("UTF16",__UTF16);
        CHARSETS.put("ISO-8859-1",__ISO_8859_1);
        CHARSETS.put("ISO_8859_1",__ISO_8859_1);
    }
}
