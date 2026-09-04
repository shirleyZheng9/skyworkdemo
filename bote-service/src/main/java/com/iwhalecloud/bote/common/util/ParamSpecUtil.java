package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.Nullable;

/**
 * 参数规格工具类
 *
 * @author chen.linfa
 * @author bianjp
 * @since 2024-09-21
 */
public final class ParamSpecUtil {
  /** 日期模式 */
  private static final Pattern datePattern = Pattern.compile("^\\d{4}-\\d{1,2}-\\d{1,2}$");
  /** 日期时间模式，只匹配开头，以兼容末尾包含时区信息的情况 */
  private static final Pattern dateTimePattern = Pattern.compile("^\\d{4}-\\d{1,2}-\\d{1,2} \\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?");

  private ParamSpecUtil() {
  }

  /**
   * 根据参数值构造参数规格
   *
   * @param params 参数值
   * @return 参数规格
   */
  public static ParameterSpec buildParamSpec(Object params) {
    return buildSpec(params, BaseConsts.PARAMETER_NODE_ROOT, BaseConsts.PARAMETER_NODE_ROOT_DESCRIPTION);
  }

  /**
   * 构造参数规格
   *
   * @param value 参数值
   * @param name 参数名称
   * @param description 参数描述
   */
  @SuppressWarnings("unchecked")
  private static ParameterSpec buildSpec(Object value, String name, @Nullable String description) {
    // 对象
    if (value instanceof Map) {
      Map<String, Object> map = (Map<String, Object>) value;
      // 对象属性
      List<ParameterSpec> children = null;
      if (!map.isEmpty()) {
        children = new ArrayList<>(map.size());
        for (Entry<String, Object> entry : map.entrySet()) {
          children.add(buildSpec(entry.getValue(), entry.getKey(), null));
        }
      }
      return ParameterSpec.newObject(name, description, children);
    }

    // 列表
    if (value instanceof List) {
      List<Object> list = (List<Object>) value;
      // 列表元素
      ParameterSpec element = null;
      if (CollectionUtils.isNotEmpty(list)) {
        element = buildSpec(list.get(0), "listItem", null);
      }
      return ParameterSpec.newList(name, description, element);
    }

    // 属性
    AttrDataType type = detectDataType(value);
    return ParameterSpec.newProperty(name, description, type);
  }

  /**
   * 探测属性的数据类型
   */
  private static AttrDataType detectDataType(@Nullable Object value) {
    AttrDataType type;
    if (value instanceof String) {
      type = detectDataTypeOfString((String) value);
    }
    else if (value instanceof Integer || value instanceof Long) {
      type = AttrDataType.INTEGER;
    }
    else if (value instanceof Number) {
      type = AttrDataType.NUMBER;
    }
    else if (value instanceof Boolean) {
      type = AttrDataType.BOOLEAN;
    }
    else if (value instanceof LocalDate) {
      type = AttrDataType.DATE;
    }
    else if (value instanceof Date) {
      type = AttrDataType.DATETIME;
    }
    else {
      type = AttrDataType.ANY;
    }
    return type;
  }

  /**
   * 探测字符串的类型，用于兼容日期类型（JSON 不支持日期类型）
   */
  private static AttrDataType detectDataTypeOfString(String value) {
    // 检测日期类型
    if (datePattern.matcher(value).matches()) {
      return AttrDataType.DATE;
    }
    if (dateTimePattern.matcher(value).matches()) {
      return AttrDataType.DATETIME;
    }
    return AttrDataType.STRING;
  }
}
