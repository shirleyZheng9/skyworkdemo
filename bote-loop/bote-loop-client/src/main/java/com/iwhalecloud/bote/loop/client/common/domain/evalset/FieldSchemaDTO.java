package com.iwhalecloud.bote.loop.client.common.domain.evalset;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldDisplayFormatDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldStatusDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldTransformationConfigDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.MultiModalSpecDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.ContentTypeDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段Schema数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldSchemaDTO {

  /**
   * 唯一键
   */
  @JsonProperty("key")
  private String key;

  /**
   * 展示名称
   */
  @JsonProperty("name")
  private String name;

  /**
   * 描述
   */
  @JsonProperty("description")
  private String description;

  /**
   * 类型，如 文本，图片，etc.
   */
  @JsonProperty("content_type")
  private ContentTypeDTO contentType;

  /**
   * 默认渲染格式，如 code, json, etc.
   */
  @JsonProperty("default_display_format")
  private FieldDisplayFormatDTO defaultDisplayFormat;

  /**
   * 当前列的状态
   */
  @JsonProperty("status")
  private FieldStatusDTO status;

  /**
   * 是否必填
   */
  @JsonProperty("isRequired")
  private Boolean isRequired;

  /**
   * 内容格式限制相关
   */
  @JsonProperty("text_schema")
  private String textSchema;

  /**
   * 多模态规格限制
   */
  @JsonProperty("multi_model_spec")
  private MultiModalSpecDTO multiModelSpec;

  /**
   * 用户是否不可见
   */
  @JsonProperty("hidden")
  private Boolean hidden;

  /**
   * 默认的预置转换配置，目前在数据校验后执行
   */
  @JsonProperty("default_transformations")
  private List<FieldTransformationConfigDTO> defaultTransformations;
}
