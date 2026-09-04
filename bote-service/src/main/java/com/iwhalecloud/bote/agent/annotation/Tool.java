package com.iwhalecloud.bote.agent.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 工具注解
 *
 * @author bianjp
 * @since 2026-03-09
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Tool {
  /** 工具名称 */
  String name();

  /** 工具描述 */
  String description() default "";

  /** 是否直接返回（开启时工具出参作为最终结果，不再调用大模型） */
  boolean returnDirect() default false;

  /** 是否隐藏工具调用 */
  boolean hideToolCall() default false;
}
