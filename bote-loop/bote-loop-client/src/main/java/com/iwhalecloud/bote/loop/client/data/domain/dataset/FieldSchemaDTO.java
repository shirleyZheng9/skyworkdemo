package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Field Schema DTO
 * 对应Go: FieldSchema
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "字段Schema数据传输对象")
public class FieldSchemaDTO {

  @Schema(description = "唯一键")
  private String key;

  @Schema(description = "展示名称")
  private String name;

  @Schema(description = "描述")
  private String description;

  @Schema(description = "内容类型")
  private ContentTypeDTO contentType;

  @Schema(description = "默认格式")
  private FieldDisplayFormatDTO defaultFormat;

  @Schema(description = "Schema键")
  private SchemaKeyDTO schemaKey;

  @Schema(description = "文本Schema")
  private JSONSchemaDTO textSchema;

  @Schema(description = "多模态规格")
  private MultiModalSpecDTO multiModelSpec;

  @Schema(description = "状态")
  private FieldStatusDTO status;

  @Schema(description = "是否隐藏")
  private Boolean hidden;

  @Schema(description = "是否必填")
  private Boolean isRequired;
}
