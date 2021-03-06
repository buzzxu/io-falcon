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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;


/* ------------------------------------------------------------ */
/**
 * TYPE Utilities.
 * Provides various static utiltiy methods for manipulating types and their
 * string representations.
 *
 * @since Jetty 4.1
 */
public class TypeUtil {
    //    private static final Logger LOG = Log.getLogger(TypeUtil.class);
    public static final Class<?>[] NO_ARGS = new Class[]{};
    public static int CR = '\015';
    public static int LF = '\012';

    /* ------------------------------------------------------------ */
    private static final HashMap<String, Class<?>> name2Class=new HashMap<String, Class<?>>();
    static
    {
        name2Class.put("boolean", Boolean.TYPE);
        name2Class.put("byte", Byte.TYPE);
        name2Class.put("char", Character.TYPE);
        name2Class.put("double", Double.TYPE);
        name2Class.put("float", Float.TYPE);
        name2Class.put("int", Integer.TYPE);
        name2Class.put("long", Long.TYPE);
        name2Class.put("short", Short.TYPE);
        name2Class.put("void", Void.TYPE);

        name2Class.put("java.lang.Boolean.TYPE", Boolean.TYPE);
        name2Class.put("java.lang.Byte.TYPE", Byte.TYPE);
        name2Class.put("java.lang.Character.TYPE", Character.TYPE);
        name2Class.put("java.lang.Double.TYPE", Double.TYPE);
        name2Class.put("java.lang.Float.TYPE", Float.TYPE);
        name2Class.put("java.lang.Integer.TYPE", Integer.TYPE);
        name2Class.put("java.lang.Long.TYPE", Long.TYPE);
        name2Class.put("java.lang.Short.TYPE", Short.TYPE);
        name2Class.put("java.lang.Void.TYPE", Void.TYPE);

        name2Class.put("java.lang.Boolean",Boolean.class);
        name2Class.put("java.lang.Byte",Byte.class);
        name2Class.put("java.lang.Character",Character.class);
        name2Class.put("java.lang.Double",Double.class);
        name2Class.put("java.lang.Float",Float.class);
        name2Class.put("java.lang.Integer",Integer.class);
        name2Class.put("java.lang.Long",Long.class);
        name2Class.put("java.lang.Short",Short.class);

        name2Class.put("Boolean",Boolean.class);
        name2Class.put("Byte",Byte.class);
        name2Class.put("Character",Character.class);
        name2Class.put("Double",Double.class);
        name2Class.put("Float",Float.class);
        name2Class.put("Integer",Integer.class);
        name2Class.put("Long",Long.class);
        name2Class.put("Short",Short.class);

        name2Class.put(null, Void.TYPE);
        name2Class.put("string",String.class);
        name2Class.put("String",String.class);
        name2Class.put("java.lang.String",String.class);
    }

    /* ------------------------------------------------------------ */
    private static final HashMap<Class<?>, String> class2Name=new HashMap<Class<?>, String>();
    static
    {
        class2Name.put(Boolean.TYPE,"boolean");
        class2Name.put(Byte.TYPE,"byte");
        class2Name.put(Character.TYPE,"char");
        class2Name.put(Double.TYPE,"double");
        class2Name.put(Float.TYPE,"float");
        class2Name.put(Integer.TYPE,"int");
        class2Name.put(Long.TYPE,"long");
        class2Name.put(Short.TYPE,"short");
        class2Name.put(Void.TYPE,"void");

        class2Name.put(Boolean.class,"java.lang.Boolean");
        class2Name.put(Byte.class,"java.lang.Byte");
        class2Name.put(Character.class,"java.lang.Character");
        class2Name.put(Double.class,"java.lang.Double");
        class2Name.put(Float.class,"java.lang.Float");
        class2Name.put(Integer.class,"java.lang.Integer");
        class2Name.put(Long.class,"java.lang.Long");
        class2Name.put(Short.class,"java.lang.Short");

        class2Name.put(null,"void");
        class2Name.put(String.class,"java.lang.String");
    }

