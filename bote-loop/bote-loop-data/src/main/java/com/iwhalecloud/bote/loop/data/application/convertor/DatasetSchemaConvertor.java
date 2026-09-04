package com.iwhalecloud.bote.loop.data.application.convertor;

import com.iwhalecloud.bote.loop.client.data.domain.dataset.ContentTypeDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldDisplayFormatDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldSchemaDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldStatusDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.JSONSchemaDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.MultiModalSpecDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.SchemaKeyDTO;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ContentType;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldDisplayFormat;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldStatus;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JSONSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.MultiModalSpec;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.SchemaKey;
import com.iwhalecloud.bss.litchi.base.exception.BssException;

/**
 * 数据集模式转换器
 * 迁移对应关系: Go语言backend/modules/data/application/convertor/dataset/schema.go
 * - 功能: 数据集模式相关的DO和DTO转换
 * - 主要方法:
 * * fieldSchemaDO2DTO - 字段模式DO转DTO
 * * fieldSchemaDTO2DO - 字段模式DTO转DO
 * * multiModalSpecDO2DTO - 多模态规格DO转DTO
 * * multiModalSpecDTO2DO - 多模态规格DTO转DO
 * * contentTypeDO2DTO - 内容类型DO转DTO
 * * contentTypeDTO2DO - 内容类型DTO转DO
 * * fieldDisplayFormatDO2DTO - 字段显示格式DO转DTO
 * * fieldDisplayFormatDTO2DO - 字段显示格式DTO转DO
 * * schemaKeyDO2DTO - 模式键DO转DTO
 * * schemaKeyDTO2DO - 模式键DTO转DO
 * * fieldStatusDO2DTO - 字段状态DO转DTO
 * * fieldStatusDTO2DO - 字段状态DTO转DO
 * <p>
 * Java实现说明:
 * - 对应Go的schema.go文件
 * - 使用静态方法进行转换
 * - 处理所有枚举类型转换
 * - 包含JSON Schema解析逻辑
 * <p>
 * 技术栈迁移:
 * - Go枚举 -> Java枚举
 * - Go switch语句 -> Java switch表达式
 * - Go error返回 -> Java异常处理
 * - Go指针操作 -> Java对象操作
 */
public final class DatasetSchemaConvertor {

  private DatasetSchemaConvertor() {
    // 工具类，禁止实例化
  }

  /**
   * 字段模式DO转DTO
   * 迁移对应关系: Go语言FieldSchemaDO2DTO方法
   *
   * @param schema 字段模式DO
   * @return 字段模式DTO
   */
  public static FieldSchemaDTO fieldSchemaDO2DTO(FieldSchema schema) {
    if (schema == null) {
      return null;
    }

    FieldSchemaDTO dto = new FieldSchemaDTO();
    dto.setKey(schema.getKey());
    dto.setName(schema.getName());
    dto.setDescription(schema.getDescription());
    dto.setContentType(contentTypeDO2DTO(schema.getContentType()));
    dto.setDefaultFormat(fieldDisplayFormatDO2DTO(schema.getDefaultFormat()));
    dto.setSchemaKey(schemaKeyDO2DTO(schema.getSchemaKey()));
    dto.setStatus(fieldStatusDO2DTO(schema.getStatus()));
    dto.setHidden(schema.getHidden());
    dto.setIsRequired(schema.getIsRequired());

    if (schema.getTextSchema() != null) {
      JSONSchemaDTO jsonSchemaDTO = new JSONSchemaDTO();
      jsonSchemaDTO.setRaw(schema.getTextSchema().getRaw());
      dto.setTextSchema(jsonSchemaDTO);
    }

    if (schema.getMultiModelSpec() != null) {
      dto.setMultiModelSpec(multiModalSpecDO2DTO(schema.getMultiModelSpec()));
    }

    return dto;
  }

