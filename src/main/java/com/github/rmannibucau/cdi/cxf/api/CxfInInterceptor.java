package com.github.rmannibucau.cdi.cxf.api;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({ FIELD, METHOD, PARAMETER })
public @interface CxfInInterceptor {
    @Nonbinding
    String classname() default "";

    @Nonbinding
    String factoryMethod() default "";

    @Nonbinding
    Class<?> clazz() default NotSpecified.class;

    @Nonbinding
    String propertyFile() default "";

    @Nonbinding
    Property[] properties() default {};

    @Nonbinding
    String prefix() default "";
}
