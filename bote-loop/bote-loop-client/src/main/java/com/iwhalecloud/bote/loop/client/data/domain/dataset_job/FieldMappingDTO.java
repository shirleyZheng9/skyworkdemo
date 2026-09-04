package com.iwhalecloud.bote.loop.client.data.domain.dataset_job;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "列映射")
public class FieldMappingDTO {
  @Schema(description = "评测集列")
  private String source;
  @Schema(description = "导入数据列")
  private String target;
}
