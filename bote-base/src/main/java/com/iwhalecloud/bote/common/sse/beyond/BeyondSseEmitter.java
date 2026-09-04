package com.iwhalecloud.bote.common.sse.beyond;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.common.enums.BeyondContentType;
import com.iwhalecloud.bote.common.enums.BeyondSseEvent;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.beyond.BeyondChatResponse;
import com.iwhalecloud.bote.dto.beyond.BeyondReferenceDocumentDTO;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 百应 SSE 触发器
 *
 * <p>用于转换事件，按百应的接口协议返回</p>
 *
 * @author bianjp
 * @since 2025-07-17
 */
public class BeyondSseEmitter extends SseEmitter {
  private static final Logger logger = LoggerFactory.getLogger(BeyondSseEmitter.class);
  /** 支持的事件列表 */
  private static final List<String> SUPPORTED_EVENTS = ImmutableList.of(
    ChatMessageType.QUESTIONS.getCode(),
    ChatMessageType.REFERENCES.getCode(),
    ChatMessageType.FILE_INFO.getCode(),
    ChatMessageType.REASONING.getCode(),
    ChatMessageType.TEXT.getCode(),
    ChatMessageType.RECOMMENDATION.getCode(),
    ChatMessageType.PAGE.getCode(),
    ChatMessageType.PAGE_FUNC.getCode(),
    ChatMessageType.AGENT_REPLY.getCode(),
    ChatMessageType.TOOL_CALL.getCode(),
    ChatMessageType.TOOL_CALL_RESULT.getCode(),
    ChatMessageType.DONE.getCode(),
    ChatMessageType.ERROR.getCode()
  );

  /** 上一条消息 ID, 用于判断是否生成了新回复，生成新回复时需要对上一条回复发送结束事件 */
  private String lastMsgId;
  /** 回复状态，用于特殊处理思考过程，发送思考过程的开始、结束事件 */
  private AnswerState answerState = AnswerState.INITIAL;
  /** 知识问答的追问问题列表 */
  private List<String> questions;
  /** 知识问答的参考文档列表 */
  private List<ReferenceDocumentDTO> references;
  /** 回复节点的附件列表 */
  private List<FileInfoDTO> fileInfos;

  public BeyondSseEmitter(Long timeout) {
    super(timeout);
  }

  @Override
  protected void extendResponse(ServerHttpResponse outputMessage) {
    super.extendResponse(outputMessage);
    // 避免 Nginx 缓冲
    //noinspection UastIncorrectHttpHeaderInspection
    outputMessage.getHeaders().set("X-Accel-Buffering", "no");
  }

  @Override
  public void send(Object object, @Nullable MediaType mediaType) throws IOException {
    send(new BeyondSseEventBuilder().data(object, mediaType));
  }

  @Override
  public void send(SseEventBuilder builder) throws IOException {
    Assert.isTrue(builder instanceof BeyondSseEventBuilder, "非法的 SSE 事件构造器类型");
    BeyondSseEventBuilder beyondBuilder = (BeyondSseEventBuilder) builder;
    String eventName = beyondBuilder.getEventName();
    // 只处理支持的事件
    if (!SUPPORTED_EVENTS.contains(eventName)) {
      return;
    }

    // 每条回复结束时要发送 answerEnd 事件
    if (lastMsgId == null) {
      lastMsgId = beyondBuilder.getId();
    }
    else if (!lastMsgId.equals(beyondBuilder.getId())) {
      lastMsgId = beyondBuilder.getId();
      super.send(event().name(BeyondSseEvent.ANSWER_END.getCode()).data("{\"content\":\"\",\"status\":\"success\"}", MediaType.TEXT_PLAIN));
    }

    // 转换事件
    convertEvent(eventName, beyondBuilder);
  }

  /**
   * 转换事件
   */
  @SuppressWarnings("unchecked")
  private void convertEvent(String eventName, BeyondSseEventBuilder beyondBuilder) throws IOException {
    SseEventBuilder newBuilder = event();
    ChatMessageType msgType = ChatMessageType.ofCode(eventName);
    switch (msgType) {
      case REASONING:
        handleReasoningEvent(beyondBuilder, newBuilder);
        break;
      case TEXT:
        handleTextEvent(beyondBuilder, newBuilder);
        break;
      case QUESTIONS:
        questions = (List<String>) beyondBuilder.getData();
        // 不需要发送事件
        return;
      case REFERENCES:
        references = (List<ReferenceDocumentDTO>) beyondBuilder.getData();
        // 不需要发送事件
        return;
      case FILE_INFO:
        fileInfos = (List<FileInfoDTO>) beyondBuilder.getData();
        // 不需要发送事件
        return;
      case RECOMMENDATION:
      case PAGE:
      case PAGE_FUNC:
      case AGENT_REPLY:
      case TOOL_CALL:
      case TOOL_CALL_RESULT:
        sendBoteCard(msgType, beyondBuilder, newBuilder);
        break;
      case DONE:
        newBuilder.name(BeyondSseEvent.END.getCode()).data(buildDoneEvent(), MediaType.APPLICATION_JSON);
        break;
      case ERROR:
        String errMsg = JsonUtil.parseJsonRequired((String) beyondBuilder.getData(), String.class);
        newBuilder.name(BeyondSseEvent.ERROR.getCode()).data(ImmutableMap.of("message", errMsg), MediaType.APPLICATION_JSON);
        break;
      default:
        // 不应该执行到这里
        logger.error("Unexpected SSE event type: {}", eventName);
        return;
    }
    super.send(newBuilder);
  }

