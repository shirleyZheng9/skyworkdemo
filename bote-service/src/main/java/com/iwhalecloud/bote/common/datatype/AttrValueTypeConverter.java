package com.iwhalecloud.bote.common.datatype;

import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * 规则属性值类型转换器
 *
 * @author bianjp
 * @since 2025-11-25
 */
@Service
@RequiredArgsConstructor
public class AttrValueTypeConverter {
  private static final Logger logger = LoggerFactory.getLogger(AttrValueTypeConverter.class);

  private final ConversionService conversionService;

  /**
   * 将属性值转为指定的类型 数据数据类型由枚举类 {@link AttrDataType} 维护 类型转换借用 Spring 现成的 {@link ConversionService}
   *
   * @param source 属性值
   * @param dataTypeValue 属性数据类型
   * @return 转换后的属性值
   */
  @Nullable
  public Object convertValue(@Nullable Object source, String dataTypeValue) {
    return convertValue(null, source, dataTypeValue);
  }

  /**
   * 将属性值转为指定的类型 数据数据类型由枚举类 {@link AttrDataType} 维护 类型转换借用 Spring 现成的 {@link ConversionService}
   *
   * @param propertyPath 属性路径。报错时放在错误信息中以便于定位属性
   * @param source 属性值
   * @param dataTypeValue 属性数据类型
   * @return 转换后的属性值
   */
  @Nullable
  public Object convertValue(@Nullable String propertyPath, @Nullable Object source, String dataTypeValue) {
    if (source == null) {
      return null;
    }

    AttrDataType dataType = AttrDataType.ofCode(dataTypeValue);
    if (dataType == null) {
      throw new BssException("未知的属性数据类型：attr=" + propertyPath + "，type=" + dataTypeValue);
    }

    // 如果类型相同，不需要再转换
    if (dataType.getJavaType().isInstance(source)) {
      return source;
    }

    // linkedHashMap 转换json 抛错，这个不做转换；使用到的地方再做特殊处理
    if (source.getClass().equals(LinkedHashMap.class) && dataType.getJavaType().equals(String.class)) {
      return source;
    }

    return doConvertValue(propertyPath, source, dataType.getJavaType());
  }

  @Nullable
  private Object doConvertValue(@Nullable String description, Object source, Class<?> targetType) {
    Object result;
    try {
      result = conversionService.convert(source, targetType);
    }
    catch (RuntimeException e) {
      String message = String.format("属性值类型转换失败：attr=%s，value=%s，source_type=%s，target_type=%s", description, source,
        source.getClass().getCanonicalName(), targetType.getCanonicalName());
      logger.error(message, e);
      throw new BssException(message, e);
    }

    return result;
  }
}
