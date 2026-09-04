package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM调用参数
 * 对应Go: LLMCallParam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LLMCallParam {

  private Long spaceId;
  private String evaluatorId;
  private String userId;
  private Scenario scenario;
  private List<Message> messages;
  private List<Tool> tools;
  private ToolCallConfig toolCallConfig;
  private ModelConfig modelConfig;
}
