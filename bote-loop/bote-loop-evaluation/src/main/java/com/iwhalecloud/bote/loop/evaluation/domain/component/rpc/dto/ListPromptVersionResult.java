package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页查询Prompt版本结果
 * 迁移对应关系: Go语言ListPromptVersionResult结构体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListPromptVersionResult {
  private List<CommitInfo> versions;
  private String nextCursor;
  private boolean hasMore;
}
