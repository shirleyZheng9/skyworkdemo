package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.llm.client.consts.JsonSchemaDataType;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * JSON Schema 工具类
 *
 * @author bianjp
 * @since 2024-09-04
 */
public final class JsonSchemaUtil {
  private JsonSchemaUtil() {
  }

  /**
   * 将虚拟根节点转为 JSON Schema 节点
   *
   * @param root 虚拟根节点，可为空，不为空时必须是对象
   * @return 参数结构，虚拟根节点为空或没有子节点时返回 null
   */
  @Nullable
  public static JsonSchemaNode convertRoot(@Nullable ParameterSpec root) {
    Assert.isTrue(root == null || root.isObject(), "根节点必须是对象");
    if (root == null || !root.hasChildren()) {
      return null;
    }
    return convert(root);
  }

  /**
   * 将参数规格转为 JSON Schema 节点
   */
  public static JsonSchemaNode convert(ParameterSpec parameter) {
    // 对象
    if (parameter.isObject()) {
      if (!parameter.hasChildren()) {
        return JsonSchemaNode.newObject(parameter.getDescription());
      }
      List<String> requiredProperties = new ArrayList<>();
      Map<String, JsonSchemaNode> properties = new LinkedHashMap<>();
      for (ParameterSpec property : parameter.getChildren()) {
        // 忽略有赋值的参数，这种不需要大模型组装参数，由 AbstractToolExecutor#convertArgument 处理
        if (Boolean.FALSE.equals(property.getModelVisible()) || StringUtils.isNotEmpty(property.getDefaultValue())) {
          continue;
        }
        properties.put(property.getName(), convert(property));
        if (Boolean.TRUE.equals(property.getRequired())) {
          requiredProperties.add(property.getName());
        }
      }
      return JsonSchemaNode.newObject(parameter.getDescription(), properties, requiredProperties);
    }
    // 列表
    else if (parameter.isList()) {
      ParameterSpec element = parameter.getArrayElement();
      if (element == null) {
        return JsonSchemaNode.newArray(parameter.getDescription());
      }
      return JsonSchemaNode.newArray(parameter.getDescription(), convert(element));
    }
    // 属性
    return convertPrimitiveTypes(parameter);
  }

  /**
   * 转换基本类型的参数
   */
  private static JsonSchemaNode convertPrimitiveTypes(ParameterSpec parameter) {
    AttrDataType dataType = ObjectUtils.getIfNull(parameter.getType(), AttrDataType.ANY);
    switch (dataType) {
      case STRING:
      case FILE:
        return JsonSchemaNode.newString(parameter.getDescription());
      case INTEGER:
        return JsonSchemaNode.newInteger(parameter.getDescription());
      case NUMBER:
        return JsonSchemaNode.newNumber(parameter.getDescription());
      case BOOLEAN:
        return JsonSchemaNode.newBoolean(parameter.getDescription());
      case DATE:
        return JsonSchemaNode.newDate(parameter.getDescription());
      case DATETIME:
        return JsonSchemaNode.newDateTime(parameter.getDescription());
      case ANY:
        return JsonSchemaNode.newProperty(parameter.getDescription());
      default:
        throw new BssException("未知的属性类型: " + dataType.getCode());
    }
  }

  /**
   * 解析根节点的 JSON Schema
   *
   * @param node JSON Schema 节点
   * @return 虚拟根节点的参数规格
   */
  public static ParameterSpec parseRootSchema(@Nullable JsonSchemaNode node) {
    if (node == null) {
      return ParameterSpec.newRoot();
    }
    Assert.isTrue(node.getType() == null || node.getType() == JsonSchemaDataType.OBJECT, "根节点必须是对象");
    if (MapUtils.isEmpty(node.getProperties())) {
      return ParameterSpec.newRoot();
    }
    ParameterSpec spec = parseObjectSchema(BaseConsts.PARAMETER_NODE_ROOT, node);
    spec.setDescription(BaseConsts.PARAMETER_NODE_ROOT_DESCRIPTION);
    return spec;
  }

  /**
   * 解析 JSON Schema 节点
   */
  private static ParameterSpec parseSchema(String name, JsonSchemaNode node) {
    JsonSchemaDataType type = node.getType();
    if (type == JsonSchemaDataType.OBJECT) {
      return parseObjectSchema(name, node);
    }
    if (type == JsonSchemaDataType.ARRAY) {
      return parseArraySchema(name, node);
    }
    // 兼容异常数据
    if (type == null) {
      return ParameterSpec.newProperty(name, node.getDescription(), AttrDataType.ANY);
    }
    return parsePropertySchema(name, node, type);
  }

  /**
   * 解析对象类型的 JSON Schema 节点
   */
  private static ParameterSpec parseObjectSchema(String name, JsonSchemaNode node) {
    List<ParameterSpec> children = null;
    if (MapUtils.isNotEmpty(node.getProperties())) {
      children = new ArrayList<>(node.getProperties().size());
      List<String> required = ListUtils.emptyIfNull(node.getRequired());
      for (Map.Entry<String, JsonSchemaNode> entry : node.getProperties().entrySet()) {
        ParameterSpec spec = parseSchema(entry.getKey(), entry.getValue());
        if (required.contains(entry.getKey())) {
          spec.setRequired(true);
        }
        children.add(spec);
      }
    }
    return ParameterSpec.newObject(name, node.getDescription(), children);
  }

  /**
   * 解析数组类型的 JSON Schema 节点
   */
  private static ParameterSpec parseArraySchema(String name, JsonSchemaNode node) {
    ParameterSpec element = null;
    if (node.getItems() != null) {
      element = parseSchema("listItem", node.getItems());
    }
    return ParameterSpec.newList(name, node.getDescription(), element);
  }

  /**
   * 解析基本属性类型的 JSON Schema 节点
   */
  private static ParameterSpec parsePropertySchema(String name, JsonSchemaNode node, JsonSchemaDataType type) {
    AttrDataType dataType;
    switch (type) {
      case STRING:
        // https://json-schema.org/understanding-json-schema/reference/type#dates-and-times
        if ("date-time".equals(node.getFormat())) {
          dataType = AttrDataType.DATETIME;
        }
        else if ("date".equals(node.getFormat())) {
          dataType = AttrDataType.DATE;
        }
        else {
          dataType = AttrDataType.STRING;
        }
        break;
      case NUMBER:
        dataType = AttrDataType.NUMBER;
        break;
      case INTEGER:
        dataType = AttrDataType.INTEGER;
        break;
      case BOOLEAN:
        dataType = AttrDataType.BOOLEAN;
        break;
      case NULL:
        dataType = AttrDataType.ANY;
        break;
      default:
        throw new BssException("未知的 JSON Schema 参数类型: " + type);
    }
    return ParameterSpec.newProperty(name, node.getDescription(), dataType);
  }
}
