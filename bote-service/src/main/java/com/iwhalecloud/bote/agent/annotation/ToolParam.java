package com.iwhalecloud.bote.agent.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 工具参数注解
 *
 * @author bianjp
 * @since 2026-03-09
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ToolParam {
  /** 是否必填 */
  boolean required() default true;

  /** 参数描述 */
  String description() default "";

  /**
   * 枚举值列表
   */
  String[] enumValues() default {};
}
