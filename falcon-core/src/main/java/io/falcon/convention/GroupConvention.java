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
package io.falcon.convention;

import com.google.inject.Module;
import io.falcon.Controller;

import java.io.File;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-17
 * Time: 下午2:02
 * To change this template use File | Settings | File Templates.
 */
public interface GroupConvention {


    /**
     * 用于自定义的组织的策略的类
     * 如果需要自己实现组织级的策略，需要实现这个类
     * 可以在该类上配置注解
     * @see io.falcon.annotations.GroupConventionAnnotation
     * 也可以实现接口
     * @see io.falcon.convention.GroupConvention
     *
     * <br/>
     *
     * 接口的优先级比annotation高
     *
     * <br/>
     *
     * 一个组织应该只提供一个实现类的jar包给其他项目约束。
     *
     */
    public final static String annotatedGroupConventionBinder = "io.falcon.GroupConventionBinder";
    /**
     * 组织级配置
     * @return 组织级配置
     */
    GroupConfig group();

    /**
     * 项目级配置
     * @return 项目级配置
     */
    ProjectConfig currentProject();

    interface GroupConfig{
        /**
         * 公司级的配置文件路径，项目根据各自id对应相应文件夹放置配置文件
         * @return 公司级的配置文件路径
         */
        File configFolder();

        /**
         * 公司级的日志文件路径，项目根据各自id对应相应文件夹写日志文件
         * @return 公司级的日志文件路径
         */
        File logFolder();

        /**
         * 组织级的Guice注入module配置，
         * 在项目级的module后面实现，保证组织级的策略实现
         *
         * @return 组织级的Guice注入module配置
         */
        Module module();
    }

    /**
     * 项目级配置
     */
    interface ProjectConfig {
        /**
         * 项目的Id，用于组织和运维进行统一管理
         */
        String id();

        /**
         * 项目所有的Controller集合
         *
         */
        Set<Class<? extends Controller>> controllerClasses();

        /**
         * 项目级的注入配置
         *
         */
        Module module();
    }
}
