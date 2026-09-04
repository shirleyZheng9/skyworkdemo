package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import lombok.Data;

/**
 * 获取Prompt参数
 * 迁移对应关系: Go语言GetPromptParams结构体
 */
@Data
public class GetPromptParams {
  private String commitVersion;
}
