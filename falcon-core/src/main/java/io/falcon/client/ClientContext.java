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
package io.falcon.client;

import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;
import com.google.inject.ImplementedBy;
import io.falcon.Falcon;
import io.falcon.controller.ReverseProxy;
import io.falcon.servlet.Request;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;

/**
 * 获得客户端信息
 *
 * 包括浏览器端所发送给服务器的所有信息
 * User: xux
 * Date: 13-10-16
 * Time: 下午4:40
 * To change this template use File | Settings | File Templates.
 */
@ImplementedBy(ClientContext.DefaultClientContext.class)
public interface ClientContext {
    /**
     * 获得远程用户cookies
     *
     * @return cookies
     */
    Cookies getCookies();

    /**
     * 得到当前请求的url,不包括参数
     *
     * @return 当前请求的url
     */
    String getRelativeUrl();

    /**
     * 获得url中的参数
     * 其中可以通过注入可以安全获得参赛防止 xss或sql注入等
     * @see SafeParameter#encoding(String)
     *
     * @param name 参数名
     * @return url中的参数值
     */
    String queryString(String name);

    /**
     * 获得url中的参数
     * 其中可以通过注入可以安全获得参赛防止 xss或sql注入等
     * @see SafeParameter#encoding(String)
     *
     * @param name 参数名
     * @return url中的参数值集合
     */
    Collection<String> queryStrings(String name);

    /**
     * 获得post中的参数
     * 其中可以通过注入可以安全获得参赛防止 xss或sql注入等
     * @see SafeParameter#encoding(String)
     *
     * @param name 参数名
     * @return url中的参数值
     */
    String form(String name);

    /**
     * 获得url中的参数
     * 其中可以通过注入可以安全获得参赛防止 xss或sql注入等
     * @see SafeParameter#encoding(String)
     *
     * @param name 参数名
     * @return url中的参数值集合
     */
    Collection<String> forms(String name);

    /**
     * 获得url中的参数
     * 其中可以通过注入可以安全获得参赛防止 xss或sql注入等
     * @see SafeParameter
     *
     * @return 所有url中的参数集合
     */
    Map<String, Collection<String>> queryStrings();


    /**
     * 获得post中的参数，不包括文件
     * 其中可以通过注入可以安全获得参赛防止 xss或sql注入等
     * @see SafeParameter
     *
     * @return 所有post中的参数集合
     */
    Map<String, Collection<String>> forms();

    /**
     * 获得post中文件
     *
     * @param name 文件名
     *
     * @return 对应文件
     */
    Upload getUpload(String name);


    /**
     * 获得post中所有文件
     *
     * @return 对应文件
     */
    Collection<Upload> getUploads();

    /**
     * 获得用户ip
     *
     * @see ReverseProxy
     *
     * @return 用户ip
     */
    InetAddress getAddress();


    String ip();
    /**
     * 默认的Client实现
     */
    public static class DefaultClientContext implements ClientContext {

        private final Request request;
        private final ReverseProxy reverseProxy;

        private Cookies cookies = null;
        private String relativeUrl = null;
        private InetAddress address = null;

        @Inject
        public DefaultClientContext(HttpServletRequest request, ReverseProxy reverseProxy){
            this.request = (Request)request;
            Preconditions.checkNotNull(request);
            this.reverseProxy = reverseProxy;
        }

        /**
         * 获得远程用户cookies
         *
         * @return cookies
         */
        @Override
        public Cookies getCookies() {
            if (cookies != null)
                return cookies;

            Cookie[] cks = request.getCookies();
            cookies = Falcon.instance.instance(Cookies.class);
            for (Cookie ck : cks) {
                cookies.add(ck);
            }
            return cookies;
        }

        /**
         * 得到当前请求的url,不包括参数
         *
         * @return 当前请求的url
         */
        @Override
        public String getRelativeUrl() {
            if (relativeUrl != null)
                return relativeUrl;

            String uri = request.getRequestURI();
            String contextPath = request.getContextPath();
            relativeUrl = uri.substring(contextPath.length());

            return relativeUrl;
        }

        /**
         * 获得url中的参数
         * 其中可以通过注入可以安全获得参赛防止 xss或sql注入等
         *
         * @param name 参数名
         * @return url中的参数值
         * @see io.falcon.client.SafeParameter#encoding(String)
         */
        @Override
        public String queryString(String name) {
            return request.queryString(name);
        }

        /**
         * 获得url中的参数
         * 其中可以通过注入可以安全获得参赛防止 xss或sql注入等
         *
         * @param name 参数名
         * @return url中的参数值集合
         * @see io.falcon.client.SafeParameter#encoding(String)
         */
        @Override
        public Collection<String> queryStrings(String name) {
            return request.queryStrings().get(name);
        }

        /**
         * 获得post中的参数
         * 其中可以通过注入可以安全获得参赛防止 xss或sql注入等
         *
         * @param name 参数名
         * @return url中的参数值
         * @see io.falcon.client.SafeParameter#encoding(String)
         */
        @Override
        public String form(String name) {
            return request.form(name);
        }

        /**
         * 获得url中的参数
         * 其中可以通过注入可以安全获得参赛防止 xss或sql注入等
         *
         * @param name 参数名
         * @return url中的参数值集合
         * @see io.falcon.client.SafeParameter#encoding(String)
         */
        @Override
        public Collection<String> forms(String name) {
            return request.forms().get(name);
        }

        /**
         * 获得url中的参数
         * 其中可以通过注入可以安全获得参赛防止 xss或sql注入等
         *
         * @return 所有url中的参数集合
         * @see io.falcon.client.SafeParameter
         */
        @Override
        public Map<String, Collection<String>> queryStrings() {
            return request.queryStrings();
        }

        /**
         * 获得post中的参数，不包括文件
         * 其中可以通过注入可以安全获得参赛防止 xss或sql注入等
         *
         * @return 所有post中的参数集合
         * @see io.falcon.client.SafeParameter
         */
        @Override
        public Map<String, Collection<String>> forms() {
            return request.forms();
        }

        /**
         * 获得post中文件
         *
         * @param name 文件名
         * @return 对应文件
         */
        @Override
        public Upload getUpload(String name) {
            return request.getUpload(name);
        }

        /**
         * 获得post中所有文件
         *
         * @return 对应文件
         */
        @Override
        public Collection<Upload> getUploads() {
            return request.uploads();
        }

        /**
         * 获得用户ip
         *
         * @return 用户ip
         * @see ReverseProxy
         */
        @Override
        public InetAddress getAddress() {
            if (address != null)
                return address;

            //TODO:synchronized
            address = gerRemoteAddress();
            if (!reverseProxy.isCluster(address))
                return address;

            return reverseProxy.getRemoteAddress(request);
        }

        @Override
        public String ip() {
            return getRemoteAddress(request);
        }

        protected InetAddress gerRemoteAddress() {
            return InetAddresses.forString(request.getRemoteAddr());
        }

        public String getRemoteAddress(HttpServletRequest request) {
            String ip = request.getHeader("x-forwarded-for");
            if ((ip == null) || (ip.length() == 0) || (ip.equalsIgnoreCase("unknown")))
                ip = request.getHeader("Proxy-Client-IP");
            if ((ip == null) || (ip.length() == 0) || (ip.equalsIgnoreCase("unknown")))
                ip = request.getHeader("WL-Proxy-Client-IP");
            if ((ip == null) || (ip.length() == 0) || (ip.equalsIgnoreCase("unknown")))
                ip = request.getRemoteAddr();
            return ip;
        }
    }
}
