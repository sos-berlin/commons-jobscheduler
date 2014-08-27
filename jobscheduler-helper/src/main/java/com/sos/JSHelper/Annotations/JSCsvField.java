package com.sos.JSHelper.Annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( { FIELD , METHOD } )
@Retention( RetentionPolicy.RUNTIME )
public @interface JSCsvField  {

	String name() default "";

	String description() default "";

	String xmltagname() default "";

	int size() default -1;

	int pos() default -1;

	String type() default "string";

	String defaultvalue() default "";

	boolean isReadOnly() default true;

	boolean isFormField() default false;
	
	boolean isField4Rename() default false;
}
