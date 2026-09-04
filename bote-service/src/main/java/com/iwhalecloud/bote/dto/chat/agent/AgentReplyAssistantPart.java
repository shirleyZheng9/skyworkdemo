package com.iwhalecloud.bote.dto.chat.agent;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作流 Agent 节点的助手回复
 *
 * @author bianjp
 * @since 2025-06-02
 */
@Getter
@Setter
@ToString
public class AgentReplyAssistantPart extends AgentReplyPart {
  /** 思考内容 */
  private String reasoning;
  /** 文本内容 */
  private String content;

  public AgentReplyAssistantPart() {
    super("assistant");
  }

  public AgentReplyAssistantPart(String content) {
    this();
    this.content = content;
  }

  public AgentReplyAssistantPart(String content, String reasoning) {
    this();
    this.reasoning = reasoning;
    this.content = content;
  }
}
