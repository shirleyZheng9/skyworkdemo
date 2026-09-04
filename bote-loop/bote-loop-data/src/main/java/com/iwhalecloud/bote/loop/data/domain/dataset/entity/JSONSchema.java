package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.loop.data.pkg.json.JSONSchemaSpec;
import com.iwhalecloud.bote.loop.data.pkg.json.JSONSchemaSpecChecker;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * JSON Schema实体
 */
@Getter
public final class JSONSchema {
  @JsonProperty("raw")
  private String raw;
  @JsonIgnore
  private JSONSchemaSpec schema;

  private JSONSchema() {
  }

  public static JSONSchema newJSONSchema(String raw) {
    JSONSchema jsonSchema = new JSONSchema();
    jsonSchema.setRaw(raw);
    return jsonSchema;
  }

  public void setRaw(String raw) {
    this.raw = raw;
    this.schema = JsonUtil.parseJson(raw, JSONSchemaSpec.class);
  }

  @Override
  public String toString() {
    return this.raw;
  }

  /**
   * 兼容性校验：other的类型包含当前所有类型
   */
  public boolean compatibleWith(JSONSchema other) {
    List<String> currentTypes = getTypes();
    List<String> otherTypes = other.getTypes();
    return new HashSet<>(otherTypes).containsAll(currentTypes);
  }

  /**
   * 校验JSON内容
   */
  public void validate(String content) {
    JSONSchemaSpecChecker.check(schema, content);
  }

  /**
   * 提取Schema定义的类型
   */
  public List<String> getTypes() {
    try {
      JsonNode rootNode = JsonUtil.getObjectMapper().readTree(this.raw);
      JsonNode typeNode = rootNode.get("type");
      if (typeNode == null) {
        return new ArrayList<>();
      }

      List<String> types = new ArrayList<>();
      if (typeNode.isTextual()) {
        // 单类型（如 "type": "string"）
        types.add(typeNode.asText());
      } else if (typeNode.isArray()) {
        // 多类型（如 "type": ["string", "number"]）
        for (JsonNode node : typeNode) {
          if (node.isTextual()) {
            types.add(node.asText());
          }
        }
      }
      return types;
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }

  /**
   * 获取单一类型（若类型数量为1则返回，否则返回空）
   */
  public String getSingleType() {
    List<String> types = getTypes();
    if (types.size() == 1) {
      return types.get(0);
    }
    return "";
  }

}
