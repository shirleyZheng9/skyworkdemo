package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段映射数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "字段映射数据传输对象")
public class FieldMappingDTO {

  @Schema(description = "字段名称")
  private String fieldName;

  @Schema(description = "常量值")
  private String constValue;

  @Schema(description = "来源字段名称")
  private String fromFieldName;
}
