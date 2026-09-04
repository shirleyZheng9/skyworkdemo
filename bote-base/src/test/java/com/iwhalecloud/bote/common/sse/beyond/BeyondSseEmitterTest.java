package com.iwhalecloud.bote.common.sse.beyond;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * {@link BeyondSseEmitter} 单元测试。
 *
 * <p>仅验证本类自身的行为与内部状态：事件分支是否走对、私有字段（questions/references/fileInfos/
 * lastMsgId/answerState）是否被正确赋值。发送动作通过反射注入 no-op Handler 兜底，避免依赖
 * Spring 的 handler 初始化与 SSE 报文格式，不断言框架内部输出。</p>
 *
 * <p>sendBoteCard 与 buildDoneEvent 的 fileInfos 分支会触发 TenantIdUtil/BaseSystemParameter 的静态
 * 加载，为避免与其他测试类的静态状态互相污染而暂不覆盖。</p>
 *
 * <p>text/reasoning/error 分支依赖 JsonUtil，其静态初始化需通过 SpringUtil.getBean 获取 ObjectMapper，
 * 故在 @BeforeAll 中以 mockStatic(SpringUtil) 注入真实 ObjectMapper 供 JsonUtil 加载使用。</p>
 */
class BeyondSseEmitterTest {

  private static MockedStatic<SpringUtil> spring;

  @BeforeAll
  static void setUp() {
    spring = mockStatic(SpringUtil.class);
    // JsonUtil 静态初始化时通过 SpringUtil.getBean(ObjectMapper.class, Supplier) 获取 ObjectMapper，
    // 注入真实实例以使 text/reasoning/error 分支的 JsonUtil.parseJsonRequired 可正常工作
    spring.when(() -> SpringUtil.getBean(eq(ObjectMapper.class), any())).thenReturn(new ObjectMapper());
  }

  @AfterAll
  static void tearDown() {
    spring.close();
  }

  private BeyondSseEmitter newEmitter() throws Exception {
    BeyondSseEmitter emitter = new BeyondSseEmitter(60_000L);
    setNoopHandler(emitter);
    return emitter;
  }

  /** 注入一个 no-op Handler，使 super.send 不致因 handler 为 null 而抛异常。 */
  private void setNoopHandler(SseEmitter emitter) throws Exception {
    Class<?> handlerClass =
        Class.forName("org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter$Handler");
    Object handler = mock(handlerClass);
    Field f = ResponseBodyEmitter.class.getDeclaredField("handler");
    f.setAccessible(true);
    f.set(emitter, handler);
  }

  private Object field(BeyondSseEmitter emitter, String name) throws Exception {
    Field f = BeyondSseEmitter.class.getDeclaredField(name);
    f.setAccessible(true);
    return f.get(emitter);
  }

  private BeyondSseEventBuilder event(String eventName, Object data) {
    BeyondSseEventBuilder builder = new BeyondSseEventBuilder();
    builder.name(eventName);
    builder.data(data);
    return builder;
  }

  private BeyondSseEventBuilder event(String id, String eventName, Object data) {
    BeyondSseEventBuilder builder = new BeyondSseEventBuilder();
    builder.id(id);
    builder.name(eventName);
    builder.data(data);
    return builder;
  }

  // ==================== 非发送分支 ====================

  @Test
  void unsupportedEvent_returnsWithoutException() throws Exception {
    BeyondSseEmitter emitter = newEmitter();
    assertThatCode(() -> emitter.send(event("not-supported", "x"))).doesNotThrowAnyException();
  }

  @Test
  void twoArgSend_routesToNamelessBuilderAndReturns() throws Exception {
    BeyondSseEmitter emitter = newEmitter();
    // 2 参 send 包装为无事件名的 BeyondSseEventBuilder，命中“不支持事件”分支直接返回
    assertThatCode(() -> emitter.send("payload", null)).doesNotThrowAnyException();
  }

  @Test
  void questionsEvent_storesQuestionsWithoutSending() throws Exception {
    BeyondSseEmitter emitter = newEmitter();
    List<String> questions = List.of("q1", "q2");
    emitter.send(event(ChatMessageType.QUESTIONS.getCode(), questions));
    assertThat(field(emitter, "questions")).isSameAs(questions);
  }

  @Test
  void referencesEvent_storesReferencesWithoutSending() throws Exception {
    BeyondSseEmitter emitter = newEmitter();
    List<ReferenceDocumentDTO> refs = List.of(mock(ReferenceDocumentDTO.class));
    emitter.send(event(ChatMessageType.REFERENCES.getCode(), refs));
    assertThat(field(emitter, "references")).isSameAs(refs);
  }

