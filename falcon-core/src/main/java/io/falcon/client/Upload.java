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

import javax.servlet.http.Part;

/**
 * 上传文件的封装，继承与servlet3.0的Upload 并增加了获得文件名的方法
 * User: xux
 * Date: 13-10-16
 * Time: 下午5:01
 * To change this template use File | Settings | File Templates.
 */
public interface Upload extends Part {
    String getFileName();
}
