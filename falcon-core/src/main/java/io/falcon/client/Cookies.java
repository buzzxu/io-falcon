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

import com.google.inject.ImplementedBy;
import io.falcon.RequestCycle;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * 封装cookie管理
 * User: xux
 * Date: 13-10-16
 * Time: 下午4:43
 * To change this template use File | Settings | File Templates.
 */
@ImplementedBy(Cookies.DefaultCookies.class)
public interface Cookies {
    /**
     * 通过名字和值新增一个cookie
     * @param name cookie名称，必须是ascii码和数字，其他会抛错 RFC2019
     * @param value cookie的值
     */
    void add(String name, String value);

    /**
     * 通过名字，值和有效期新增一个cookie
     * @param name cookie名称，必须是ascii码和数字，其他会抛错 RFC2019
     * @param value cookie的值
     * @param cookieMaxAge cookie的有效期
     */
    void add(String name, String value, int cookieMaxAge);

    /**
     * 新增一个cookie
     * @param cookie
     */
    void add(Cookie cookie);

    /**
     * 通过名字获得cookie的值，如果名字不存在，返回null
     * @param name
     * @return
     */
    String get(String name);

    /**
     * 根据名字获得对应Cookie，如果名字不存在，返回null
     * @param name
     * @return
     */
    Cookie getCookie(String name);

    /**
     * 获得所有的cookie数组，如果没有cookie数组，返回一个长度为0的数组，这样不用判断null。
     * @return 对应的cookie数组，不可能是null。
     */
    Cookie[] getCookies();

    /**
     * 移除一个对应名称的cookie
     * @param name cookie名
     */
    void remove(String name);

    public static class DefaultCookies implements Cookies {

        private final HttpServletResponse response;
        private final HttpServletRequest request;

        private Cookie[] cookies = null;
        private static final Cookie[] emptyCookies = new Cookie[0];

        @Inject
        public DefaultCookies(RequestCycle cycle) {
            response = cycle.getResponse();
            request = cycle.getRequest();

        }
        /**
         * 通过名字和值新增一个cookie
         *
         * @param name  cookie名称，必须是ascii码和数字，其他会抛错 RFC2019
         * @param value cookie的值
         */
        @Override
        public void add(String name, String value) {
            Cookie cookie = new Cookie(name, value);
            // 设置路径（默认）
            cookie.setPath("/");
            // 把cookie放入响应中
            add(cookie);
        }

        /**
         * 通过名字，值和有效期新增一个cookie
         *
         * @param name         cookie名称，必须是ascii码和数字，其他会抛错 RFC2019
         * @param value        cookie的值
         * @param cookieMaxAge cookie的有效期
         */
        @Override
        public void add(String name, String value, int cookieMaxAge) {
            Cookie cookie = new Cookie(name, value);
            // 设置有效日期
            cookie.setMaxAge(cookieMaxAge);
            // 设置路径（默认）
            cookie.setPath("/");

            add(cookie);
        }

        /**
         * 新增一个cookie
         *
         * @param cookie
         */
        @Override
        public void add(Cookie cookie) {
            response.addCookie(cookie);
        }

        /**
         * 通过名字获得cookie的值，如果名字不存在，返回null
         *
         * @param name
         * @return
         */
        @Override
        public String get(String name) {
            Cookie cookie = getCookie(name);
            return cookie == null?  null : cookie.getValue();
        }

        /**
         * 根据名字获得对应Cookie，如果名字不存在，返回null
         *
         * @param name
         * @return
         */
        @Override
        public Cookie getCookie(String name) {
            Cookie[] cookies = getCookies();

            for(Cookie cookie : cookies){
                if(name.equalsIgnoreCase(cookie.getName()))
                    return cookie;
            }

            return null;
        }

        /**
         * 获得所有的cookie数组，如果没有cookie数组，返回一个长度为0的数组，这样不用判断null。
         *
         * @return 对应的cookie数组，不可能是null。
         */
        @Override
        public Cookie[] getCookies() {
            if (cookies != null)
                return cookies;

            cookies = request.getCookies();
            if (cookies == null)
                cookies = emptyCookies;

            return cookies;
        }

        /**
         * 移除一个对应名称的cookie
         *
         * @param name cookie名
         */
        @Override
        public void remove(String name) {
            Cookie cookie = getCookie(name);

            if (cookie == null) return;

            // 销毁
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
    }
}
