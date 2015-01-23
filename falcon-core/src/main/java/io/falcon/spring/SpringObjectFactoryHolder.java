/**
 * Copyright (C) 2013 Shforce, Inc.
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
package io.falcon.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: xiang
 * Date: 13-5-7
 * Time: 下午12:50
 * To change this template use File | Settings | File Templates.
 */
public class SpringObjectFactoryHolder implements BeanFactoryAware,Serializable {

    private static final long serialVersionUID = -1422803901187713709L;
    private static BeanFactory beanFactory;
    public static BeanFactory getBeanFactory(){
        return beanFactory;
    }
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
