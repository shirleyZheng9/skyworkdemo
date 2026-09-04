package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.ArrayFilterPluginParams;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 数组筛选器插件
 * 快速获取数组中匹配特定条件的值，方便数据筛选和提取
 *
 * @author lizuyin
 * @since 2025-11-17
 */
@Component
public class ArrayFilterPlugin extends AbstractPlugin<ArrayFilterPluginParams> {

  public ArrayFilterPlugin() {
    super(ArrayFilterPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_ARRAY_FILTER;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("array", "待筛选的数组（必填，List或数组类型）", AttrDataType.ARRAY));
    children.add(ParameterSpec.newProperty("fieldName", "字段名（可选，字符串类型），如果数组元素是对象，则通过此字段访问属性；如果为null或空字符串，则直接比较数组元素本身", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("operator", "操作符（必填，字符串类型），可选值：gt, gte, lt, lte, eq, contains, notContains", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("value", "比较值（必填，任意类型），用于与数组元素进行比较的值", AttrDataType.ANY));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("success", "是否成功", AttrDataType.BOOLEAN));
    children.add(ParameterSpec.newProperty("message", "消息", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("result", "筛选结果列表", AttrDataType.ARRAY));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(ArrayFilterPluginParams params) {
    Assert.notNull(params, "参数不能为空");
    Assert.notNull(params.getArray(), "参数 array 不能为空");
    Assert.notNull(params.getOperator(), "参数 operator 不能为空");

    // 校验 operator 参数
    String operator = params.getOperator().trim().toLowerCase();
    List<String> validOperators = List.of("gt", "gte", "lt", "lte", "eq", "contains", "notcontains");
    Assert.isTrue(validOperators.contains(operator),
        "参数 operator 必须为 \"gt\"、\"gte\"、\"lt\"、\"lte\"、\"eq\"、\"contains\" 或 \"notContains\" 之一");

    // 对于某些操作符，value可以为null（表示检查null值）
    // 其他操作符，value不能为null
    if (params.getValue() == null && !"contains".equals(operator) && !"notcontains".equals(operator)) {
      throw new IllegalArgumentException("参数 value 不能为空");
    }
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public Object doRun(ArrayFilterPluginParams pluginParams) {
    try {
      // 将array转换为List
      List<?> list = convertToList(pluginParams.getArray());
      if (list == null) {
        return createErrorResult("参数 array 必须是数组或List类型");
      }

      // 处理fieldName
      String fieldName = pluginParams.getFieldName();
      boolean useField = StringUtils.isNotBlank(fieldName);
      String fieldNameStr = useField ? fieldName.trim() : null;

      // 转换并校验 operator 参数
      String operator = pluginParams.getOperator().trim().toLowerCase();

      // 执行筛选
      List<Object> resultList = new ArrayList<>();

      for (Object item : list) {
        try {
          // 获取要比较的值
          Object compareValue = getCompareValue(item, useField, fieldNameStr);
          if (compareValue == null && useField) {
            // 字段不存在或无法访问，跳过该元素
            continue;
          }

          // 根据操作符执行比较
          boolean match = performComparison(compareValue, pluginParams.getValue(), operator);

          // 如果匹配，添加到结果列表
          if (match) {
            resultList.add(item);
          }
        } catch (Exception e) {
          // 单个元素处理失败，跳过该元素
          logger.debug("处理数组元素时发生异常，跳过该元素: {}", e.getMessage());
        }
      }

      // 返回成功结果
      return createSuccessResult(resultList);
    } catch (Exception e) {
      logger.error("筛选数组时发生异常", e);
      return createErrorResult("筛选数组时发生异常: " + e.getMessage());
    }
  }

  /**
   * 将array转换为List
   */
  private List<?> convertToList(Object array) {
    if (array == null) {
      return null;
    }

    if (array instanceof List<?> list) {
      return list;
    }

    if (array.getClass().isArray()) {
      List<Object> list = new ArrayList<>();
      int length = java.lang.reflect.Array.getLength(array);
      for (int i = 0; i < length; i++) {
        list.add(java.lang.reflect.Array.get(array, i));
      }
      return list;
    }

    return null;
  }

  /**
   * 获取要比较的值
   */
  private Object getCompareValue(Object item, boolean useField, String fieldName) {
    if (!useField) {
      // 直接使用数组元素
      return item;
    }

    // 从对象中获取字段值
    if (item == null) {
      return null;
    }

    // 尝试通过属性访问
    if (item instanceof Map) {
      return ((Map<?, ?>) item).get(fieldName);
    }

    // 尝试通过反射访问字段
    try {
      Field field = item.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      return field.get(item);
    }
    catch (NoSuchFieldException | IllegalAccessException | SecurityException e) {
      // 字段不存在或无法访问，尝试通过getter方法访问
      return null;
    }
  }
  /**
   * 执行比较操作
   */
  private boolean performComparison(Object compareValue, Object targetValue, String operator) {
    switch (operator) {
      case "gt":
        // 大于
        return compareNumbers(compareValue, targetValue) > 0;
      case "gte":
        // 大于等于
        return compareNumbers(compareValue, targetValue) >= 0;
      case "lt":
        // 小于
        return compareNumbers(compareValue, targetValue) < 0;
      case "lte":
        // 小于等于
        return compareNumbers(compareValue, targetValue) <= 0;
      case "eq":
        // 等于
        return compareEqual(compareValue, targetValue);
      case "contains":
        // 包含
        return checkContains(compareValue, targetValue);
      case "notcontains":
        // 不包含
        return !checkContains(compareValue, targetValue);
      default:
        return false;
    }
  }

  /**
   * 比较两个数字
   *
   * @param a 第一个值
   * @param b 第二个值
   * @return 负数表示a<b，0表示a==b，正数表示a>b
   */
  private int compareNumbers(Object a, Object b) {
    try {
      // 尝试转换为数字进行比较
      BigDecimal numA = convertToNumber(a);
      BigDecimal numB = convertToNumber(b);
      return numA.compareTo(numB);
    } catch (Exception e) {
      // 如果无法转换为数字，尝试字符串比较
      return a.toString().compareTo(b.toString());
    }
  }

  /**
   * 将对象转换为数字
   */
  private BigDecimal convertToNumber(Object value) {
    if (value instanceof Number) {
      if (value instanceof BigDecimal) {
        return (BigDecimal) value;
      }
      return BigDecimal.valueOf(((Number) value).doubleValue());
    }
    return new BigDecimal(value.toString());
  }

  /**
   * 比较两个值是否相等
   *
   * @param a 第一个值
   * @param b 第二个值
   * @return true表示相等，false表示不相等
   */
  private boolean compareEqual(Object a, Object b) {
    if (a == null && b == null) {
      return true;
    }
    if (a == null || b == null) {
      return false;
    }

    // 尝试数字比较
    try {
      BigDecimal numA = convertToNumber(a);
      BigDecimal numB = convertToNumber(b);
      return numA.compareTo(numB) == 0;
    } catch (Exception e) {
      // 如果无法转换为数字，使用字符串比较
      return a.toString().equals(b.toString());
    }
  }

  /**
   * 检查是否包含
   *
   * @param source 源值（字符串或集合）
   * @param target 目标值
   * @return true表示包含，false表示不包含
   */
  private boolean checkContains(Object source, Object target) {
    if (source == null && target == null) {
      return true;
    }
    if (source == null || target == null) {
      return false;
    }

    // 如果source是字符串，检查是否包含子串
    if (source instanceof String || source instanceof CharSequence) {
      return source.toString().contains(target.toString());
    }

    // 如果source是集合，检查是否包含元素
    if (source instanceof Collection) {
      return ((Collection<?>) source).contains(target);
    }

    // 其他情况，转换为字符串后检查
    return source.toString().contains(target.toString());
  }

  /**
   * 创建成功结果
   */
  private Map<String, Object> createSuccessResult(List<Object> result) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", "");
    response.put("result", result);
    return response;
  }

  /**
   * 创建错误结果
   */
  private Map<String, Object> createErrorResult(String message) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", false);
    response.put("message", message);
    response.put("result", Collections.emptyList());
    return response;
  }
}

