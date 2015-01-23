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
package io.falcon.internal.action;

import com.google.common.collect.Maps;
import io.falcon.Result;
import io.falcon.interceptor.PostInterceptor;
import io.falcon.interceptor.PreInterceptor;
import io.falcon.internal.result.ExceptionResult;
import io.falcon.internal.result.JSONResult;
import io.falcon.route.Action;
import io.falcon.route.RouteBag;
import io.falcon.route.RouteResult;
import io.falcon.utils.AntPathMatcher;
import io.falcon.utils.PathMatcher;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-17
 * Time: 下午8:59
 * To change this template use File | Settings | File Templates.
 */
public class MethodAction implements Action {
    public static MethodAction create(ActionInfo actionInfo) {
        return new MethodAction(actionInfo);
    }

    private ActionInfo actionInfo;

    private final double order;

    private PathMatcher pathMatcher = new AntPathMatcher();



    private MethodAction(ActionInfo actionInfo) {
        this.actionInfo = actionInfo;

        order = actionInfo.getOrder()
                + (10000.0d - actionInfo.getPathPattern().length())/100000.0d
                + (actionInfo.isPattern() ? 0.5d : 0d);
    }

    @Override
    public double order() {
        return order;
    }

    @Override
    public RouteResult matchAndInvoke(RouteBag bag) {

        if (!actionInfo.matchHttpMethod(bag))
            return RouteResult.unMatch();

        Map<String, String> uriTemplateVariables = Maps.newHashMap();

        boolean match = actionInfo.match(bag, uriTemplateVariables);
        if (!match)
            return RouteResult.unMatch();

        // PreIntercept 拦截器
        for(PreInterceptor preInterceptor : actionInfo.getPreInterceptors()) {
            Result actionResult = preInterceptor.invoke(bag.getCycle());
            if (Result.NULL != actionResult)
                return RouteResult.invoked(actionResult);
        }

        Object resultObj  = null;
        try {
            resultObj = actionInfo.invoke(uriTemplateVariables);
        } catch (Throwable throwable) {
            resultObj = new ExceptionResult(throwable);
        }
        Result actionResult;
        if(resultObj instanceof Result){
            actionResult = Result.class.cast(resultObj);
        }else{
            actionResult = new JSONResult(resultObj);
        }
        // PostIntercept
        for(PostInterceptor postInterceptor : actionInfo.getPostInterceptors()) {
            actionResult = postInterceptor.invoke(bag.getCycle(), actionResult);
        }

        return RouteResult.invoked(actionResult);
    }
}
