package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段转换配置实体
 * 对应Go: FieldTransformationConfig
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldTransformationConfig {

  private FieldTransformationType transType;
  private Boolean global;
}
