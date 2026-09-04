package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段Schema实体
 * 迁移对应关系: Go语言entity.FieldSchema
 * - 功能: 存储字段Schema信息
 * - 字段定义:
 * * Key: string - 数据集 Schema 版本变化中 Key 唯一
 * * Name: string - 展示名称
 * * Description: string - 描述
 * * ContentType: ContentType - 类型，如文本，图片
 * * DefaultFormat: FieldDisplayFormat - 默认展示格式
 * * SchemaKey: SchemaKey - 内置格式 key
 * * TextSchema: *JSONSchema - 文本内容格式限制，JSON schema 格式
 * * MultiModelSpec: *MultiModalSpec - 多模态规格限制
 * * Status: FieldStatus - 状态
 * * Hidden: bool - 用户不可见
 * * IsRequired: bool - 是否必填
 * <p>
 * Java实现说明:
 * - 对应Go的entity.FieldSchema结构体
 * - 使用Java类定义，包含字段Schema字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化（使用驼峰命名）
 * - 包含业务逻辑方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson默认驼峰命名
 * - Go指针类型 -> Java对象引用
 * - Go枚举类型 -> Java枚举
 * - Go方法 -> Java方法
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldSchema {

  private String key;

  private String name;

  private String description;

  private ContentType contentType;

  private FieldDisplayFormat defaultFormat;

  private SchemaKey schemaKey;

  private JSONSchema textSchema;

  private MultiModalSpec multiModelSpec;

  private FieldStatus status;

  private Boolean hidden;

  private Boolean isRequired;

  /**
   * 验证字段数据
   * 迁移对应关系: Go语言entity.FieldSchema.ValidateData()
   * - 功能: 验证字段数据是否符合Schema要求
   * - 参数: data - 字段数据
   * - 返回: 验证结果
   */
  public void validateData(FieldData data) {
    if (data == null) {
      throw new IllegalArgumentException("nil field data");
    }
    if (!key.equals(data.getKey())) {
      throw new IllegalArgumentException(String.format("key mismatch, schema_key=%s, data_key=%s", key, data.getKey()));
    }

    switch (contentType) {
      case TEXT:
        validateTextData(data);
        break;
      case IMAGE:
      case AUDIO:
      case VIDEO:
        validateMultiModalData(data);
        break;
      case MULTIPART:
        throw new UnsupportedOperationException("multipart content type not supported");
      default:
        throw new UnsupportedOperationException("unsupported content type: " + contentType);
    }
  }

  /**
   * 验证文本数据
   * 迁移对应关系: Go语言entity.FieldSchema.validateTextData()
   * - 功能: 验证文本数据
   * - 参数: data - 字段数据
   */
  private void validateTextData(FieldData data) {
    JSONSchema schema = textSchema;
    if (schemaKey != null && schemaKey != SchemaKey.UNKNOWN) {
      schema = BuiltinSchemas.getSchemaObject(schemaKey);
    }
    if (schema != null) {
      schema.validate(data.getContent());
    }
  }

  /**
   * 验证多模态数据
   * 迁移对应关系: Go语言entity.FieldSchema.validateMultiModalData()
   * - 功能: 验证多模态数据
   * - 参数: data - 字段数据
   */
  private void validateMultiModalData(FieldData data) {
    if (multiModelSpec == null) {
      return;
    }
    if (multiModelSpec.getMaxFileCount() > 0 &&
      multiModelSpec.getMaxFileCount() < data.getAttachments().size()) {
      throw new IllegalArgumentException(String.format(
        "file count out of range, max_file_count=%d, file_count=%d",
        multiModelSpec.getMaxFileCount(), data.getAttachments().size()));
    }
  }

  /**
   * 判断字段是否可用
   * 迁移对应关系: Go语言entity.FieldSchema.Available()
   * - 功能: 判断字段是否可用
   * - 返回: 是否可用
   */
  public boolean isAvailable() {
    return status == FieldStatus.AVAILABLE || status == null;
  }

  /**
   * 判断字段是否与另一个字段兼容
   * 迁移对应关系: Go语言entity.FieldSchema.CompatibleWith()
   * - 功能: 判断字段是否与另一个字段兼容
   * - 参数: other - 另一个字段Schema
   * - 返回: 是否兼容
   */
  public boolean isCompatibleWith(FieldSchema other) {
    if (contentType != other.contentType) {
      return false;
    }
    if (schemaKey != other.schemaKey && other.schemaKey != SchemaKey.UNKNOWN) {
      return false;
    }
    if (textSchema == null && other.textSchema != null) {
      return false;
    }
    if (textSchema != null && other.textSchema != null) {
      return textSchema.compatibleWith(other.textSchema);
    }
    return true;
  }
}
