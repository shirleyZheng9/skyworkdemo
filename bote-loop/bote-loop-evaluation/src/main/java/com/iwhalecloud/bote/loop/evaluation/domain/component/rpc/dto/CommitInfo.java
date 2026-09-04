package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import lombok.Data;

/**
 * 提交信息
 * 迁移对应关系: Go语言CommitInfo结构体
 */
@Data
public class CommitInfo {
  private String version;
  private String baseVersion;
  private String description;
  private String committedBy;
  private Long committedAt;
}