  /**
   * 构造结束事件的数据
   */
  private Map<String, Object> buildDoneEvent() {
    // 构造关联资源列表
    List<BeyondReferenceDocumentDTO> relatedResources = new ArrayList<>();
    // 参考文档。只处理百应知识库返回的参考文档，原样返回
    if (CollectionUtils.isNotEmpty(references)) {
      references.stream().map(ReferenceDocumentDTO::getBeyondDocument).filter(Objects::nonNull).forEach(relatedResources::add);
    }
    // 附件
    if (CollectionUtils.isNotEmpty(fileInfos)) {
      // 下载文件接口地址
      String downloadFileApiUrl = StringUtils.stripEnd(BaseSystemParameter.BOTE_API_URL.getValueFromEnv(), "/") + "/bote/file/file/id/";
      for (FileInfoDTO file : fileInfos) {
        BeyondReferenceDocumentDTO document = new BeyondReferenceDocumentDTO();
        document.setDocumentUrl(downloadFileApiUrl + file.getFileId());
        document.setTitle(file.getFileName());
        document.setType("AGENT");
        relatedResources.add(document);
      }
    }

    Map<String, Object> data = new LinkedHashMap<>();
    if (lastMsgId != null) {
      data.put("messageId", lastMsgId);
    }
    if (CollectionUtils.isNotEmpty(questions)) {
      data.put("relatedQuestions", questions);
    }
    if (!relatedResources.isEmpty()) {
      data.put("relatedResources", relatedResources);
    }
    return data;
  }

  /**
   * 处理思考内容事件
   */
  private void handleReasoningEvent(BeyondSseEventBuilder builder, SseEventBuilder newBuilder) throws IOException {
    if (answerState != AnswerState.REASONING_STARTED) {
      // 发送思考过程开始事件
      sendReasoningEvent(BeyondSseEvent.REASONING_START.getCode());
      answerState = AnswerState.REASONING_STARTED;
    }

    String reasoningContent = JsonUtil.parseJsonRequired((String) builder.getData(), String.class);
    BeyondChatResponse response = BeyondChatResponse.builder()
      .id(builder.getId())
      .contentType(BeyondContentType.TEXT)
      .message(new AssistantMessage(reasoningContent))
      .build();
    newBuilder.name(BeyondSseEvent.REASONING_DELTA.getCode()).data(response, MediaType.APPLICATION_JSON);
  }

  /**
   * 发送思考过程开始、结束事件
   */
  private void sendReasoningEvent(String eventName) throws IOException {
    Map<String, Long> data = ImmutableMap.of("createTime", Instant.now().getEpochSecond());
    SseEventBuilder endEvent = event().name(eventName).data(data, MediaType.APPLICATION_JSON);
    super.send(endEvent);
  }

  /**
   * 处理文本事件
   */
  private void handleTextEvent(BeyondSseEventBuilder builder, SseEventBuilder newBuilder) throws IOException {
    // 发送思考过程结束事件
    if (answerState == AnswerState.REASONING_STARTED) {
      sendReasoningEvent(BeyondSseEvent.REASONING_END.getCode());
    }
    answerState = AnswerState.CONTENT_STARTED;
    String content = JsonUtil.parseJsonRequired((String) builder.getData(), String.class);
    BeyondChatResponse response = BeyondChatResponse.builder()
      .id(builder.getId())
      .contentType(BeyondContentType.TEXT)
      .message(new AssistantMessage(content))
      .build();
    newBuilder.name(BeyondSseEvent.ANSWER_DELTA.getCode()).data(response, MediaType.APPLICATION_JSON);
  }

  /**
   * 发送博特卡片消息
   */
  private void sendBoteCard(ChatMessageType msgType, BeyondSseEventBuilder builder, SseEventBuilder newBuilder) {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("id", builder.getId());
    data.put("tenantId", TenantIdUtil.getTenantId().toString());
    data.put("event", builder.getEventName());
    data.put("data", builder.getData());
    AssistantMessage message = new AssistantMessage(JsonUtil.toJsonString(data));
    BeyondChatResponse response = BeyondChatResponse.builder()
      .id(builder.getId())
      .contentType(msgType == ChatMessageType.PAGE_FUNC ? BeyondContentType.BOTE_PAGE_FUNC : BeyondContentType.BOTE)
      .message(message)
      .build();
    newBuilder.name(BeyondSseEvent.ANSWER_DELTA.getCode()).data(response, MediaType.APPLICATION_JSON);
  }

  /**
   * 回答状态
   */
  private enum AnswerState {
    /** 初始状态 */
    INITIAL,
    /** 思考已开始 */
    REASONING_STARTED,
    /** 正文已开始 */
    CONTENT_STARTED
  }
}
