package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 内置Schema工具类
 * 迁移对应关系: Go语言entity.builtinSchemas
 * - 功能: 提供内置的JSON Schema
 * - 字段定义: 各种内置Schema的映射
 * <p>
 * Java实现说明:
 * - 对应Go的entity.builtinSchemas变量
 * - 使用Java静态方法提供内置Schema
 * - 包含Schema获取方法
 * <p>
 * 技术栈迁移:
 * - Go变量 -> Java静态方法
 * - Go map -> Java Map
 */
public final class BuiltinSchemas {

  private BuiltinSchemas() {
    // 工具类，禁止实例化
  }

  private static final Map<SchemaKey, String> SCHEMA_MAP = new HashMap<>();

  static {
    SCHEMA_MAP.put(SchemaKey.STRING, "{\"type\": \"string\"}");
    SCHEMA_MAP.put(SchemaKey.INTEGER, "{\"type\": \"integer\"}");
    SCHEMA_MAP.put(SchemaKey.FLOAT, "{\"type\": \"number\"}");
    SCHEMA_MAP.put(SchemaKey.BOOL, "{\"type\": \"boolean\"}");
    SCHEMA_MAP.put(SchemaKey.MESSAGE, "{\"type\": \"object\", \"properties\": {\"role\": {\"type\": \"string\"}, \"content\": {\"type\": \"string\"}}}");
  }

  /**
   * 获取Schema
   * 迁移对应关系: Go语言entity.builtinSchemas[key]
   * - 功能: 根据SchemaKey获取对应的Schema
   * - 参数: key - Schema键
   * - 返回: Schema字符串
   */
  public static String getSchema(SchemaKey key) {
    return SCHEMA_MAP.get(key);
  }

  /**
   * 获取Schema对象
   * 迁移对应关系: Go语言entity.builtinSchemas[key]
   * - 功能: 根据SchemaKey获取对应的JSONSchema对象
   * - 参数: key - Schema键
   * - 返回: JSONSchema对象
   */
  public static JSONSchema getSchemaObject(SchemaKey key) {
    String schemaString = SCHEMA_MAP.get(key);
    if (schemaString != null) {
      return JSONSchema.newJSONSchema(schemaString);
    }
    return null;
  }
}