  /**
   * 字段模式DTO转DO
   * 迁移对应关系: Go语言FieldSchemaDTO2DO方法
   *
   * @param dto 字段模式DTO
   * @return 字段模式DO
   */
  public static FieldSchema fieldSchemaDTO2DO(FieldSchemaDTO dto) {
    if (dto == null) {
      return null;
    }

    FieldSchema schema = new FieldSchema();
    schema.setKey(dto.getKey());
    schema.setName(dto.getName());
    schema.setDescription(dto.getDescription());
    schema.setContentType(contentTypeDTO2DO(dto.getContentType()));
    schema.setDefaultFormat(fieldDisplayFormatDTO2DO(dto.getDefaultFormat()));
    schema.setSchemaKey(schemaKeyDTO2DO(dto.getSchemaKey()));
    schema.setMultiModelSpec(multiModalSpecDTO2DO(dto.getMultiModelSpec()));
    schema.setStatus(fieldStatusDTO2DO(dto.getStatus()));
    schema.setHidden(dto.getHidden());
    schema.setIsRequired(dto.getIsRequired());

    if (dto.getContentType() == ContentTypeDTO.TEXT &&
      dto.getTextSchema() != null && dto.getTextSchema().getRaw() != null && !dto.getTextSchema().getRaw().isEmpty()) {
      try {
        JSONSchema jsonSchema = JSONSchema.newJSONSchema(dto.getTextSchema().getRaw());
        schema.setTextSchema(jsonSchema);
      }
      catch (Exception e) {
        throw new BssException("parse text json schema failed: " + e.getMessage(), e);
      }
    }

    return schema;
  }

  /**
   * 多模态规格DO转DTO
   * 迁移对应关系: Go语言MultiModalSpecDO2DTO方法
   *
   * @param spec 多模态规格DO
   * @return 多模态规格DTO
   */
  public static MultiModalSpecDTO multiModalSpecDO2DTO(MultiModalSpec spec) {
    if (spec == null) {
      return null;
    }

    MultiModalSpecDTO dto = new MultiModalSpecDTO();
    dto.setMaxFileCount(spec.getMaxFileCount());
    dto.setMaxFileSize(spec.getMaxFileSize());
    dto.setSupportedFormats(spec.getSupportedFormats());
    return dto;
  }

  /**
   * 多模态规格DTO转DO
   * 迁移对应关系: Go语言MultiModalSpecDTO2DO方法
   *
   * @param dto 多模态规格DTO
   * @return 多模态规格DO
   */
  public static MultiModalSpec multiModalSpecDTO2DO(MultiModalSpecDTO dto) {
    if (dto == null) {
      return null;
    }

    MultiModalSpec spec = new MultiModalSpec();
    spec.setMaxFileCount(dto.getMaxFileCount());
    spec.setMaxFileSize(dto.getMaxFileSize());
    spec.setSupportedFormats(dto.getSupportedFormats());
    return spec;
  }

  /**
   * 内容类型DO转DTO
   * 迁移对应关系: Go语言ContentTypeDO2DTO方法
   *
   * @param contentType DO层内容类型
   * @return DTO层内容类型
   */
  public static ContentTypeDTO contentTypeDO2DTO(ContentType contentType) {
    if (contentType == null) {
      return null;
    }

    return switch (contentType) {
      case TEXT -> ContentTypeDTO.TEXT;
      case IMAGE -> ContentTypeDTO.IMAGE;
      case AUDIO -> ContentTypeDTO.AUDIO;
      case VIDEO -> ContentTypeDTO.AUDIO; // 注意：Go代码中VIDEO映射到AUDIO
      case MULTIPART -> ContentTypeDTO.MULTI_PART;
      default -> null;
    };
  }

  /**
   * 内容类型DTO转DO
   * 迁移对应关系: Go语言ContentTypeDTO2DO方法
   *
   * @param contentType DTO层内容类型
   * @return DO层内容类型
   */
  public static ContentType contentTypeDTO2DO(ContentTypeDTO contentType) {
    if (contentType == null) {
      return ContentType.UNKNOWN;
    }

    return switch (contentType) {
      case TEXT -> ContentType.TEXT;
      case IMAGE -> ContentType.IMAGE;
      case AUDIO -> ContentType.AUDIO;
      case MULTI_PART -> ContentType.MULTIPART;
      default -> ContentType.UNKNOWN;
    };
  }

