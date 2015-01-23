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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-16
 * Time: 下午5:19
 * To change this template use File | Settings | File Templates.
 */
public class ReadLineInputStream extends BufferedInputStream{
    boolean _seenCRLF;
    boolean _skipLF;

    public ReadLineInputStream(InputStream in)
    {
        super(in);
    }

    public ReadLineInputStream(InputStream in, int size)
    {
        super(in,size);
    }

    public String readLine() throws IOException
    {
        mark(buf.length);

        while (true)
        {
            int b=super.read();
            if (b==-1)
            {
                int m=markpos;
                markpos=-1;
                if (pos>m)
                    return new String(buf,m,pos-m,StringUtil.__UTF8_CHARSET);

                return null;
            }

            if (b=='\r')
            {
                int p=pos;

                // if we have seen CRLF before, hungrily consume LF
                if (_seenCRLF && pos<count)
                {
                    if (buf[pos]=='\n')
                        pos+=1;
                }
                else
                    _skipLF=true;
                int m=markpos;
                markpos=-1;
                return new String(buf,m,p-m-1,StringUtil.__UTF8_CHARSET);
            }

            if (b=='\n')
            {
                if (_skipLF)
                {
                    _skipLF=false;
                    _seenCRLF=true;
                    markpos++;
                    continue;
                }
                int m=markpos;
                markpos=-1;
                return new String(buf,m,pos-m-1,StringUtil.__UTF8_CHARSET);
            }
        }
    }

    @Override
    public synchronized int read() throws IOException
    {
        int b = super.read();
        if (_skipLF)
        {
            _skipLF=false;
            if (_seenCRLF && b=='\n')
                b=super.read();
        }
        return b;
    }

    @Override
    public synchronized int read(byte[] buf, int off, int len) throws IOException
    {
        if (_skipLF && len>0)
        {
            _skipLF=false;
            if (_seenCRLF)
            {
                int b = super.read();
                if (b==-1)
                    return -1;

                if (b!='\n')
                {
                    buf[off]=(byte)(0xff&b);
                    return 1+super.read(buf,off+1,len-1);
                }
            }
        }

        return super.read(buf,off,len);
    }
}
