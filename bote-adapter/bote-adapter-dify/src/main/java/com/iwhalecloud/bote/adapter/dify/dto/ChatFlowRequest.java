package com.iwhalecloud.bote.adapter.dify.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * dify chatFlow请求参数
 *
 * @author qian.sisheng
 * @since 2025-10-15
 */
@Setter
@Getter
@ToString
@JsonNaming(SnakeCaseStrategy.class)
public class ChatFlowRequest {
  /** 问题 */
  private String query;
  /** 响应模式 流式模式 streaming; blocking 阻塞模式，等待执行完毕后返回结果(100 秒超时无返回后中断) */
  private String responseMode;
  /** 用户标识，用于定义终端用户的身份，方便检索、统计。 由开发者定义规则，需保证用户标识在应用内唯一。 */
  private String user;
  /** 会话 ID，需要基于之前的聊天记录继续对话，必须传之前消息的 conversation_id。 */
  private String conversationId;
  /** 输入参数 */
  private Map<String, Object> inputs;
}
