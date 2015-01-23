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
import com.google.common.collect.Maps;
import com.google.inject.*;
import com.google.inject.internal.CircularDependencyProxy;
import com.google.inject.internal.LinkedBindingImpl;
import com.google.inject.servlet.RequestParameters;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.ExposedBinding;
import io.falcon.Falcon;
import io.falcon.ScopingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-21
 * Time: 下午11:19
 * To change this template use File | Settings | File Templates.
 */
public class SkyScopes {


    enum NullObject { INSTANCE }
    //线程范围
    public static final Scope THREAD = new Scope() {
        private ThreadLocal<Map<String, Provider>> LocalMap = new ThreadLocal<Map<String, Provider>>() {
            @Override
            protected Map<String, Provider> initialValue() {
                return super.initialValue();    //To change body of overridden methods use File | Settings | File Templates.
            }
        };

        @Override
        public <T> Provider<T> scope(Key<T> key, final Provider<T> creator) {
            final String name = key.toString();
            return new Provider() {
                @Override
                public Object get() {
                    Map map = (Map) LocalMap.get();
                    if (map == null) {
                        map = Maps.newConcurrentMap();
                        LocalMap.set(map);
                    }
                    synchronized (map){
                        Object ret = map.get(name);
                        if (ret == null) {
                            ret = creator.get();
                            if (!isCircularProxy(ret))
                                map.put(name, ret);
                        }
                        return ret;
                    }
                }

                @Override
                public String toString() {
                    return String.format("%s[%s]", creator, THREAD);
                }
            };
        }
    };

    //

    private static final ThreadLocal<Map<String, Object>> requestScopeContext
            = new ThreadLocal<Map<String, Object>>();
    public static final Scope REQUEST = new Scope() {


        @Override
        public <T> Provider<T> scope(Key<T> key,final Provider<T> creator) {
            final String name = key.toString();
            return new Provider<T>() {
                @Override
                public T get() {

                    HttpServletRequest request = Falcon.instance.currentRequest();
                    if(request == null ){
                        Map<String, Object> scopeMap = requestScopeContext.get();
                        if (null != scopeMap) {
                            @SuppressWarnings("unchecked")
                            T t = (T) scopeMap.get(name);

                            // Accounts for @Nullable providers.
                            if (NullObject.INSTANCE == t) {
                                return null;
                            }

                            if (t == null) {
                                t = creator.get();
                                // Store a sentinel for provider-given null values.
                                if (!isCircularProxy(t))
                                    scopeMap.put(name, t != null ? t : NullObject.INSTANCE);
                            }

                            return t;
                        } // else: fall into normal HTTP request scope and out of scope
                        // exception is thrown.
                    }
                    synchronized (request) {
                        Object obj = request.getAttribute(name);
                        if (NullObject.INSTANCE == obj) {
                            return null;
                        }
                        @SuppressWarnings("unchecked")
                        T t = (T) obj;
                        if (t == null) {
                            t = creator.get();
                            if (!isCircularProxy(t))
                                request.setAttribute(name, (t != null) ? t : NullObject.INSTANCE);
                        }
                        return t;
                    }

                }

                @Override
                public String toString() {
                    return String.format("%s[%s]", creator, REQUEST);
                }
            };

        }

        @Override
        public String toString() {
            return "FlyScopes.REQUEST";
        }
    };

    public static final Scope SESSION = new Scope() {
        @Override
        public <T> Provider<T> scope(Key<T> key, final Provider<T> creator) {
            final String name = key.toString();
            return new Provider<T>() {
                @Override
                public T get() {
                    HttpSession session = Falcon.instance.currentRequest().getSession(true);
                    synchronized (session) {
                        Object obj = session.getAttribute(name);
                        if (NullObject.INSTANCE == obj) {
                            return null;
                        }
                        @SuppressWarnings("unchecked")
                        T t = (T) obj;
                        if (t == null) {
                            t = creator.get();
                            if (!isCircularProxy(t)) {
                                session.setAttribute(name, (t != null) ? t : NullObject.INSTANCE);
                            }
                        }
                        return t;
                    }
                }

                @Override
                public String toString() {
                    return String.format("%s[%s]", creator, SESSION);
                }
            };
        }

        @Override
        public String toString() {
            return "FlyScopes.SESSION";
        }
    };

    public static boolean isCircularProxy(Object object) {
        return object instanceof CircularDependencyProxy;
    }
    public static boolean isRequestScoped(Binding<?> binding) {
        return isScoped(binding,REQUEST, RequestScoped.class);
    }


    /**
     * Copy Guice 4.0
     * @see com.google.inject.Scopes
     * @param binding
     * @param scope
     * @param scopeAnnotation
     * @return
     */
    public static boolean isScoped(Binding<?> binding, final Scope scope,
                                   final Class<? extends Annotation> scopeAnnotation) {
        do {
            boolean matches = binding.acceptScopingVisitor(new BindingScopingVisitor<Boolean>() {
                public Boolean visitNoScoping() {
                    return false;
                }

                public Boolean visitScopeAnnotation(Class<? extends Annotation> visitedAnnotation) {
                    return visitedAnnotation == scopeAnnotation;
                }

                public Boolean visitScope(Scope visitedScope) {
                    return visitedScope == scope;
                }

                public Boolean visitEagerSingleton() {
                    return false;
                }
            });

            if (matches) {
                return true;
            }

            if (binding instanceof LinkedBindingImpl) {
                LinkedBindingImpl<?> linkedBinding = (LinkedBindingImpl) binding;
                Injector injector = linkedBinding.getInjector();
                if (injector != null) {
                    binding = injector.getBinding(linkedBinding.getLinkedKey());
                    continue;
                }
            } else if(binding instanceof ExposedBinding) {
                ExposedBinding<?> exposedBinding = (ExposedBinding)binding;
                Injector injector = exposedBinding.getPrivateElements().getInjector();
                if (injector != null) {
                    binding = injector.getBinding(exposedBinding.getKey());
                    continue;
                }
            }

            return false;
        } while (true);
    }
}
