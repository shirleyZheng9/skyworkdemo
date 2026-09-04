package com.iwhalecloud.bote.dto.beyond;

import com.iwhalecloud.bote.llm.client.dto.message.Message;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应会话请求对象
 *
 * @author bianjp
 * @since 2025-07-17
 */
@Getter
@Setter
@ToString
public class BeyondChatRequest {
  /** 会话 ID */
  private String sessionId;
  /** 数字员工 ID */
  private Long agentId;
  /** 客户端 ID(流式接口中断请求使用) */
  private String clientId;
  /** 用户消息内容 */
  private String chatContent;
  /** 文件列表 */
  private List<BeyondFileDTO> files;
  /** 扩展参数，用作上下文参数 */
  private Map<String, Object> extParam;
  /** 历史消息 */
  private List<Message> histories;
}
