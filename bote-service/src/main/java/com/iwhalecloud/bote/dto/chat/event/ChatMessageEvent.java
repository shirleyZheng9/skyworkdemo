package com.iwhalecloud.bote.dto.chat.event;

import com.iwhalecloud.bote.common.consts.ChatMessageType;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话消息事件
 *
 * @author Admin
 */
@Getter
@Setter
@ToString
public class ChatMessageEvent {
  /** 消息 ID */
  private Long msgId;
  /** 消息类型 */
  private ChatMessageType msgType;
  /** 消息内容 */
  private Object data;
  /** 关联消息 ID */
  private Long refMsgId;
  /** 流式消息开始时间 */
  private Date startTime;
  /** 创建时间 */
  private Date createTime;
  /** 是否允许记忆。用于页面、页面函数 */
  private boolean memorized;
  /** 记忆内容。用于页面、页面函数 */
  private Object memoryContent;
  /** 文件下载类型 */
  private String downloadType;
  /** 文件下载内容 */
  private String downloadContent;
  /** 输出内容格式 */
  private String contentType;
  /** 段落格式归属的文档 */
  private String paragraphGroup;
  /** 段落格式的序号 */
  private Integer paragraphSortby;
  /** 多智能体调度，不可直接使用会话上下文中的 sceneId */
  private Long sceneId;
  /** 多智能体调度，标记确认计划关联的计划 */
  private Long planId;

  public ChatMessageEvent(Long sceneId, Long msgId, ChatMessageType msgType, Object data) {
    this.sceneId = sceneId;
    this.msgId = msgId;
    this.msgType = msgType;
    this.data = data;
    this.createTime = new Date();
    this.memorized = msgType == ChatMessageType.INPUT || msgType == ChatMessageType.POINT || msgType == ChatMessageType.TEXT;
  }

  public ChatMessageEvent(Long sceneId, Long msgId, ChatMessageType msgType, Object data, Object memoryContent) {
    this(sceneId, msgId, msgType, data);
    this.memorized = true;
    this.memoryContent = memoryContent;
  }
}
