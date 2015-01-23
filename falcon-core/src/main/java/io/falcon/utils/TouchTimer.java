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
package io.falcon.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 一个不需要定时线程的定时器，减少线程量
 * User: xux
 * Date: 13-10-16
 * Time: 下午4:19
 * To change this template use File | Settings | File Templates.
 */
public class TouchTimer {
    private final long interval;

    private final Runnable run;

    private final Executor executor;

    private volatile long lastTime = 0;
    private AtomicBoolean isRun = new AtomicBoolean(false);

    public static TouchTimer build(long interval, Runnable run, Executor executor) {
        return new TouchTimer(interval, run, executor);
    }

    public TouchTimer(long interval, Runnable run, Executor executor) {
        this.interval = interval;
        this.run = run;
        this.executor = executor;
    }

    public void touch() {

        long time = System.currentTimeMillis();
        if (isRun.get())
            return;

        if (time - lastTime < interval)
            return;

        execute();

        lastTime = time;

    }

    public void execute() {

        if(!isRun.compareAndSet(false, true))
            return;

        executor.execute(new Runnable() {
            @Override
            public void run() {
                immediateRun();
            }
        });

    }

    public void immediateRun() {
        try {
            if (isRun.get())
                return;

            executor.execute(run);
        } finally {
            lastTime = System.currentTimeMillis();
            isRun.set(false);
        }
    }
}
