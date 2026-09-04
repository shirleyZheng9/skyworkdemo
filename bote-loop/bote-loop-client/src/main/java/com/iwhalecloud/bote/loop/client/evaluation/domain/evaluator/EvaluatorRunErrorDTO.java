package com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评测器运行错误数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluatorRunErrorDTO {

  @JsonProperty("code")
  private Integer code;

  @JsonProperty("message")
  private String message;
}
