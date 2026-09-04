package com.iwhalecloud.bote.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

/**
 * 属性数据类型
 *
 * @author qian.sisheng
 * @author bianjp
 * @since 2024/8/8
 */
@RequiredArgsConstructor
@Getter
public enum AttrDataType {
  /** 对象 */
  OBJECT("object", Map.class),
  /** 数组 */
  ARRAY("array", List.class),
  /** 字符串 */
  STRING("string", String.class),
  /** 字符串：大字段 */
  TEXT("text", String.class),
  /** 整数 */
  INTEGER("integer", Long.class),
  /** 浮点数 */
  NUMBER("number", BigDecimal.class),
  /** 布尔 */
  BOOLEAN("boolean", Boolean.class),
  /** 日期 */
  DATE("date", LocalDate.class),
  /** 日期时间 */
  DATETIME("datetime", Date.class),
  /** 文件类型 */
  FILE("file", Resource.class),
  /** 任意类型 */
  ANY("any", Object.class);

  /** 类型编码 */
  private final String code;
  /** Java 类型 */
  private final Class<?> javaType;

  /** 类型转换服务 */
  private static ConversionService conversionService;

  /**
   * 获取类型编码
   *
   * <p>添加 JsonValue 注解以使 Jackson 使用类型编码作为 JSON 序列化的结果（默认会使用枚举值名称，但名称是大写的不太方便）</p>
   */
  @JsonValue
  public String getCode() {
    return code;
  }

  /**
   * 转换属性值类型
   */
  @Nullable
  @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
  @SuppressWarnings("PMD.PreserveStackTrace")
  public Object convert(@Nullable String propertyPath, @Nullable Object value) {
    if (value == null || javaType.isInstance(value)) {
      return value;
    }
    // 日期转字符串需要特殊处理，否则会用 toString 方式，转换结果不合预期
    if (this == STRING) {
      if (value instanceof Date) {
        return DateUtil.format((Date) value);
      }
      else if (value instanceof LocalDate) {
        return DateUtil.formatDate(DateUtil.toDate((LocalDate) value));
      }
    }
    if (conversionService == null) {
      conversionService = SpringUtil.getBean(ConversionService.class);
    }
    try {
      return conversionService.convert(value, javaType);
    }
    catch (RuntimeException e) {
      // ObjectToResourceConverter 资源转换会抛出 BssException
      Throwable cause = e.getCause() instanceof BssException ? e.getCause() : e;
      String msg;
      if (propertyPath != null) {
        msg = String.format("属性值类型转换失败：attr=%s，value=%s，source_type=%s，target_type=%s, error=%s",
          propertyPath, value, value.getClass().getCanonicalName(), javaType.getSimpleName(), ExpUtil.getMsg(cause));
      }
      else {
        msg = String.format("属性值类型转换失败：value=%s，source_type=%s，target_type=%s, error=%s",
          value, value.getClass().getCanonicalName(), javaType.getSimpleName(), ExpUtil.getMsg(cause));
      }
      throw new BssException(msg, e);
    }
  }

  /**
   * 转换属性值类型
   */
  @Nullable
  public static Object convert(@Nullable String propertyPath, @Nullable AttrDataType dataType, @Nullable Object value) {
    if (dataType == null || value == null) {
      return value;
    }
    return dataType.convert(propertyPath, value);
  }

  /**
   * 转换属性值类型
   */
  @Nullable
  public static Object convert(@Nullable String propertyPath, @Nullable String dataType, @Nullable Object value) {
    return convert(propertyPath, ofCode(dataType), value);
  }

  /**
   * 根据类型编码获取属性数据类型
   *
   * @param code 数据类型的静态属性值
   * @return 属性数据类型
   */
  public static AttrDataType ofCode(@Nullable String code) {
    if (StringUtils.isEmpty(code)) {
      return null;
    }
    for (AttrDataType type : values()) {
      if (code.equalsIgnoreCase(type.code)) {
        return type;
      }
    }
    throw new BssException("未知的属性类型: " + code);
  }

}
