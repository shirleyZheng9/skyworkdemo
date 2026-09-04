package com.iwhalecloud.bote.agent.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 工具请求注解，用于标记使用 POJO 接收工具调用的所有参数
 *
 * @author bianjp
 * @since 2026-03-14
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ToolRequest {
}
