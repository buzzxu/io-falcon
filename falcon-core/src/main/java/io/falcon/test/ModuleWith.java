package io.falcon.test;

import com.google.inject.Module;

import java.lang.annotation.*;

/**
 * Created by xux on 14-1-9.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface ModuleWith {
    Class<? extends Module>[] value();
}
