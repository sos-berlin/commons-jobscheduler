package com.sos.JSHelper.Annotations;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** @author KB */
@Target({ FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface JSOptionDefinition {

    String name();

    String description();

    String key() default "";

    String xmltagname() default "";

    int size() default -1;

    boolean mandatory() default false;

    String type() default "JSOptionString";

    String value() default "";

    String defaultvalue() default "";

}