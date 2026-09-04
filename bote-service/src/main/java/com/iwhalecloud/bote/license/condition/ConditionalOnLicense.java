package com.iwhalecloud.bote.license.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要 license 的条件注解（与 {@link OnLicenseCondition} 一致；{@code bote.license.enabled=false} 时不匹配）。
 *
 * @author zhangJun
 * @since 2022-04-07
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(OnLicenseCondition.class)
public @interface ConditionalOnLicense {
}