  @Test
  void fileInfoEvent_storesFileInfosWithoutSending() throws Exception {
    BeyondSseEmitter emitter = newEmitter();
    List<FileInfoDTO> files = List.of(mock(FileInfoDTO.class));
    emitter.send(event(ChatMessageType.FILE_INFO.getCode(), files));
    assertThat(field(emitter, "fileInfos")).isSameAs(files);
  }

  @Test
  void firstMessage_setsLastMsgId() throws Exception {
    BeyondSseEmitter emitter = newEmitter();
    // QUESTIONS 为支持事件：先赋值 lastMsgId，随后 convertEvent 仅存储 questions 不触发发送
    // （不支持的事件名在赋值 lastMsgId 之前即提前 return，无法覆盖该分支）
    emitter.send(event("m1", ChatMessageType.QUESTIONS.getCode(), List.of("q1")));
    assertThat(field(emitter, "lastMsgId")).isEqualTo("m1");
  }

  // ==================== 发送分支（no-op handler 兜底） ====================

  @Test
  void textEvent_setsContentStartedState() throws Exception {
    BeyondSseEmitter emitter = newEmitter();
    assertThatCode(() -> emitter.send(event("m1", ChatMessageType.TEXT.getCode(), "\"hello\"")))
        .doesNotThrowAnyException();
    assertThat(((Enum<?>) field(emitter, "answerState")).name()).isEqualTo("CONTENT_STARTED");
  }

  @Test
  void reasoningEvent_setsReasoningStartedState() throws Exception {
    BeyondSseEmitter emitter = newEmitter();
    assertThatCode(() -> emitter.send(event("m1", ChatMessageType.REASONING.getCode(), "\"think\"")))
        .doesNotThrowAnyException();
    assertThat(((Enum<?>) field(emitter, "answerState")).name()).isEqualTo("REASONING_STARTED");
  }

  @Test
  void reasoningThenText_transitionsToContentStarted() throws Exception {
    BeyondSseEmitter emitter = newEmitter();
    emitter.send(event("m1", ChatMessageType.REASONING.getCode(), "\"r\""));
    assertThat(((Enum<?>) field(emitter, "answerState")).name()).isEqualTo("REASONING_STARTED");

    // 文本事件应先发送思考结束事件，再切换到正文已开始状态
    assertThatCode(() -> emitter.send(event("m1", ChatMessageType.TEXT.getCode(), "\"t\"")))
        .doesNotThrowAnyException();
    assertThat(((Enum<?>) field(emitter, "answerState")).name()).isEqualTo("CONTENT_STARTED");
  }

  @Test
  void secondMessage_differentId_updatesLastMsgId() throws Exception {
    BeyondSseEmitter emitter = newEmitter();
    emitter.send(event("m1", ChatMessageType.TEXT.getCode(), "\"a\""));
    // 不同 ID 触发发送 answerEnd 事件并更新 lastMsgId
    assertThatCode(() -> emitter.send(event("m2", ChatMessageType.TEXT.getCode(), "\"b\"")))
        .doesNotThrowAnyException();
    assertThat(field(emitter, "lastMsgId")).isEqualTo("m2");
  }

  @Test
  void doneEvent_empty_executesWithoutException() throws Exception {
    BeyondSseEmitter emitter = newEmitter();
    assertThatCode(() -> emitter.send(event(ChatMessageType.DONE.getCode(), "x")))
        .doesNotThrowAnyException();
  }

  @Test
  void doneEvent_withStoredQuestionsAndReferences_executesWithoutException() throws Exception {
    BeyondSseEmitter emitter = newEmitter();
    emitter.send(event(ChatMessageType.QUESTIONS.getCode(), List.of("q1")));
    ReferenceDocumentDTO ref = mock(ReferenceDocumentDTO.class);
    when(ref.getBeyondDocument()).thenReturn(mock(
        com.iwhalecloud.bote.dto.beyond.BeyondReferenceDocumentDTO.class));
    emitter.send(event(ChatMessageType.REFERENCES.getCode(), List.of(ref)));

    assertThatCode(() -> emitter.send(event(ChatMessageType.DONE.getCode(), "x")))
        .doesNotThrowAnyException();
  }

  @Test
  void errorEvent_executesWithoutException() throws Exception {
    BeyondSseEmitter emitter = newEmitter();
    assertThatCode(() -> emitter.send(event(ChatMessageType.ERROR.getCode(), "\"err msg\"")))
        .doesNotThrowAnyException();
  }

  @Test
  void assertionError_forNonBeyondBuilder() {
    // send(SseEventBuilder) 断言构造器类型必须是 BeyondSseEventBuilder
    BeyondSseEmitter emitter = new BeyondSseEmitter(60_000L);
    org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder other =
        SseEmitter.event();
    assertThatCode(() -> emitter.send(other))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("SSE");
  }
}
