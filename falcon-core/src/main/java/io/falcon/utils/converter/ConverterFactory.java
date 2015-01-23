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
package io.falcon.utils.converter;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for all converters.
 *
 * fixMe: 可以改成注入
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class ConverterFactory {

//    private static final Log log = LogFactory.getLog(ConverterFactory.class);

    private static final Map<Class<?>, Converter<?>> map = new HashMap<>();
    static{
        loadInternal();
    }
    public ConverterFactory() {

    }

    static void loadInternal() {

        Converter<?> c = new StringConverter();
        map.put(String.class, c);

        c = new BooleanConverter();
        map.put(boolean.class, c);
        map.put(Boolean.class, c);

        c = new CharacterConverter();
        map.put(char.class, c);
        map.put(Character.class, c);

        c = new ByteConverter();
        map.put(byte.class, c);
        map.put(Byte.class, c);

        c = new ShortConverter();
        map.put(short.class, c);
        map.put(Short.class, c);

        c = new IntegerConverter();
        map.put(int.class, c);
        map.put(Integer.class, c);

        c = new LongConverter();
        map.put(long.class, c);
        map.put(Long.class, c);

        c = new FloatConverter();
        map.put(float.class, c);
        map.put(Float.class, c);

        c = new DoubleConverter();
        map.put(double.class, c);
        map.put(Double.class, c);
    }


    public boolean canConvert(Class<?> clazz) {
        return clazz.equals(String.class) || map.containsKey(clazz);
    }

    public Object convert(Class<?> clazz, String s) {
        Converter<?> c = map.get(clazz);
        return c.convert(s);
    }
}
