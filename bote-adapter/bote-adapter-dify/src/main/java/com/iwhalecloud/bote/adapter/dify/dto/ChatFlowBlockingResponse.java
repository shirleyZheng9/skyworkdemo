package com.iwhalecloud.bote.adapter.dify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.llm.client.dto.Usage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * chatFlow 阻塞响应
 *
 * @author qian.sisheng
 * @since 2025-10-15
 */

@Setter
@Getter
@ToString
public class ChatFlowBlockingResponse {
  /** 完整回复内容 */
  private String answer;
  /** 会话ID */
  @JsonProperty("conversation_id")
  private String conversationId;
  /** 响应ID */
  private String id;
  /** 元数据 */
  private Metadata metadata;
  /** 创建时间 */
  @JsonProperty("created_at")
  private Long createdAt;

  @Setter
  @Getter
  @ToString
  public static class Metadata {
    /** 计量信息 */
    private Usage usage;
  }

}
