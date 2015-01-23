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

import com.google.common.collect.*;
import io.falcon.Controller;
import io.falcon.Falcon;
import io.falcon.RequestCycle;
import io.falcon.Result;
import io.falcon.annotations.StaticActionAnnotation;
import io.falcon.internal.action.ActionInfo;
import io.falcon.internal.action.MethodAction;
import io.falcon.route.Action;
import io.falcon.route.RouteBag;
import io.falcon.route.RouteResult;
import io.falcon.route.Router;

import com.google.common.base.Function;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;
/**
 * Created with IntelliJ IDEA.
 * User: xux
 * Date: 13-10-17
 * Time: 下午9:02
 * To change this template use File | Settings | File Templates.
 */
@Singleton
public class DefaultRouter implements Router {

    private final Falcon fly;
    private final List<Action> actions;

    @Inject
    public DefaultRouter(Falcon fly,@SkySystem Set<Class<? extends Controller>> controllerClasses,
                         @SkySystem Set<Class<?>> controllers, @StaticActionAnnotation Action staticAction) {

        this.fly = fly;

        fly.logger().info("initializing a {}(implements Router)", this.getClass());

        this.actions = buildActions(fly, controllerClasses,controllers, staticAction);

        fly.logger().info("{}(implements Router) constructed.", this.getClass());
    }

    @Override
    public Result route(RequestCycle cycle) {

        RouteBag bag = RouteBag.create(cycle);

        for(Action action : actions) {
            RouteResult routeResult = action.matchAndInvoke(bag);
            if (routeResult.isSuccess())
                return routeResult.getResult();
        }

        return Result.NULL;
    }

    /**
     * 构建方法操作对象
     * @param fly
     * @param controllerClasses
     * @param staticAction
     * @return
     */
    List<Action> buildActions(Falcon fly, Set<Class<? extends Controller>> controllerClasses, Set<Class<?>> controllers,Action staticAction) {
//        Set<Class<? extends Controller>> singletonClass = Sets.newHashSet();
//        Set<Class<?>> annotationOrSpringClazz = Sets.newHashSet();
//        for (Class<?> clazz : controllerClasses){
//            if(Controller.class.isAssignableFrom(clazz)){
//                singletonClass.add(clazz.asSubclass(Controller.class));
////                singletonClass.add((Class<? extends Controller>)clazz);
//            }else{
//
//                annotationOrSpringClazz.add(clazz);
//            }
//        }
        return ImmutableList.<Action>builder()
                .addAll(buildActions(getControllerSingletonInstances(fly, controllerClasses), staticAction))
                .addAll(buildActions(controllers))    //添加注解或Spring的类
                .build();

    }

    private Set<Controller> getControllerSingletonInstances(final Falcon fly, Set<Class<? extends Controller>> controllerClasses) {

        Iterable<Controller> sets = Iterables.transform(controllerClasses, new Function<Class<? extends Controller>,
                Controller>() {
            @Override
            public Controller apply(Class<? extends Controller> clazz) {

                // instance a controller
                Controller controller = fly.instance(clazz);
                // initialize the controller.
                controller.init();

                return controller;
            }
        });

        return ImmutableSet.copyOf(sets);
    }
    //TODO
    List<Action> buildActions(Set<Class<?>> controllers){
        List<Action> actions = Lists.newArrayList();
        for (Class<?> controller : controllers){
            ControllerInfo controllerInfo = new ControllerInfo2(controller);
            List<ActionInfo> subActions = controllerInfo.analyze();
            for(ActionInfo newAction : subActions){
                merge(actions, MethodAction.create(newAction));
            }
        }
        return actions;
    }

    //TODO:static files actions.
    List<Action> buildActions(Set<Controller> controllers, Action staticAction) {

        List<Action> actions = Lists.newArrayList();
        actions.add(staticAction);

        for (Controller controller : controllers) {
            ControllerInfo controllerInfo = new ControllerInfo1(controller);
            List<ActionInfo> subActions = controllerInfo.analyze();

            for(ActionInfo newAction : subActions)
                merge(actions, MethodAction.create(newAction));

        }

        return ImmutableList.copyOf(actions);
    }


    void merge(List<Action> actions, Action newAction) {

        for (int index = 0; index < actions.size(); index++) {
            Action action = actions.get(index);
            if(action.order() > newAction.order()) {
                actions.add(index, newAction);
                return;
            }
        }

        actions.add(newAction);
    }
}
