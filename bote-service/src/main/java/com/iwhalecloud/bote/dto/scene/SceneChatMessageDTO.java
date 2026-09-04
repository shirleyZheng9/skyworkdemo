package com.iwhalecloud.bote.dto.scene;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ToolMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 场景会话消息
 *
 * @author bianjp
 * @since 2024-08-06
 */
@Getter
@Setter
@ToString
public class SceneChatMessageDTO {
  /** 消息顺序 */
  private Integer seq;
  /** 消息角色 */
  private String messageRole;
  /** 消息内容 */
  private String messageContent;
  /** 工具调用标识 */
  private String toolCallId;
  /** 工具名称 */
  private String toolName;
  /** 工具参数 */
  private String toolArguments;

  /**
   * 是否是工具消息
   */
  @JsonIgnore
  public boolean isTool() {
    return MessageRole.TOOL.getCode().equals(messageRole);
  }

  /**
   * 是否是助手消息
   */
  @JsonIgnore
  public boolean isAssistant() {
    return MessageRole.ASSISTANT.getCode().equals(messageRole);
  }

  /**
   * 是否有工具调用
   */
  @JsonIgnore
  public boolean hasToolCall() {
    return isAssistant() && StringUtils.isNotEmpty(toolCallId);
  }

  /**
   * 转为大模型的消息对象
   */
  public Message toMessage() {
    MessageRole role = MessageRole.valueOf(messageRole.toUpperCase());
    switch (role) {
      case SYSTEM:
        return new SystemMessage(messageContent);
      case USER:
        return new UserMessage(messageContent);
      case ASSISTANT:
        if (StringUtils.isNotEmpty(toolCallId)) {
          return new AssistantMessage(new ToolCall(toolCallId, toolName, toolArguments));
        }
        // 避免 content=null 导致调用外部接口时报错
        return new AssistantMessage(StringUtils.defaultString(messageContent));
      case TOOL:
        return new ToolMessage(toolCallId, messageContent);
      default:
        throw new IllegalStateException("未知角色");
    }
  }
}
