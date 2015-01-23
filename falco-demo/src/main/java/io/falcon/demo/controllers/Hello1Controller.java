package io.falcon.demo.controllers;

import io.falcon.annotations.Controller;
import io.falcon.annotations.POST;
import io.falcon.annotations.Path;

/**
 * Created by xux on 15-1-23.
 */

@Path("/a")
@Controller
public class Hello1Controller {


    @Path("/index")
    @POST
    public Object hello(){
        return "你好";
    }
}
