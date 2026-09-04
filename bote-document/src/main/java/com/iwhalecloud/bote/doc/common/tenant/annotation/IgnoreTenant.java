package com.iwhalecloud.bote.doc.common.tenant.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 忽略租户标识注解
 *
 * @author Aiqing
 * @since 2023/8/30
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RUNTIME)
public @interface IgnoreTenant {

}
