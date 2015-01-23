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
package io.falcon.internal.action;



import io.falcon.Falcon;
import io.falcon.RequestCycle;
import io.falcon.SkyException;
import io.falcon.client.ClientContext;
import io.falcon.interceptor.PostInterceptor;
import io.falcon.interceptor.PreInterceptor;
import io.falcon.route.RouteBag;
import io.falcon.utils.JSON;
import io.falcon.utils.StringUtils;
import io.falcon.utils.converter.ConverterFactory;

import javax.servlet.http.HttpServletRequest;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-25
 * Time: 下午2:47
 * To change this template use File | Settings | File Templates.
 */
public interface ActionInfo {

    int getOrder();

    String getPathPattern();

    boolean isPattern();

    boolean matchHttpMethod(RouteBag bag);

    boolean match(RouteBag bag, Map<String, String> uriTemplateVariables);

    List<PreInterceptor> getPreInterceptors();

    List<PostInterceptor> getPostInterceptors();

    Method method();

    Object invoke(Map<String, String> urlParams)throws Throwable;

    Object controller();

    List<Class<?>> getParamTypes();

    List<String> getParamNames();

    Falcon fly();
    /**
     * Method调用
     */
    interface MethodInvoke{
        Object invoke(Map<String, String> urlParams) throws Throwable;
    }

    abstract class ParamsMethodInvoke implements MethodInvoke{
        protected ActionInfo actionInfo;
        public ParamsMethodInvoke(ActionInfo actionInfo){
            this.actionInfo = actionInfo;
        }


        protected RequestCycle cycle(Class<?> clazz){
            if (RequestCycle.class.equals(clazz)){
                return actionInfo.fly().requestCycle();
            }
            return null;
        }
    }

    /**
     * URL不含参数
     */
    class URLNonParamsMethodInvoke extends ParamsMethodInvoke{
        public URLNonParamsMethodInvoke(ActionInfo actionInfo){
            super(actionInfo);
        }

        @Override
        public Object invoke(Map<String, String> urlParams) throws Throwable {
            try {
                return  actionInfo.method().invoke(actionInfo.controller(), null);
            } catch (Exception e) {
                throw SkyException.newBuilder("invoke exception.", e)
                        .addContextVariables(urlParams)
                        .build();
            }
        }
    }
    /**
     * 处理URL包含的参数
     */
    class UrlParamsMethodInvoke extends ParamsMethodInvoke{

        private final ConverterFactory converter = new ConverterFactory();
        public UrlParamsMethodInvoke(ActionInfo actionInfo){
            super(actionInfo);
            //验证参数类型是否能转换为基础类型或包装类型
            verifyConvert();
        }

        private void verifyConvert(){
            for(int index = 0; index < actionInfo.getParamNames().size(); index++){
                String paramName = actionInfo.getParamNames().get(index);
                Class<?> clazz = actionInfo.getParamTypes().get(index);
                if(!converter.canConvert(clazz))
                    throw SkyException.newBuilder("Invoke cannot convert parameter.")
                            .addContextVariable(paramName, "expect the " + clazz.getName() + "  to primitive type or wrapped type")
                            .build();
            }
        }
        @Override
        public Object invoke(Map<String, String> urlParams) throws Throwable {
            Object[] param = new Object[actionInfo.getParamTypes().size()];
            for(int index = 0; index < actionInfo.getParamNames().size(); index++){
                String paramName = actionInfo.getParamNames().get(index);
                Class<?> clazz = actionInfo.getParamTypes().get(index);
                String v = urlParams.get(paramName);
                if (v == null)
                    throw SkyException.newBuilder("Invoke exception:")
                            .addContextVariable(paramName, "null")
                            .build();
                param[index] = converter.convert(clazz, v);
            }
            try {
                return  actionInfo.method().invoke(actionInfo.controller(), param);

            } catch (Exception e) {
                throw SkyException.newBuilder("invoke exception.", e)
                        .addContextVariables(urlParams)
                        .build();
            }
        }
    }

