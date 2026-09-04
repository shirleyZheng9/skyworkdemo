package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import lombok.Data;

/**
 * Prompt版本信息
 * 迁移对应关系: Go语言PromptVersionInfo结构体
 */
@Data
public class PromptVersionInfo {
  private String version;
  private String description;
  private Long committedAt;
  private String committedBy;
}
