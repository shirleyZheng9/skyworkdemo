package com.iwhalecloud.bote.llm.client.dto.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.llm.client.consts.JsonSchemaDataFormat;
import com.iwhalecloud.bote.llm.client.consts.JsonSchemaDataType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;

/**
 * JSON Schema 的节点
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class JsonSchemaNode {
  /** 空节点 */
  public static final JsonSchemaNode EMPTY = new JsonSchemaNode();

  /** 类型 */
  private JsonSchemaDataType type;
  /** 格式 */
  private String format;
  /** 描述 */
  private String description;
  /** 标题 */
  private String title;
  /** 对象的属性，key 为属性名称，value 为属性描述 */
  private Map<String, JsonSchemaNode> properties;
  /** 对象的必填属性名称列表 */
  private List<String> required;
  /** 数组元素的结构（所有元素结构相同），仅当 type=array 时使用 */
  private JsonSchemaNode items;
  /** 元组的结构（数组的每个元素 */
  private List<JsonSchemaNode> prefixItems;
  @JsonProperty("enum")
  private String[] enumValues;

  public JsonSchemaNode() {
  }

  public JsonSchemaNode(JsonSchemaDataType type, String description) {
    this.type = type;
    this.description = description;
  }

  public JsonSchemaNode(JsonSchemaDataType type, String format, String description) {
    this.type = type;
    this.format = format;
    this.description = description;
  }

  /**
   * 添加对象的属性
   */
  public JsonSchemaNode addProperty(String name, String description, JsonSchemaDataType dataType) {
    return addProperty(name, description, dataType, false);
  }

  /**
   * 添加对象的属性
   */
  public JsonSchemaNode addProperty(String name, String description, JsonSchemaDataType dataType, boolean required) {
    if (properties == null) {
      properties = new HashMap<>();
    }
    properties.put(name, new JsonSchemaNode(dataType, description));
    if (required) {
      if (this.required == null) {
        this.required = new ArrayList<>();
      }
      this.required.add(name);
    }
    return this;
  }

  /**
   * 构造对象
   */
  public static JsonSchemaNode newObject() {
    return new JsonSchemaNode(JsonSchemaDataType.OBJECT, null);
  }

  /**
   * 构造对象
   */
  public static JsonSchemaNode newObject(String description) {
    return new JsonSchemaNode(JsonSchemaDataType.OBJECT, description);
  }

  /**
   * 构造对象
   */
  public static JsonSchemaNode newObject(String description, Map<String, JsonSchemaNode> properties, List<String> required) {
    JsonSchemaNode node = new JsonSchemaNode(JsonSchemaDataType.OBJECT, description);
    node.setProperties(properties);
    node.setRequired(CollectionUtils.isNotEmpty(required) ? required : null);
    return node;
  }

  /**
   * 构造数组
   */
  public static JsonSchemaNode newArray(String description) {
    return new JsonSchemaNode(JsonSchemaDataType.ARRAY, description);
  }

  /**
   * 构造数组
   */
  public static JsonSchemaNode newArray(String description, JsonSchemaNode item) {
    JsonSchemaNode node = new JsonSchemaNode(JsonSchemaDataType.ARRAY, description);
    node.setItems(item);
    return node;
  }

  /**
   * 构造属性，类型未知
   */
  public static JsonSchemaNode newProperty(String description) {
    return new JsonSchemaNode(null, description);
  }

  /**
   * 构造日期时间属性
   */
  public static JsonSchemaNode newDateTime(String description) {
    return new JsonSchemaNode(JsonSchemaDataType.STRING, JsonSchemaDataFormat.DATETIME, description);
  }

  /**
   * 构造日期属性
   */
  public static JsonSchemaNode newDate(String description) {
    return new JsonSchemaNode(JsonSchemaDataType.STRING, JsonSchemaDataFormat.DATE, description);
  }

  /**
   * 构造字符串属性
   */
  public static JsonSchemaNode newString(String description) {
    return new JsonSchemaNode(JsonSchemaDataType.STRING, description);
  }

  /**
   * 构造数值属性（支持正数、浮点数）
   */
  public static JsonSchemaNode newNumber(String description) {
    return new JsonSchemaNode(JsonSchemaDataType.NUMBER, description);
  }

  /**
   * 构造整数属性
   */
  public static JsonSchemaNode newInteger(String description) {
    return new JsonSchemaNode(JsonSchemaDataType.INTEGER, description);
  }

  /**
   * 构造布尔属性
   */
  public static JsonSchemaNode newBoolean(String description) {
    return new JsonSchemaNode(JsonSchemaDataType.BOOLEAN, description);
  }

}