    /**
     * 方法参数中只含有RequestCycle类型的参数
     */
     class RequestCycleOnceParamsMethodInvoke extends ParamsMethodInvoke{
         protected  Falcon falcon;
         public RequestCycleOnceParamsMethodInvoke(ActionInfo actionInfo){
             super(actionInfo);
             falcon = actionInfo.fly();
         }
         @Override
         public Object invoke(Map<String, String> urlParams) throws Throwable {
             Object[] param = new Object[]{falcon.requestCycle()};
             try {
                 return actionInfo.method().invoke(actionInfo.controller(),param);
             }catch (Exception e) {
                 throw SkyException.newBuilder("invoke exception.", e)
                         .addContextVariables(urlParams)
                         .build();
             }
         }
     }
    /**
     * Post/Get请求 JSON处理参数  一般用于Ajax请求
     */
     class JSONParamsMethodInvoke extends ParamsMethodInvoke{
        protected  Falcon falcon;
        public JSONParamsMethodInvoke(ActionInfo actionInfo){
            super(actionInfo);
            falcon = actionInfo.fly();
        }

        @Override
        public Object invoke(Map<String, String> urlParams) throws Throwable {
            Object[] param = new Object[actionInfo.getParamTypes().size()];
            HttpServletRequest request = falcon.currentRequest();
            for(int index = 0; index < actionInfo.getParamNames().size(); index++){
                String paramName = actionInfo.getParamNames().get(index);
                Class<?> clazz = actionInfo.getParamTypes().get(index);

                //Ajax 请求时，无论get或post请求都从parameter中获取
                String v = request.getParameter(paramName);
                if ( StringUtils.isBlank(v))
                    throw SkyException.newBuilder("Invoke exception:")
                            .addContextVariable(paramName, "null")
                            .build();
                param[index] = JSON.parse(v.toString(), clazz);
            }
            try {
                return  actionInfo.method().invoke(actionInfo.controller(), param);
            } catch (Exception e) {
                throw SkyException.newBuilder("Invoke exception.", e)
                        .addContextVariables(clientContext().forms())
                        .build();
            }
        }

        protected ClientContext clientContext() {
            return falcon.requestCycle().getClient();
        }
    }

    /**
     * 从form里获取数据并json转化传入参数
     */
    class JSONFormParams1MethodInvoke extends ParamsMethodInvoke{
        protected  Falcon falcon;
        public JSONFormParams1MethodInvoke(ActionInfo actionInfo){
            super(actionInfo);
            falcon = actionInfo.fly();
        }
        @Override
        public Object invoke(Map<String, String> urlParams) throws Throwable {
            Object[] args = new Object[actionInfo.getParamTypes().size()];
            if(args.length > 0 ){
                Map<String, Collection<String>> forms =  falcon.requestCycle().getClient().forms();
                if(forms.size() != args.length ){
                    throw SkyException.newBuilder("Parameter list and the server does not match the request method, check the client.").build();
                }
                Iterator<Collection<String>> it = forms.values().iterator();
                int index = 0;
                while(it.hasNext()){
                    Class<?> clazz = actionInfo.getParamTypes().get(index);
                    args[index] = JSON.parse(it.next().iterator().next(), clazz);
                    index++;
                }
            }
            try {
                return  actionInfo.method().invoke(actionInfo.controller(), args);
            } catch (Exception e) {
                throw SkyException.newBuilder("Invoke exception.", e)
                        .addContextVariables(clientContext().forms())
                        .build();
            }
        }
        protected ClientContext clientContext() {
            return falcon.requestCycle().getClient();
        }
    }
    /**
     *
     */
    class WebMethodParamsMethodInvoke extends JSONParamsMethodInvoke{
        protected  Falcon falcon;
        private static String PARAMS = "params";
        public WebMethodParamsMethodInvoke(ActionInfo actionInfo){
            super(actionInfo);
            falcon = actionInfo.fly();
        }

        @Override
        public Object invoke(Map<String, String> urlParams) throws Throwable {
            Object[] param = new Object[actionInfo.getParamTypes().size()];
            HttpServletRequest request = falcon.currentRequest();
            String params = request.getParameter(PARAMS);
            if(StringUtils.isBlank(params)){
                params = "{}";
            }
            //以‘#’号分割参数
            String[] temp = params.split("#");
            if(temp.length != actionInfo.getParamNames().size()){
                throw SkyException.newBuilder("Request parameters and the method does not match the number of parameters ").build();
            }
            for(int index = 0; index < actionInfo.getParamNames().size(); index++){
                Class<?> clazz = actionInfo.getParamTypes().get(index);
                param[index] = JSON.parse(temp[index],clazz);
            }
            try {
                return  actionInfo.method().invoke(actionInfo.controller(), param);
            } catch (Exception e) {
                throw SkyException.newBuilder("Invoke exception.", e)
                        .addContextVariables(clientContext().forms())
                        .build();
            }
        }
    }
}
