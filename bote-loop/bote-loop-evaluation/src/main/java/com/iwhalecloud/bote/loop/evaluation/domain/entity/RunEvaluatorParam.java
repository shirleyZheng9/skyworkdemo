package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 运行评估器请求
 * 对应Go: RunEvaluatorRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunEvaluatorParam {

  private Long spaceId;
  private String name;
  private Long evaluatorVersionId;
  private EvaluatorInputData inputData;
  private Long experimentId;
  private Long experimentRunId;
  private Long itemId;
  private Long turnId;
  private Map<String, String> ext;
}