  /**
   * 字段显示格式DO转DTO
   * 迁移对应关系: Go语言FieldDisplayFormatDO2DTO方法
   *
   * @param format DO层字段显示格式
   * @return DTO层字段显示格式
   */
  public static FieldDisplayFormatDTO fieldDisplayFormatDO2DTO(FieldDisplayFormat format) {
    if (format == null) {
      return null;
    }

    return switch (format) {
      case PLAIN_TEXT -> FieldDisplayFormatDTO.PLAIN_TEXT;
      case MARKDOWN -> FieldDisplayFormatDTO.MARKDOWN;
      case JSON -> FieldDisplayFormatDTO.JSON;
      case YAML -> FieldDisplayFormatDTO.YAML;
      case CODE -> FieldDisplayFormatDTO.CODE;
      default -> null;
    };
  }

  /**
   * 字段显示格式DTO转DO
   * 迁移对应关系: Go语言FieldDisplayFormatDTO2DO方法
   *
   * @param format DTO层字段显示格式
   * @return DO层字段显示格式
   */
  public static FieldDisplayFormat fieldDisplayFormatDTO2DO(FieldDisplayFormatDTO format) {
    if (format == null) {
      return FieldDisplayFormat.UNKNOWN;
    }

    return switch (format) {
      case PLAIN_TEXT -> FieldDisplayFormat.PLAIN_TEXT;
      case MARKDOWN -> FieldDisplayFormat.MARKDOWN;
      case JSON -> FieldDisplayFormat.JSON;
      case YAML -> FieldDisplayFormat.YAML;
      case CODE -> FieldDisplayFormat.CODE;
      default -> FieldDisplayFormat.UNKNOWN;
    };
  }

  /**
   * 模式键DO转DTO
   * 迁移对应关系: Go语言SchemaKeyDO2DTO方法
   *
   * @param schemaKey DO层模式键
   * @return DTO层模式键
   */
  public static SchemaKeyDTO schemaKeyDO2DTO(SchemaKey schemaKey) {
    if (schemaKey == null) {
      return null;
    }

    return switch (schemaKey) {
      case STRING -> SchemaKeyDTO.STRING;
      case INTEGER -> SchemaKeyDTO.INTEGER;
      case FLOAT -> SchemaKeyDTO.FLOAT;
      case BOOL -> SchemaKeyDTO.BOOL;
      case MESSAGE -> SchemaKeyDTO.MESSAGE;
      default -> null;
    };
  }

  /**
   * 模式键DTO转DO
   * 迁移对应关系: Go语言SchemaKeyDTO2DO方法
   *
   * @param schemaKey DTO层模式键
   * @return DO层模式键
   */
  public static SchemaKey schemaKeyDTO2DO(SchemaKeyDTO schemaKey) {
    if (schemaKey == null) {
      return SchemaKey.UNKNOWN;
    }

    return switch (schemaKey) {
      case STRING -> SchemaKey.STRING;
      case INTEGER -> SchemaKey.INTEGER;
      case FLOAT -> SchemaKey.FLOAT;
      case BOOL -> SchemaKey.BOOL;
      case MESSAGE -> SchemaKey.MESSAGE;
      default -> SchemaKey.UNKNOWN;
    };
  }

  /**
   * 字段状态DO转DTO
   * 迁移对应关系: Go语言FieldStatusDO2DTO方法
   *
   * @param status DO层字段状态
   * @return DTO层字段状态
   */
  public static FieldStatusDTO fieldStatusDO2DTO(FieldStatus status) {
    if (status == null) {
      return FieldStatusDTO.AVAILABLE;
    }

    return switch (status) {
      case AVAILABLE -> FieldStatusDTO.AVAILABLE;
      case DELETED -> FieldStatusDTO.DELETED;
      default -> FieldStatusDTO.AVAILABLE;
    };
  }

  /**
   * 字段状态DTO转DO
   * 迁移对应关系: Go语言FieldStatusDTO2DO方法
   *
   * @param status DTO层字段状态
   * @return DO层字段状态
   */
  public static FieldStatus fieldStatusDTO2DO(FieldStatusDTO status) {
    if (status == null) {
      return FieldStatus.UNKNOWN;
    }

    return switch (status) {
      case AVAILABLE -> FieldStatus.AVAILABLE;
      case DELETED -> FieldStatus.DELETED;
      default -> FieldStatus.UNKNOWN;
    };
  }
}
