package com.iwhalecloud.bote.loop.client.data.domain.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段转换配置数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldTransformationConfigDTO {

  @JsonProperty("trans_type")
  private FieldTransformationTypeDTO transType;

  @JsonProperty("global")
  private Boolean global;
}
