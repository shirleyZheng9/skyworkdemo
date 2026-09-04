package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import lombok.Data;

/**
 * 批量获取Prompt查询条件
 * 迁移对应关系: Go语言MGetPromptQuery结构体
 */
@Data
public class MGetPromptQuery {
  private Long promptId;
  private String version;
}
