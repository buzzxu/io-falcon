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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author buzz.xux@gmail.com
 * @创建时间：2012-7-31 下午06:30:23
 * @TODO
 * @version 0.0.1
 */
public class JSON {

	private static final ObjectMapper mapper;

	static{
		mapper  = new ObjectMapper();
		//设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        //回车
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS,true);
	}
	private JSON() {

	}

	public static ObjectMapper getInstance() {

		return mapper;
	}

	public static String json(Object obj) throws IOException{
		StringWriter sw = new StringWriter();
		JsonGenerator gen = new JsonFactory().createGenerator(sw);
		mapper.writeValue(gen, obj);
		gen.close();
		return sw.toString();
	}
	
	public static <T> T parse(String source,Class<T> type){
		try {
			return mapper.readValue(source, type);
		} catch (Exception e) {			
			throw new RuntimeException(e);
		}
	}
	public static Object[] parse(String source, Class<?>[] types)throws IOException {

		Object[] args = new Object[types.length];
		String[] params = source.split("#");
		try {
			// TODO 是否需要改为 ast? 暂时 先这样吧
			for (int i = 0; i < types.length; i++) {
				args[i] = mapper.readValue(params[i], types[i]);
			}
		} catch (IndexOutOfBoundsException e) {
			throw new RuntimeException("the incoming value method parameter type mismatch!");
		}

		return args;
	}

	public static Object deserialize(String json) throws IOException {
		JSONReader reader = new JSONReader();
		return reader.read(json);
	}


	public static Object deserialize(Reader reader) throws IOException {
		// read content
		BufferedReader bufferReader = new BufferedReader(reader);
		String line;
		StringBuilder buffer = new StringBuilder();
		while ((line = bufferReader.readLine()) != null) {
			buffer.append(line);
		}
		return deserialize(buffer.toString());
	}

	@SuppressWarnings("rawtypes")
	public static Map parse(String source) throws IOException{
		return mapper.readValue(source, Map.class);
	}

	public static void main(String[] args) throws JsonParseException,
			JsonMappingException, IOException {
		String a = "{\"$rpc.name\":'test',\"$class\":\"Y29tLnB6b29tLmFwb2xsby5mbHkuUmVtb3RlSW52b2NhdGlvbkhhbmRsZXI=\",\"$rpc.method\":\"say\"}";
		String b = "[\"徐翔\",11]";
		String c = "[\"徐翔\",{\"name\":\"浩博\",\"age\":22}]";
		List value = JSON.mapper.readValue(b, List.class);
		System.out.println(value.toArray());
	}
}
