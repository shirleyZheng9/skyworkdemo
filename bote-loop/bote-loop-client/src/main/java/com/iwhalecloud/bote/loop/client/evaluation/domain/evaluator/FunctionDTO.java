package com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 函数数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunctionDTO {

  @JsonProperty("name")
  private String name;

  @JsonProperty("description")
  private String description;

  @JsonProperty("parameters")
  private String parameters;
}
