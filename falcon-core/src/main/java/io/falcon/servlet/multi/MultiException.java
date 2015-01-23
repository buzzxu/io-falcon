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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Wraps multiple exceptions.
 *
 * Allows multiple exceptions to be thrown as a single exception.
 */
@SuppressWarnings("serial")
/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-16
 * Time: 下午5:23
 * To change this template use File | Settings | File Templates.
 */
public class MultiException extends RuntimeException{
    private List<Throwable> nested;

    /* ------------------------------------------------------------ */
    public MultiException()
    {
        super("Multiple exceptions");
    }

    /* ------------------------------------------------------------ */
    public void add(Throwable e)
    {
        if(nested == null)
        {
            nested = new ArrayList();
        }

        if (e instanceof MultiException)
        {
            MultiException me = (MultiException)e;
            nested.addAll(me.nested);
        }
        else
            nested.add(e);
    }

    /* ------------------------------------------------------------ */
    public int size()
    {
        return (nested ==null)?0:nested.size();
    }

    /* ------------------------------------------------------------ */
    public List<Throwable> getThrowables()
    {
        if(nested == null) {
            return Collections.emptyList();
        }
        return nested;
    }

    /* ------------------------------------------------------------ */
    public Throwable getThrowable(int i)
    {
        return nested.get(i);
    }

    /* ------------------------------------------------------------ */
    /** Throw a multiexception.
     * If this multi exception is empty then no action is taken. If it
     * contains a single exception that is thrown, otherwise the this
     * multi exception is thrown.
     * @exception Exception
     */
    public void ifExceptionThrow()
            throws Exception
    {
        if(nested == null)
            return;

        switch (nested.size())
        {
            case 0:
                break;
            case 1:
                Throwable th=nested.get(0);
                if (th instanceof Error)
                    throw (Error)th;
                if (th instanceof Exception)
                    throw (Exception)th;
            default:
                throw this;
        }
    }

    /* ------------------------------------------------------------ */
    /** Throw a Runtime exception.
     * If this multi exception is empty then no action is taken. If it
     * contains a single error or runtime exception that is thrown, otherwise the this
     * multi exception is thrown, wrapped in a runtime exception.
     * @exception Error If this exception contains exactly 1 {@link Error}
     * @exception RuntimeException If this exception contains 1 {@link Throwable} but it is not an error,
     *                             or it contains more than 1 {@link Throwable} of any type.
     */
    public void ifExceptionThrowRuntime()
            throws Error
    {
        if(nested == null)
            return;

        switch (nested.size())
        {
            case 0:
                break;
            case 1:
                Throwable th=nested.get(0);
                if (th instanceof Error)
                    throw (Error)th;
                else if (th instanceof RuntimeException)
                    throw (RuntimeException)th;
                else
                    throw new RuntimeException(th);
            default:
                throw new RuntimeException(this);
        }
    }

    /* ------------------------------------------------------------ */
    /** Throw a multiexception.
     * If this multi exception is empty then no action is taken. If it
     * contains a any exceptions then this
     * multi exception is thrown.
     */
    public void ifExceptionThrowMulti()
            throws MultiException
    {
        if(nested == null)
            return;

        if (nested.size()>0)
            throw this;
    }

    /* ------------------------------------------------------------ */
    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        str.append(MultiException.class.getSimpleName());
        if((nested == null) || (nested.size()<=0)) {
            str.append("[]");
        } else {
            str.append(nested);
        }
        return str.toString();
    }

    /* ------------------------------------------------------------ */
    @Override
    public void printStackTrace()
    {
        super.printStackTrace();
        if(nested != null) {
            for(Throwable t: nested) {
                t.printStackTrace();
            }
        }
    }


    /* ------------------------------------------------------------------------------- */
    /**
     * @see Throwable#printStackTrace(java.io.PrintStream)
     */
    @Override
    public void printStackTrace(PrintStream out)
    {
        super.printStackTrace(out);
        if(nested != null) {
            for(Throwable t: nested) {
                t.printStackTrace(out);
            }
        }
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * @see Throwable#printStackTrace(java.io.PrintWriter)
     */
    @Override
    public void printStackTrace(PrintWriter out)
    {
        super.printStackTrace(out);
        if(nested != null) {
            for(Throwable t: nested) {
                t.printStackTrace(out);
            }
        }
    }

}
