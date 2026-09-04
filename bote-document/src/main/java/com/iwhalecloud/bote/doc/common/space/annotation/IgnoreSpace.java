package com.iwhalecloud.bote.doc.common.space.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 忽略企业空间标识注解
 *
 * @author Aiqing
 * @since 2025/10/24
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RUNTIME)
public @interface IgnoreSpace {

}
