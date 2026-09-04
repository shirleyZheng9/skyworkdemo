package com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * WS /ws/complete 首帧请求体
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
@Getter
@Setter
@ToString
public class KnowledgeGraphWebsocketCompleteRequest {

  /** 知识库名称 */
  private String database;
  /** 问题 */
  private String question;
  /** 会话ID */
  @JsonProperty("session_id")
  private String sessionId;
  /** 提示词 */
  private String instruct;
}
