package com.sos.JSHelper.Annotations;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface JSCsvField {

    String name();

    String description();

    String xmltagname() default "";

    int size() default -1;

    int pos();

    String type() default "string";

    String defaultvalue() default "";

    boolean isReadOnly() default true;

    boolean isFormField() default false;

    boolean isField4Rename() default false;
}
