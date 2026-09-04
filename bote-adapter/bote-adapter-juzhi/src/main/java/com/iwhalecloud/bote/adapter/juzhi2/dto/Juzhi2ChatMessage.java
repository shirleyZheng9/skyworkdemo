package com.iwhalecloud.bote.adapter.juzhi2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 二级聚智知识问答消息
 */
@Getter
@Setter
@ToString
public class Juzhi2ChatMessage {
  /** 消息角色 */
  private String role;
  /** 消息内容 */
  private String content;
  /** 消息内容类型 */
  @JsonProperty("content_type")
  private String contentType;

  public Juzhi2ChatMessage(MessageRole role, String content) {
    this.role = role.getCode();
    this.content = content;
    this.contentType = "text";
  }
}
