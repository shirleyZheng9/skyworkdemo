package com.iwhalecloud.bote.common.operator.impl.list;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import com.iwhalecloud.bote.common.operator.AbstractOperatorExecutor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.Nullable;

/**
 * 列表操作符抽象类
 *
 * @author bianjp
 * @since 2024-08-30
 */
public abstract class AbstractListOperatorExecutor extends AbstractOperatorExecutor {
  /** 类型转换服务 */
  protected static final ConversionService conversionService = SpringUtil.getBean(ConversionService.class);

  /**
   * 解析列表
   */
  @SuppressWarnings("unchecked")
  protected List<Object> parseList(String spec, @Nullable Object value) {
    List<Object> result;
    if (value == null) {
      value = Collections.emptyList();
    }
    if (value instanceof List) {
      result = (List<Object>) value;
    }
    else if (value instanceof Collection) {
      result = new ArrayList<>((Collection<Object>) value);
    }
    else if (value.getClass().isArray()) {
      result = Arrays.asList((Object[]) value);
    }
    else {
      throw new BssException("条件表达式的条件参数不是列表: " + spec);
    }
    return result;
  }

  /**
   * 是否是简单类型
   */
  protected boolean isSimpleType(Class<?> clazz) {
    return clazz == String.class || clazz == Date.class || Number.class.isAssignableFrom(clazz) || ClassUtils.isPrimitiveOrWrapper(clazz);
  }
}
