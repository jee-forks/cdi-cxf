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

@Qualifier
@Documented
@Retention(RUNTIME)
@Target({ FIELD, METHOD, PARAMETER })
public @interface CxfJaxWSClient {
    @Nonbinding
    boolean lazy() default false;

    @Nonbinding
    String wsdl() default "";

    @Nonbinding
    String qname() default "";

    @Nonbinding
    String address() default "";

    @Nonbinding
    String username() default "";

    @Nonbinding
    String password() default "";

    @Nonbinding
    String properties() default "";
}
