package com.iwhalecloud.bote.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 参数转换工具类
 *
 * <p>用于将大模型返回的工具参数、前端传的参数，根据参数结构转换成要求的结构，注意不处理参数的赋值(value)，但会处理默认值(defaultValue)</p>
 *
 * <p>会做必要的类型转换（比如整数和字符串的互转、字符串转为日期对象）</p>
 *
 * @author bianjp
 * @since 2025-04-18
 */
public final class ParamConverterUtil {
  private ParamConverterUtil() {
  }

  /**
   * 解析大模型返回的工具参数 JSON 字符串
   *
   * @param toolArgumentsJson JSON 字符串
   * @return 参数
   */
  @Nullable
  public static Map<String, Object> parseToolArgumentsJson(@Nullable String toolArgumentsJson) {
    if (StringUtils.isEmpty(toolArgumentsJson) || "null".equals(toolArgumentsJson)) {
      return null;
    }
    else if ("{}".equals(toolArgumentsJson)) {
      return Collections.emptyMap();
    }
    return JsonUtil.parseJsonRequired(toolArgumentsJson, new TypeReference<Map<String, Object>>() {
    });
  }

  /**
   * 将对象转为数组，用于构造脚本的参数
   *
   * <p>页面上配置脚本的参数结构时，使用对象形式，但实际执行脚本时需要以数组形式传递</p>
   *
   * <p>注意: 只做数组转换，不处理元素的类型转换</p>
   *
   * @param root 虚拟根节点，必须是对象
   * @param params 参数对象
   * @return 参数列表
   */
  public static Object[] convertMapToArray(@Nullable ParameterSpec root, @Nullable Map<String, Object> params) {
    Assert.isTrue(root == null || root.isObject(), "根节点必须是对象");
    // 没有参数，返回空数组
    if (root == null || !root.hasChildren()) {
      return new Object[0];
    }
    // 所有参数都为空，返回固定长度的空数组
    if (MapUtils.isEmpty(params)) {
      return new Object[root.getChildren().size()];
    }
    return root.getChildren().stream().map(c -> params.get(c.getName())).toArray();
  }

  /**
   * 转换根节点参数
   *
   * @param root 虚拟根节点，可以为空，不为空时必须是对象
   * @param params 原始参数
   * @return 转换后的参数
   */
  public static Map<String, Object> convertRoot(@Nullable ParameterSpec root, @Nullable Map<String, Object> params) {
    Assert.isTrue(root == null || root.isObject(), "根节点必须是对象");
    // 没有根节点，或者根节点没有子节点，表示不需要参数，直接返回空对象
    if (root == null || !root.hasChildren()) {
      return Collections.emptyMap();
    }
    return MapUtils.emptyIfNull(convertObject("", root, params));
  }

  /**
   * 转换任意类型的参数
   *
   * @param propertyPath 属性路径，用于错误提示，虚拟根节点传空字符串
   * @param spec 参数结构，可以是任意类型
   * @param value 原始值
   * @return 转换后的参数值
   */
  @Nullable
  public static Object convert(String propertyPath, @Nullable ParameterSpec spec, @Nullable Object value) {
    if (spec == null) {
      return value;
    }
    // 对象
    if (spec.isObject()) {
      return convertObject(propertyPath, spec, value);
    }
    // 列表
    else if (spec.isList()) {
      return convertList(propertyPath, spec, value);
    }
    // 属性
    return convertProperty(propertyPath, spec, value);
  }

  /**
   * 转换对象
   *
   * @param propertyPath 对象的属性路径，用于错误提示
   * @param spec 对象结构
   * @param value 原始值
   * @return 对象，可能为 null
   */
  @Nullable
  @SuppressWarnings("unchecked")
  public static Map<String, Object> convertObject(String propertyPath, ParameterSpec spec, @Nullable Object value) {
    Assert.isTrue(spec.isObject(), () -> "参数类型必须是对象: property=" + propertyPath);
    if (value == null) {
      // 如果未配置子节点，或者子节点都未赋值且没有默认值，直接返回 null
      if (!spec.hasChildrenAssignment()) {
        return null;
      }
      value = Collections.emptyMap();
    }
    Assert.isTrue(value instanceof Map, "参数类型错误，应为 Map: param=" + spec.getName() + ", 实际为 " + value.getClass().getCanonicalName());
    // 未配置对象结构时不处理
    if (!spec.hasChildren()) {
      return (Map<String, Object>) value;
    }
    // 转换对象结构
    Map<String, Object> originalMap = (Map<String, Object>) value;
    Map<String, Object> convertedMap = new LinkedHashMap<>();
    for (ParameterSpec property : spec.getChildren()) {
      String childPropertyPath = propertyPath.isEmpty() ? property.getName() : propertyPath + "." + property.getName();
      Object propertyValue = convert(childPropertyPath, property, originalMap.get(property.getName()));
      convertedMap.put(property.getName(), propertyValue);
    }
    return convertedMap;
  }

  /**
   * 转换列表
   *
   * @param propertyPath 列表的属性路径，用于错误提示
   * @param spec 列表结构
   * @param value 原始值
   * @return 列表，可能为 null
   */
  @Nullable
  @SuppressWarnings("unchecked")
  public static List<Object> convertList(String propertyPath, ParameterSpec spec, @Nullable Object value) {
    Assert.isTrue(spec.isList(), () -> "参数类型必须是数组: property=" + propertyPath);
    if (value == null) {
      return null;
    }
    Assert.isTrue(value instanceof List, "参数类型错误，应为 List: param=" + spec.getName() + ", 实际为 " + value.getClass().getCanonicalName());
    List<Object> originalList = (List<Object>) value;
    if (originalList.isEmpty()) {
      return Collections.emptyList();
    }
    List<Object> targetList;
    ParameterSpec elementSpec = spec.getArrayElement();
    // 转换列表元素的结构，未配置时不处理
    if (elementSpec == null) {
      targetList = originalList;
    }
    else {
      targetList = new ArrayList<>(originalList.size());
      for (int i = 0, originalListSize = originalList.size(); i < originalListSize; i++) {
        targetList.add(convert(propertyPath + "[" + i + "]", elementSpec, originalList.get(i)));
      }
    }
    return targetList;
  }

  /**
   * 转换简单属性
   *
   * @param propertyPath 属性路径，用于错误提示
   * @param spec 属性结构
   * @param value 原始值
   * @return 属性值，可能为 null
   */
  @Nullable
  private static Object convertProperty(String propertyPath, ParameterSpec spec, @Nullable Object value) {
    // 属性，支持默认值
    if (value == null) {
      if (StringUtils.isEmpty(spec.getDefaultValue())) {
        return null;
      }
      value = spec.getDefaultValue();
    }
    return AttrDataType.convert(propertyPath, spec.getType(), value);
  }
}
