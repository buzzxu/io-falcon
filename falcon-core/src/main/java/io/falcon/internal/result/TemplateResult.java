package io.falcon.internal.result;

import io.falcon.Falcon;
import io.falcon.RequestCycle;
import io.falcon.Result;

/**
 * 模版视图
 * User: xux
 * Date: 13-11-18
 * Time: 下午5:15
 * To change this template use File | Settings | File Templates.
 */
public class TemplateResult implements Result {

    private final String view;
    private final Falcon Falcon;
//    public TemplateResult(String view){
//        this.view = view;
//    }
    public TemplateResult(Falcon Falcon,String view){
        this.view = view;
        this.Falcon = Falcon;
    }
    @Override
    public void render(RequestCycle cycle) throws Throwable {
        Falcon.getViewFactory().create(view).render(cycle);
    }
}
