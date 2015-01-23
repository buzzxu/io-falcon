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
package io.falcon.lifecycle;

import com.google.common.collect.Queues;
import io.falcon.RequestCycle;

import java.util.Deque;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-16
 * Time: 下午4:23
 * To change this template use File | Settings | File Templates.
 */
public class Lifecycle {
    /**
     * 创建时间
     */
    private final long birthTime = System.currentTimeMillis();

    /**
     * 析构函数栈
     */
    private final Deque<Destruct> destructs = Queues.newArrayDeque();

    /**
     * push 一个当前请求的析构处理
     * @param destruct 析构处理
     */
    public void pushDestruct(Destruct destruct) {
        destructs.push(destruct);
    }

    /**
     * pop 出一个当前析构处理
     * @return 栈上第一个析构处理 <tt>null</tt> 栈上没数据。
     */
    public Destruct pollDestruct() {
        return destructs.poll();
    }

    public long getBirthTime() {
        return birthTime;
    }

    /**
     * 当前调用
     */
    public interface Destruct {
        void clean(RequestCycle cycle);
    }
}
