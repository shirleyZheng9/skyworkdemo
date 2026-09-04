package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮次数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "轮次数据")
public class TurnDTO {

  @Schema(description = "轮次ID，如果是单轮评测集，id=0")
  private Long id;

  @Schema(description = "字段数据列表")
  private List<FieldDataDTO> fieldDataList;
}
