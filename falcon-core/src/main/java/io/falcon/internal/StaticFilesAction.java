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
package io.falcon.internal;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.falcon.Result;
import io.falcon.internal.result.StaticActionResult;
import io.falcon.route.Action;
import io.falcon.route.RouteBag;
import io.falcon.route.RouteResult;
import io.falcon.utils.Pair;
import io.falcon.utils.TouchTimer;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import java.io.File;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
/**
 * 对静态文件处理，把所有静态文件名保存在set中，如何精确匹配，表明当前请求就是静态文件
 * User: xux
 * Date: 13-10-17
 * Time: 下午6:26
 * To change this template use File | Settings | File Templates.
 */
@Singleton
public class StaticFilesAction implements Action {
    /**
     * 静态文件名set
     */
    private Set<String> staticFiles = Sets.newHashSet();

    /**
     * 不允许访问的文件或文件夹
     */
    private final Set<String> forbitPath = ImmutableSet.of("/WEB-INF");

    /**
     * 定时获取静态文件更新，但不需要另外的定时线程
     */
    private final TouchTimer timer;

    private final StaticActionResult.Factory staticFactory;

    @Inject
    public StaticFilesAction(
            ServletContext servletContext
            , StaticActionResult.Factory staticFactory
            , @SkySystem final Executor executor
    ) {

        this.staticFactory = staticFactory;

        final File staticResourcesFolder = new File(servletContext.getRealPath("/"));


        Runnable findFiles = new Runnable() {
            @Override
            public void run() {
                staticFiles = findFiles(staticResourcesFolder, staticFiles.size(), forbitPath);
            }
        };

        timer = TouchTimer
                .build(60 * 1000, findFiles, executor);

        timer.immediateRun();
    }

    @Override
    public double order() {
        return 100d;
    }

    @Override
    public RouteResult matchAndInvoke(RouteBag bag) {
        return RouteResult.invoked(match(bag));
    }

    private Result match(RouteBag bag) {

        String simplyPath = bag.getSimplyPath();

        if (!exist(simplyPath)) return Result.NULL;

        return staticFactory.create(simplyPath);

    }

    private boolean exist(String url) {
        timer.touch();
        return staticFiles.contains(url);
    }

    Set<String> findFiles(File directory, int cap, Set<String> forbitPath) {

        Set<String> staticFiles = new HashSet<String>(cap);

        Deque<Pair<File, String>> dirs = Lists.newLinkedList();

        dirs.add(Pair.build(directory, "/"));

        while (dirs.size() > 0) {
            Pair<File, String> pop = dirs.pop();

            File[] files = pop.getKey().listFiles();

            if (files == null)
                continue;

            for (File file : files) {
                String name = pop.getValue() + file.getName();

                if (forbitPath.contains(name))
                    continue;

                if (file.isDirectory()) {
                    dirs.push(Pair.build(file
                            , name + '/'));
                    continue;
                }

                staticFiles.add(name);
            }
        }

        return staticFiles;
    }
}
