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
package io.falcon.controller;

import com.google.inject.ImplementedBy;
import io.falcon.Result;
import io.falcon.internal.VelocityViewFactory;

/**
 * 提供View工厂，默认采用velocity模板
 * User: xux
 * Date: 13-10-17
 * Time: 下午9:30
 * To change this template use File | Settings | File Templates.
 */
//@ImplementedBy(VelocityViewFactory.class)
public interface ViewFactory {
    Result create(String viewName);
}