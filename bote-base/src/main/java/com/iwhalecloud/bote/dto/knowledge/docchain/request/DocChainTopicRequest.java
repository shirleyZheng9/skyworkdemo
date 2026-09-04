package com.iwhalecloud.bote.dto.knowledge.docchain.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * DocChain 主题入参
 *
 * @author chen.linfa
 * @since 2025-03-10
 */
@Getter
@Setter
@ToString
public class DocChainTopicRequest {
  /** 主题 ID */
  private Long topicId;
  /** 主题名称 */
  private String topicName;
  /** 主题描述 */
  private String comment;
  /** 策略 */
  private String strategy;

  public DocChainTopicRequest(String topicName, String comment, String strategy) {
    this.topicName = topicName;
    this.comment = comment;
    this.strategy = strategy;
  }
}
