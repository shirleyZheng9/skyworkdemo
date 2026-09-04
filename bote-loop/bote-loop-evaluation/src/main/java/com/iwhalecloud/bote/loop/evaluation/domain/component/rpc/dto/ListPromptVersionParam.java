package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import lombok.Data;

/**
 * 分页查询Prompt版本参数
 * 迁移对应关系: Go语言ListPromptVersionParam结构体
 */
@Data
public class ListPromptVersionParam {
  private Long promptId;
  private Long spaceId;
  private String cursor;
  private Integer pageSize;
}
