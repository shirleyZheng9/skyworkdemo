package com.iwhalecloud.bote.dto.chat.agent;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作流 Agent 节点的工具调用回复
 *
 * @author bianjp
 * @since 2025-06-02
 */
@Getter
@Setter
@ToString
public class AgentReplyToolCallPart extends AgentReplyPart {
  /** 工具名称 */
  private String toolName;
  /** 工具描述 */
  private String toolDescription;
  /** 入参 */
  private Object input;
  /** 出参 */
  private Object output;
  /** 是否成功 */
  private Boolean success;
  /** 耗时(ms) */
  private Long spentTime;

  public AgentReplyToolCallPart() {
    super("toolCall");
  }

  public AgentReplyToolCallPart(String toolName, String toolDescription, Object input, Object output, Boolean success, Long spentTime) {
    this();
    this.toolName = toolName;
    this.toolDescription = toolDescription;
    this.input = input;
    this.output = output;
    this.success = success;
    this.spentTime = spentTime;
  }
}
