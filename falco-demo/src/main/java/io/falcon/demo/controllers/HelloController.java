package io.falcon.demo.controllers;

import io.falcon.Controller;

/**
 * Created by xux on 15-1-22.
 */
public class HelloController implements Controller {
    @Override
    public void init() {
        System.out.println("hello" +this);
    }
}
