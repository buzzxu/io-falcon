package io.falcon.internal.result;

import io.falcon.Falcon;
import io.falcon.RequestCycle;
import io.falcon.Result;
import io.falcon.internal.FreeMarkerViewFactory;

/**
 * Freemarder模版
 * User: xux
 * Date: 13-11-10
 * Time: 下午1:37
 * To change this template use File | Settings | File Templates.
 */
public class FreeMarkerResult implements Result {

    private String view;

    public FreeMarkerResult(String view){
        this.view = view;
    }

    @Override
    public void render(RequestCycle cycle) throws Throwable {
        Falcon.instance.instance(FreeMarkerViewFactory.class).create(view).render(cycle);
    }
}
