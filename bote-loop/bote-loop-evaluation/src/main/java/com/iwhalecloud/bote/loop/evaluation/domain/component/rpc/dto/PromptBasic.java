package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import lombok.Data;

/**
 * Prompt基础信息
 * 迁移对应关系: Go语言PromptBasic结构体
 */
@Data
public class PromptBasic {
  private String displayName;
  private String description;
  private String latestVersion;
}
