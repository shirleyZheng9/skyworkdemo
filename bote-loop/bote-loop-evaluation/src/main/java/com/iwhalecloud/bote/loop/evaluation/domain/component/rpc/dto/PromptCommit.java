package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import lombok.Data;

/**
 * Prompt提交信息
 * 迁移对应关系: Go语言PromptCommit结构体
 */
@Data
public class PromptCommit {
  private PromptDetail detail;
  private CommitInfo commitInfo;
}
