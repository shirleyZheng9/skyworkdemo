package com.iwhalecloud.bote.doc.common.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 自定义值校验注解
 *
 * @author Aiqing
 * @since 2025-08-19
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = EnumValueValidate.class)
public @interface EnumValue {

  /**
   * 默认错误消息
   *
   * @return str
   */
  String message() default "参数值不正确";

  /**
   * 枚举值字符串，与enumClass二选一
   *
   * @return 字符串数组
   */
  String[] values() default {};

  /**
   * 枚举类（与values二选一）
   */
  Class<? extends Enum<?>> enumClass() default UndefinedEnum.class;

  /**
   * 分组
   *
   * @return 分组class
   */
  Class<?>[] groups() default {};

  /**
   * payload
   *
   * @return clazz
   */
  Class<? extends Payload>[] payload() default {};

  boolean required() default true;

  /**
   * 是否忽略大小写 (默认不忽略)
   */
  boolean ignoreCase() default false;

  /**
   * 多选
   *
   * @return bool
   */
  boolean allowMultiple() default false;

  /**
   * 是否允许为空
   */
  boolean allowNull() default true;

  /**
   * 定义一个空枚举类作为默认值
   */
  enum UndefinedEnum {
    // 默认值
  }
}
