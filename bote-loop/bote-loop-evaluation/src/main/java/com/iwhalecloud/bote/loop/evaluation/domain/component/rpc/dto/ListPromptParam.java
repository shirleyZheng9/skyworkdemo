package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import lombok.Data;

/**
 * 分页查询Prompt参数
 * 迁移对应关系: Go语言ListPromptParam结构体
 */
@Data
public class ListPromptParam {
  private Long spaceId;
  private Integer page;
  private Integer pageSize;
  private String keyWord;
}
