package com.iwhalecloud.bote.service.orchestration.reply;

import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bote.dto.chat.AnswerDTO;
import com.iwhalecloud.bote.dto.chat.ReplyDTO;
import com.iwhalecloud.bote.dto.orchestration.step.ReplyStep.ContentTypeConfig;
import com.iwhalecloud.bote.dto.planning.PlanRecordDTO;
import java.util.Date;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 回复处理器
 *
 * @author bianjp
 * @since 2025-03-07
 */
public interface ReplyHandler {

  /**
   * 获取客户端 ID
   *
   * @return 客户端 ID
   */
  @Nullable
  String getClientId();

  /**
   * 获取回复列表
   *
   * @return 回复列表
   */
  List<ReplyDTO> getReplies();

  /**
   * 处理回复
   *
   * @param type 消息类型
   * @param content 消息内容
   * @param code 步骤编码
   * @param name 步骤名称
   */
  void reply(ChatMessageType type, Object content, @Nullable String code, @Nullable String name);

  /**
   * 处理回复文本
   *
   * @param type 消息类型
   * @param content 消息内容
   * @param code 步骤编码
   * @param name 步骤名称
   * @param msgId 消息 ID
   */
  void replyText(ChatMessageType type, Object content, String code, String name, @Nullable String msgId);

  /**
   * 处理回复关联的内容
   *
   * @param type 消息类型
   * @param content 消息内容
   * @param code 步骤编码
   * @param name 步骤名称
   */
  void replyRelated(ChatMessageType type, Object content, String code, String name);

  /**
   * 发送文件下载配置
   *
   * @param msgId 消息 ID
   * @param fileType 文件类型
   * @param content 内容
   */
  void download(Long tenantId, String msgId, String fileType, String content);

  /**
   * 发送内容输出格式
   */
  String sentContentType(ContentTypeConfig contentType);

  /**
   * 设置内容输出格式
   */
  void setContentType(String msgId, ContentTypeConfig contentType);

  /**
   * 异常情况下，记录已输出的文本信息
   */
  void addMessageWhenException(Date startTime, @Nullable String reasoning, @Nullable String text);

  /**
   * 推送消息，内容不记录
   */
  void pushMessage(ChatMessageType type, Object content);

  /**
   * 更新计划状态
   */
  void updatePlanState(PlanRecordDTO record);

  /**
   * 生成消息 ID
   */
  String newMsgId();

  /**
   * 发送流式输出的文本片段
   *
   * @param type 消息类型
   * @param msgId 消息 ID
   * @param partialText 片段
   */
  void sendStreamText(ChatMessageType type, String msgId, String partialText);

  /**
   * 发送消息，不记录
   *
   * @param type 消息类型
   * @param msgId 消息 ID
   * @param data 消息内容
   */
  void sendMessage(ChatMessageType type, String msgId, Object data);

  /**
   * 记录流式输出的完整消息
   *
   * <p>仅用于 A2A 智能体</p>
   *
   * @param type 消息类型
   * @param msgId 消息 ID
   * @param startTime 流式输出的开始时间
   * @param text 完整消息内容
   */
  void addStreamMessage(ChatMessageType type, String msgId, Date startTime, String text);

  /**
   * 处理流式回复
   *
   * @param code 步骤编码
   * @param name 步骤名称
   * @param msgId 消息 ID
   * @return 流式回复的完整内容
   */
  AnswerDTO stream(SseInvoker invoker, @Nullable String code, @Nullable String name, @Nullable String msgId);
}
