package com.iwhalecloud.bote.dto.knowledge.docchain.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识问答的对话记录
 *
 * @author bianjp
 * @since 2025-06-16
 */
@Getter
@Setter
@ToString
@JsonNaming(SnakeCaseStrategy.class)
public class KnowledgeChatLogResponse {
  /** 问题 */
  private String queryText;
  /** 提示词 */
  private String chatPrompt;
  /** 回复 */
  private String chatResponse;
}
