package com.iwhalecloud.bote.dto.chat.agent;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作流 Agent 节点的回复部分抽象类
 *
 * @author bianjp
 * @since 2025-06-02
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@JsonTypeInfo(use = Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
  @Type(name = "assistant", value = AgentReplyAssistantPart.class),
  @Type(name = "toolCall", value = AgentReplyToolCallPart.class),
  @Type(name = "a2ui", value = AgentReplyA2uiPart.class)
})
public abstract class AgentReplyPart {
  /** 类型(assistant, toolCall) */
  protected final String type;
}
