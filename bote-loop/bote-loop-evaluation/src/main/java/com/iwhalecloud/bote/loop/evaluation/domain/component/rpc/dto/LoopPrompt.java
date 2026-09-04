package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import lombok.Data;

/**
 * LoopPrompt实体
 * 迁移对应关系: Go语言LoopPrompt结构体
 */
@Data
public class LoopPrompt {
  private Long id;
  private String promptKey;
  private PromptBasic promptBasic;
  private PromptCommit promptCommit;
}
