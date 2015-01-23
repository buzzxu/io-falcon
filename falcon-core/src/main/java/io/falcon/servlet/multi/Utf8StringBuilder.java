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
package io.falcon.servlet.multi;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-16
 * Time: 下午5:35
 * To change this template use File | Settings | File Templates.
 */
public class Utf8StringBuilder extends Utf8Appendable{
    final StringBuilder _buffer;

    public Utf8StringBuilder()
    {
        super(new StringBuilder());
        _buffer=(StringBuilder)_appendable;
    }

    public Utf8StringBuilder(int capacity)
    {
        super(new StringBuilder(capacity));
        _buffer=(StringBuilder)_appendable;
    }

    @Override
    public int length()
    {
        return _buffer.length();
    }

    @Override
    public void reset()
    {
        super.reset();
        _buffer.setLength(0);
    }

    public StringBuilder getStringBuilder()
    {
        checkState();
        return _buffer;
    }

    @Override
    public String toString()
    {
        checkState();
        return _buffer.toString();
    }

}
