package com.iwhalecloud.bote.loop.evaluation.domain.repo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排序规则
 * 迁移对应关系: Go语言OrderBy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderBy {
  private String field;
  private Boolean byDesc;
}
