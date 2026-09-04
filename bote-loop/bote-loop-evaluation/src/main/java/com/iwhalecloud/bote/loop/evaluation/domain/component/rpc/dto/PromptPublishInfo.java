package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import lombok.Data;

/**
 * Prompt发布信息
 * 迁移对应关系: Go语言PromptPublishInfo结构体
 */
@Data
public class PromptPublishInfo {
  private String publisher;
  private String publishDescription;
  private Long publishTsms;
}
