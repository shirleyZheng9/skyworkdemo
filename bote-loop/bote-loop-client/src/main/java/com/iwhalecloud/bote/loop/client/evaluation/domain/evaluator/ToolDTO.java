package com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolDTO {

  @JsonProperty("type")
  private ToolTypeDTO type;

  @JsonProperty("function")
  private FunctionDTO function;
}
