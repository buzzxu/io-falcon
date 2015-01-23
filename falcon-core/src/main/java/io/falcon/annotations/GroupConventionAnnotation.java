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
package io.falcon.annotations;

import com.google.inject.Module;
import io.falcon.convention.EmptyModule;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 这是一个组织级的约定规则
 * User: xux
 * Date: 13-10-17
 * Time: 下午3:27
 * To change this template use File | Settings | File Templates.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GroupConventionAnnotation {
    /**
     * 约定组织中所有项目必须实现的类,
     * @see io.falcon.convention.GroupConvention.ProjectConfig
     *
     * 该类实现项目的Id用于项目的唯一编号，并便于管理
     * @see io.falcon.convention.GroupConvention.ProjectConfig#id()
     *
     * 同时实现module，
     * @see io.falcon.convention.GroupConvention.ProjectConfig#module()
     * 用于项目的Guice注入配置,优先级低于组织的module并可能会被组织级的module覆盖
     *
     */
    String projectConventionClass() default "io.falcon.ProjectConfigBinder";

    /**
     * group级的注入Module
     * 可以覆盖项目级的module，保证组织的策略实施
     *
     */
    Class<? extends Module> groupModule() default EmptyModule.class;

    /**
     * 配置文件夹位置
     *
     * @return 配置文件夹的根目录
     */
    String groupConfigFolder() default "/opt/Falcon";

    String groupLogFolder() default "{groupConfigFolder}/log";


    /**
     * group包的前缀，Falcon将只扫描该前缀下的类
     * @return group包的前缀
     */
    String groupPackagesPrefix() default "io.falcon";

    /**
     * controller的类名强制检查约定
     * 只有符合匹配条件的controller才能被Falcon管理，保证组织级代码风格一致
     *
     */
    String controllerPattern() default ".*\\.controllers\\..*Controller";

}
