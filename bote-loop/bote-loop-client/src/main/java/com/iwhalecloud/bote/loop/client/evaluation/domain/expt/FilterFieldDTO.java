package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 过滤字段数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "过滤字段数据传输对象")
public class FilterFieldDTO {

  @Schema(description = "字段类型")
  private FieldTypeDTO fieldType;

  @Schema(description = "字段键")
  private String fieldKey;
}
