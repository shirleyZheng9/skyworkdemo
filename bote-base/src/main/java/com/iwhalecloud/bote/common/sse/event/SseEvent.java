package com.iwhalecloud.bote.common.sse.event;

import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.dto.bot.RecommendedSceneDTO;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * SSE 事件
 *
 * @author bianjp
 * @since 2025-01-07
 */
@Getter
@Setter
@ToString
public abstract class SseEvent {

  public abstract ChatMessageType getMsgType();

  public abstract Object getMsgContent();

  /**
   * 构造文本事件
   */
  public static TextSseEvent ofText(String text) {
    return new TextSseEvent(text);
  }

  /**
   * 构造推理内容事件
   */
  public static ReasoningSseEvent ofReasoning(String reasoning) {
    return new ReasoningSseEvent(reasoning);
  }

  /**
   * 构造知识库参考文档事件
   */
  public static ReferencesSseEvent ofReferences(List<ReferenceDocumentDTO> references) {
    return new ReferencesSseEvent(references);
  }

  /**
   * 构造知识库追问问题事件
   */
  public static QuestionsSseEvent ofQuestions(List<String> questions) {
    return new QuestionsSseEvent(questions);
  }

  /**
   * 构造知识库对话日志事件
   */
  public static KnowledgeChatLogSseEvent ofKnowledgeChatLog(String chatLogId) {
    return new KnowledgeChatLogSseEvent(chatLogId);
  }

  /**
   * 构造推荐智能体事件
   */
  public static RecommendedScenesSseEvent ofRecommendedScenes(List<RecommendedSceneDTO> scenes) {
    return new RecommendedScenesSseEvent(scenes);
  }
}
