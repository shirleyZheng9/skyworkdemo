package com.iwhalecloud.bote.doc.common.validate;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 自定义校验器
 */
public class EnumValueValidate implements ConstraintValidator<EnumValue, Object> {

  private Set<String> allowedValues;
  private boolean ignoreCase;
  private boolean allowNull;
  private boolean allowMultiple;

  @Override
  public void initialize(EnumValue constraintAnnotation) {
    Class<? extends Enum<?>> enumClass = constraintAnnotation.enumClass();
    this.ignoreCase = constraintAnnotation.ignoreCase();
    this.allowNull = constraintAnnotation.allowNull();
    this.allowMultiple = constraintAnnotation.allowMultiple();

    // 检查是否使用了默认的UndefinedEnum
    boolean usingDefaultEnum = constraintAnnotation.enumClass().equals(EnumValue.UndefinedEnum.class);

    if (!usingDefaultEnum) {
      // 从枚举类获取允许的值
      this.allowedValues = Arrays.stream(enumClass.getEnumConstants())
        .map(Enum::name)
        .collect(Collectors.toSet());
    }
    else if (constraintAnnotation.values().length > 0) {
      // 从注解的values属性获取允许的值
      this.allowedValues = Stream.of(constraintAnnotation.values())
        .collect(Collectors.toSet());
    }
    else {
      throw new IllegalArgumentException("必须指定enumClass或values");
    }

  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (value == null) {
      return allowNull;
    }

    // 处理集合/数组类型
    if (value.getClass().isArray() || value instanceof Collection) {
      if (!allowMultiple) {
        return false;
      }
      return validateCollection(value);
    }

    // 处理单个值
    return validateSingleValue(value);
  }

  private boolean validateCollection(Object collection) {
    if (collection.getClass().isArray()) {
      return Arrays.stream((Object[]) collection)
        .allMatch(this::validateSingleValue);
    }
    else if (collection instanceof Collection) {
      return ((Collection<?>) collection).stream()
        .allMatch(this::validateSingleValue);
    }
    return false;
  }

  private boolean validateSingleValue(Object value) {
    if (value instanceof Enum<?>) {
      return allowedValues.contains(((Enum<?>) value).name());
    }

    String strValue = value.toString();
    if (ignoreCase) {
      return allowedValues.stream()
        .anyMatch(v -> v.equalsIgnoreCase(strValue));
    }
    else {
      return allowedValues.contains(strValue);
    }
  }
}
