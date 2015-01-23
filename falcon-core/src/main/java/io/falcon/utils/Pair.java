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

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-16
 * Time: 下午4:22
 * To change this template use File | Settings | File Templates.
 */
public class Pair<K, V> {

    public static <K, V> Pair<K, V> build(K k, V v) {
        return new Pair<>(k, v);
    }

    private final K key;
    private final V value;

    public Pair(K k, V v) {
        this.key = k;
        this.value = v;
    }

    public K getKey() {
        return this.key;
    }

    public V getValue() {
        return this.value;
    }

    public String toString() {
        return "Pair " + key + ": " + value;

    }
}
