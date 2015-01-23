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

import com.google.inject.ImplementedBy;
import io.falcon.internal.DefaultModel;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-16
 * Time: 下午4:36
 * To change this template use File | Settings | File Templates.
 */
@ImplementedBy(DefaultModel.class)
public interface Model {

    /**
     * 增加一个属性
     * @param attributeName 属性名称
     * @param attributeValue 属性值
     */
    Model add(String attributeName, Object attributeValue);

    /**
     * 根据属性名得到属性值
     * @param attributeName 属性名称
     * @return 对应的属性值
     */
    Object get(String attributeName);

    /**
     * Return the model map. Never returns <code>null</code>.
     * To be called by application code for modifying the model.
     */
    Map<String, Object> getModel();

    /**
     * 批量增加属性
     * @param attributes 属性map
     */
    Model addAll(Map<String, ?> attributes);

    /**
     * 判断是否包含属性名
     * @param attributeName 需要查找的属性
     * @return 是否包含
     */
    boolean contains(String attributeName);
}
