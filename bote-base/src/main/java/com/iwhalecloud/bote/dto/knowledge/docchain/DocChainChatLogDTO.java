package com.iwhalecloud.bote.dto.knowledge.docchain;


import com.iwhalecloud.bote.llm.client.dto.message.Message;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * DocChain 对话日志
 *
 * @author bianjp
 * @since 2025-06-16
 */
@Getter
@Setter
@ToString
public class DocChainChatLogDTO {
  /** 问题 */
  private String question;
  /** 思考内容 */
  private String reasoningContent;
  /** 回答内容 */
  private String answerContent;
  /** 提示词 */
  private List<Message> messages;
}
