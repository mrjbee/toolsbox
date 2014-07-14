package org.monroe.team.toolsbox.transport.timer;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface TimerSchedule {

    public static final int DISABLE = -1;

    int value() default DISABLE;
    int startDelay() default 1000;

}