    /* ------------------------------------------------------------ */
    private static final HashMap<Class<?>, Method> class2Value=new HashMap<Class<?>, Method>();
    static
    {
        try
        {
            Class<?>[] s ={String.class};

            class2Value.put(Boolean.TYPE,
                    Boolean.class.getMethod("valueOf",s));
            class2Value.put(Byte.TYPE,
                    Byte.class.getMethod("valueOf",s));
            class2Value.put(Double.TYPE,
                    Double.class.getMethod("valueOf",s));
            class2Value.put(Float.TYPE,
                    Float.class.getMethod("valueOf",s));
            class2Value.put(Integer.TYPE,
                    Integer.class.getMethod("valueOf",s));
            class2Value.put(Long.TYPE,
                    Long.class.getMethod("valueOf",s));
            class2Value.put(Short.TYPE,
                    Short.class.getMethod("valueOf",s));

            class2Value.put(Boolean.class,
                    Boolean.class.getMethod("valueOf",s));
            class2Value.put(Byte.class,
                    Byte.class.getMethod("valueOf",s));
            class2Value.put(Double.class,
                    Double.class.getMethod("valueOf",s));
            class2Value.put(Float.class,
                    Float.class.getMethod("valueOf",s));
            class2Value.put(Integer.class,
                    Integer.class.getMethod("valueOf",s));
            class2Value.put(Long.class,
                    Long.class.getMethod("valueOf",s));
            class2Value.put(Short.class,
                    Short.class.getMethod("valueOf",s));
        }
        catch(Exception e)
        {
            throw new Error(e);
        }
    }



    /* ------------------------------------------------------------ */
    /** Parse an int from a substring.
     * Negative numbers are not handled.
     * @param s String
     * @param offset Offset within string
     * @param length Length of integer or -1 for remainder of string
     * @param base base of the integer
     * @return the parsed integer
     * @throws NumberFormatException if the string cannot be parsed
     */
    public static int parseInt(String s, int offset, int length, int base)
            throws NumberFormatException
    {
        int value=0;

        if (length<0)
            length=s.length()-offset;

        for (int i=0;i<length;i++)
        {
            char c=s.charAt(offset+i);

            int digit=convertHexDigit((int)c);
            if (digit<0 || digit>=base)
                throw new NumberFormatException(s.substring(offset,offset+length));
            value=value*base+digit;
        }
        return value;
    }

    /* ------------------------------------------------------------ */
    public static String toString(byte[] bytes, int base)
    {
        StringBuilder buf = new StringBuilder();
        for (byte b : bytes)
        {
            int bi=0xff&b;
            int c='0'+(bi/base)%base;
            if (c>'9')
                c= 'a'+(c-'0'-10);
            buf.append((char)c);
            c='0'+bi%base;
            if (c>'9')
                c= 'a'+(c-'0'-10);
            buf.append((char)c);
        }
        return buf.toString();
    }


    /* ------------------------------------------------------------ */
    /**
     * @param c An ASCII encoded character 0-9 a-f A-F
     * @return The byte value of the character 0-16.
     */
    public static int convertHexDigit( int c )
    {
        int d= ((c & 0x1f) + ((c >> 6) * 0x19) - 0x10);
        if (d<0 || d>15)
            throw new NumberFormatException("!hex "+c);
        return d;
    }


    /* ------------------------------------------------------------ */
    public static String toHexString(byte b)
    {
        return toHexString(new byte[]{b}, 0, 1);
    }


    /* ------------------------------------------------------------ */
    public static String toHexString(byte[] b,int offset,int length)
    {
        StringBuilder buf = new StringBuilder();
        for (int i=offset;i<offset+length;i++)
        {
            int bi=0xff&b[i];
            int c='0'+(bi/16)%16;
            if (c>'9')
                c= 'A'+(c-'0'-10);
            buf.append((char)c);
            c='0'+bi%16;
            if (c>'9')
                c= 'a'+(c-'0'-10);
            buf.append((char)c);
        }
        return buf.toString();
    }
}
