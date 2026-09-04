package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ColumnEvalSetFieldDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ColumnEvaluatorDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ItemResultDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取实验结果响应
 * 对应Go: expt.BatchGetExperimentResultResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "批量获取实验结果响应")
public class BatchGetExperimentResultResponse {

  /**
   * 数据集表头信息
   * 对应Go: ColumnEvalSetFields []*expt.ColumnEvalSetField
   */
  @Schema(description = "数据集表头信息")
  private List<ColumnEvalSetFieldDTO> columnEvalSetFields;

  /**
   * 评估器表头信息
   * 对应Go: ColumnEvaluators []*expt.ColumnEvaluator
   */
  @Schema(description = "评估器表头信息")
  private List<ColumnEvaluatorDTO> columnEvaluators;

  /**
   * item粒度实验结果详情
   * 对应Go: ItemResults []*expt.ItemResult_
   */
  @Schema(description = "item粒度实验结果详情")
  private List<ItemResultDTO> itemResults;

  /**
   * 总数
   * 对应Go: Total *int64
   */
  @Schema(description = "总数")
  private Long total;

  /**
   * 基础响应信息
   * 对应Go: BaseResp *base.BaseResp
   */
  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
