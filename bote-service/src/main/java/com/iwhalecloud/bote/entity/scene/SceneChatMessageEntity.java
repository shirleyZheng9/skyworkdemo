package com.iwhalecloud.bote.entity.scene;

import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ToolMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import java.util.Date;
import java.util.Objects;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 场景会话消息
 *
 * @author bianjp
 * @since 2024-08-06
 */
@Getter
@Setter
@ToString
@Table(name = "bt_chat_message")
public class SceneChatMessageEntity {
  /** 消息标识 */
  private Long messageId;
  /** 场景标识 */
  private Long sceneId;
  /** 对话标识 */
  private Long conversationId;
  /** 场景会话标识 */
  private String contextId;
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
  /** 登录用户 ID */
  private Long userId;
  /** 创建时间 */
  private Date createTime;
  /** 更新时间 */
  private Date updateTime;

  /**
   * 设置消息内容
   */
  public void setMessage(Message message) {
    this.messageRole = message.getRole().getCode();
    switch (message.getRole()) {
      case SYSTEM:
        this.messageContent = ((SystemMessage) message).getContent();
        break;
      case USER:
        this.messageContent = Objects.toString(((UserMessage) message).getContent(), null);
        break;
      case ASSISTANT:
        if (((AssistantMessage) message).hasToolCall()) {
          ToolCall toolCall = ((AssistantMessage) message).getToolCall();
          this.toolCallId = toolCall.getId();
          this.toolName = toolCall.getFunction().getName();
          this.toolArguments = toolCall.getFunction().getArguments();
        }
        else {
          this.messageContent = ((AssistantMessage) message).getContent();
        }
        break;
      case TOOL:
        this.messageContent = ((ToolMessage) message).getContent();
        this.toolCallId = ((ToolMessage) message).getToolCallId();
        break;
      default:
        throw new IllegalStateException("未知角色: " + message.getRole());
    }
  }
}
