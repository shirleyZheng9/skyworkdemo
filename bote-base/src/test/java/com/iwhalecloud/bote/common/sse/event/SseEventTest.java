package com.iwhalecloud.bote.common.sse.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.dto.bot.RecommendedSceneDTO;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * SSE 事件类单元测试。
 *
 * <p>覆盖 {@link SseEvent} 的静态工厂方法（ofText/ofReasoning/ofReferences/ofQuestions/
 * ofKnowledgeChatLog/ofRecommendedScenes）以及各子类的 getMsgType/getMsgContent 与字段访问。</p>
 */
class SseEventTest {

  // ==================== 文本事件 ====================

  @Test
  void ofText_buildsTextEvent() {
    TextSseEvent event = SseEvent.ofText("hello");
    assertThat(event.getMsgType()).isEqualTo(ChatMessageType.TEXT);
    assertThat(event.getMsgContent()).isEqualTo("hello");
    assertThat(event.getText()).isEqualTo("hello");
  }

  // ==================== 推理事件 ====================

  @Test
  void ofReasoning_buildsReasoningEvent() {
    ReasoningSseEvent event = SseEvent.ofReasoning("thinking...");
    assertThat(event.getMsgType()).isEqualTo(ChatMessageType.REASONING);
    assertThat(event.getMsgContent()).isEqualTo("thinking...");
    assertThat(event.getText()).isEqualTo("thinking...");
  }

  // ==================== 参考文档事件 ====================

  @Test
  void ofReferences_buildsReferencesEvent() {
    List<ReferenceDocumentDTO> refs = Collections.emptyList();
    ReferencesSseEvent event = SseEvent.ofReferences(refs);
    assertThat(event.getMsgType()).isEqualTo(ChatMessageType.REFERENCES);
    assertThat(event.getMsgContent()).isSameAs(refs);
    assertThat(event.getReferences()).isSameAs(refs);
  }

  // ==================== 追问问题事件 ====================

  @Test
  void ofQuestions_buildsQuestionsEvent() {
    List<String> questions = List.of("q1", "q2");
    QuestionsSseEvent event = SseEvent.ofQuestions(questions);
    assertThat(event.getMsgType()).isEqualTo(ChatMessageType.QUESTIONS);
    assertThat(event.getMsgContent()).isSameAs(questions);
    assertThat(event.getQuestions()).isSameAs(questions);
  }

  // ==================== 对话日志事件 ====================

  @Test
  void ofKnowledgeChatLog_buildsChatLogEvent() {
    KnowledgeChatLogSseEvent event = SseEvent.ofKnowledgeChatLog("log-1");
    assertThat(event.getMsgType()).isEqualTo(ChatMessageType.KNOWLEDGE_CHAT_LOG);
    assertThat(event.getChatLogId()).isEqualTo("log-1");

    Object content = event.getMsgContent();
    assertThat(content).isInstanceOf(Map.class);
    assertThat(((Map<?, ?>) content).get("chatLogId")).isEqualTo("log-1");
  }

  // ==================== 推荐智能体事件 ====================

  @Test
  void ofRecommendedScenes_buildsRecommendedScenesEvent() {
    List<RecommendedSceneDTO> scenes = Collections.emptyList();
    RecommendedScenesSseEvent event = SseEvent.ofRecommendedScenes(scenes);
    assertThat(event.getMsgType()).isEqualTo(ChatMessageType.SELECT_SCENE);
    assertThat(event.getScenes()).isSameAs(scenes);

    Object content = event.getMsgContent();
    assertThat(content).isInstanceOf(Map.class);
    assertThat(((Map<?, ?>) content).get("scenes")).isSameAs(scenes);
  }

  // ==================== 工具调用事件 ====================

  @Test
  void toolCallEvent_buildsContentMap() {
    ToolCallEvent event = new ToolCallEvent("call-1", "search", "查天气", Map.of("city", "北京"));
    assertThat(event.getMsgType()).isEqualTo(ChatMessageType.TOOL_CALL);
    assertThat(event.getToolCallId()).isEqualTo("call-1");
    assertThat(event.getToolName()).isEqualTo("search");
    assertThat(event.getToolDescription()).isEqualTo("查天气");
    assertThat(event.getInput()).containsEntry("city", "北京");

    Object content = event.getMsgContent();
    assertThat(content).isInstanceOf(Map.class);
    Map<?, ?> map = (Map<?, ?>) content;
    assertThat(map.get("toolName")).isEqualTo("search");
    assertThat(map.get("toolDescription")).isEqualTo("查天气");
    assertThat(((Map<?, ?>) map.get("input")).get("city")).isEqualTo("北京");
  }

  @Test
  void toolCallEvent_supportsSetterOverrides() {
    ToolCallEvent event = new ToolCallEvent("call-1", "search", "desc", null);
    event.setToolCallId("call-2");
    event.setToolName("calc");
    event.setToolDescription("计算");
    event.setInput(Map.of("x", 1));
    assertThat(event.getToolCallId()).isEqualTo("call-2");
    assertThat(event.getToolName()).isEqualTo("calc");
    assertThat(event.getToolDescription()).isEqualTo("计算");
    assertThat(event.getInput()).containsEntry("x", 1);
  }

  // ==================== 工具调用结果事件 ====================

  @Test
  void toolCallResultEvent_buildsContentMap() {
    ToolCallResultEvent event = new ToolCallResultEvent("call-1", "search", "结果", Boolean.TRUE, 50L);
    assertThat(event.getMsgType()).isEqualTo(ChatMessageType.TOOL_CALL_RESULT);
    assertThat(event.getToolCallId()).isEqualTo("call-1");
    assertThat(event.getToolName()).isEqualTo("search");
    assertThat(event.getOutput()).isEqualTo("结果");
    assertThat(event.getSuccess()).isTrue();
    assertThat(event.getSpentTime()).isEqualTo(50L);

    Object content = event.getMsgContent();
    assertThat(content).isInstanceOf(Map.class);
    Map<?, ?> map = (Map<?, ?>) content;
    assertThat(map.get("toolName")).isEqualTo("search");
    assertThat(map.get("output")).isEqualTo("结果");
    assertThat(map.get("success")).isEqualTo(Boolean.TRUE);
    assertThat(map.get("spentTime")).isEqualTo(50L);
  }

  @Test
  void toolCallResultEvent_supportsSetterOverrides() {
    ToolCallResultEvent event = new ToolCallResultEvent("call-1", "search", null, Boolean.FALSE, null);
    event.setToolCallId("call-9");
    event.setToolName("calc");
    event.setOutput("ok");
    event.setSuccess(Boolean.TRUE);
    event.setSpentTime(99L);
    assertThat(event.getToolCallId()).isEqualTo("call-9");
    assertThat(event.getToolName()).isEqualTo("calc");
    assertThat(event.getOutput()).isEqualTo("ok");
    assertThat(event.getSuccess()).isTrue();
    assertThat(event.getSpentTime()).isEqualTo(99L);
  }

  // ==================== toString ====================

  @Test
  void toString_returnsReadableRepresentation() {
    // TextSseEvent / ReasoningSseEvent 带 @ToString，包含字段值
    assertThat(SseEvent.ofText("hi").toString()).contains("hi");
    assertThat(SseEvent.ofReasoning("think").toString()).contains("think");
    // KnowledgeChatLogSseEvent 未声明 @ToString，沿用父类 SseEvent 的 toString（无字段）
    assertThat(new KnowledgeChatLogSseEvent("log-1").toString()).isEqualTo("SseEvent()");
  }
}
