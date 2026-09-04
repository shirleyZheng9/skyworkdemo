package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建评估器请求
 * 对应Go: evaluator.CreateEvaluatorRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "创建评估器请求")
public class CreateEvaluatorRequest {

  /**
   * 评估器信息
   * 对应Go: Evaluator *evaluator.Evaluator
   */
  @Schema(description = "评估器信息")
  private EvaluatorDTO evaluator;

  /**
   * 客户端ID
   * 对应Go: Cid *string
   */
  @Schema(description = "客户端ID")
  private String cid;

  /**
   * 基础信息
   * 对应Go: Base *base.Base
   */
  @Schema(description = "基础信息")
  private Base base;
}
