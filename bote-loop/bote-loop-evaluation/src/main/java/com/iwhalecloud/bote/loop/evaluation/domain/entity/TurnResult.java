package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮次结果实体
 * 对应Go: entity.TurnResult
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnResult {
  @Schema(description = "轮次ID")
  private Long turnId;
  @Schema(description = "参与对比的实验序列，对于单报告序列长度为1")
  private List<ExperimentResult> experimentResults;
  @Schema(description = "实验运行ID")
  private Long experimentRunId;
  @Schema(description = "轮次索引")
  private Long turnIndex;
  @Schema(description = "创建时间")
  private Date createdAt;
}
