package io.falcon.test;

import java.lang.annotation.*;

/**
 * Created by Administrator on 14-1-9.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ContextConfiguration {
    String spring() default "";
    String[] properties() default {};
}
