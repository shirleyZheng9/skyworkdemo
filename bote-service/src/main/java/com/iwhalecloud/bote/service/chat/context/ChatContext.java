package com.iwhalecloud.bote.service.chat.context;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.consts.GeneralAgentConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.dto.chat.AnswerDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestMessageDTO;
import com.iwhalecloud.bote.dto.chat.ChatTraceLogDTO;
import com.iwhalecloud.bote.dto.chat.ChatTraceLogDTO.ChatTraceLogBuilder;
import com.iwhalecloud.bote.dto.chat.SessionMsgDTO;
import com.iwhalecloud.bote.dto.chat.SessionMsgExtParamsDTO;
import com.iwhalecloud.bote.dto.chat.event.ChatMessageEvent;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import com.iwhalecloud.bote.dto.orchestration.step.ReplyStep.ContentTypeConfig;
import com.iwhalecloud.bote.dto.planning.SimplePlanDTO;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import com.iwhalecloud.bote.mapper.chat.SessionMsgMapper;
import com.iwhalecloud.bote.service.chat.IChatSessionService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 会话上下文
 *
 * @author bianjp
 * @since 2025-01-03
 */
@Getter
@Setter
@ToString
public class ChatContext {
  private static final Logger logger = LoggerFactory.getLogger(ChatContext.class);
  private static final SessionMsgMapper sessionMsgMapper = SpringUtil.getBean(SessionMsgMapper.class);
  private static final IChatSessionService chatSessionService = SpringUtil.getBean(IChatSessionService.class);

  /** 请求 */
  private final ChatRequestDTO request;
  /** SSE 触发器 */
  private final SseEmitter emitter;
  /** 租户 ID */
  private final Long tenantId;
  /** 用户 ID */
  private final Long userId;
  /** 会话 ID (对应会话列表中的一组会话) */
  private final Long sessionId;
  /** 事务 ID (对应一轮对话，一问一答) */
  private final Long transactionId;
  /** 上下文 ID (对应会话中的一个场景，可以包含多轮对话) */
  private String contextId;
  /** 处理本次对话的场景 ID */
  private Long sceneId;
  /** 处理本次对话的场景名称 */
  private String sceneName;
  /** 处理本次对话的机器人 ID */
  private Long botId;
  /** 计划 ID */
  private Long planId;
  /** 客户端 ID */
  private String clientId;
  /** 用户消息 ID */
  private Long userMsgId;
  /** 是否是 claw。包括运行态的 BoteClaw, 配置态的 claw 类型智能体 */
  private Boolean isClaw;
  /** 消息事件列表 */
  private final List<ChatMessageEvent> chatMessageEvents = new ArrayList<>();
  /** 日志列表 */
  private List<ChatTraceLogBuilder> traceLogBuilders = new ArrayList<>();
  /** 开始时间 */
  private final Date startTime = new Date();
  /** 是否已完成 */
  private volatile boolean isComplete = false;

  public ChatContext(ChatRequestDTO request, SseEmitter emitter, Long userId) {
    this.request = request;
    this.emitter = emitter;
    this.tenantId = request.getTenantId();
    this.botId = request.getBotId();
    this.userId = userId;
    this.sessionId = request.getSessionId() != null ? request.getSessionId() : ChatConsts.DEFAULT_SESSION_ID;
    this.transactionId = IDUtils.nextId();
    this.contextId = StringUtils.isNotEmpty(request.getContextId()) ? request.getContextId() : null;
    this.planId = request.getPlanParams() != null ? request.getPlanParams().getPlanId() : null;
    this.clientId = request.getClientId();
  }

  /**
   * 检查当前会话是否是子智能体调用
   */
  public boolean isSubagent() {
    return contextId != null && contextId.startsWith(GeneralAgentConsts.SUBAGENT_CONTEXT_ID_PREFIX);
  }

  /**
   * 获取用户消息
   */
  public ChatRequestMessageDTO getUserMessage() {
    return request.getMessage();
  }

  /**
   * 构造新日志
   */
  public ChatTraceLogBuilder newLog(String stepName) {
    ChatTraceLogBuilder builder = ChatTraceLogDTO.builder(stepName);
    traceLogBuilders.add(builder);
    return builder;
  }

  /**
   * 生成新消息 ID
   *
   * @return 消息 ID
   */
  public Long newMsgId() {
    return Sequences.BOT_SESSION_MSG_ID.next();
  }

  /**
   * 发送上下文和场景信息
   */
  public void sendSceneInfoMessage() {
    Map<String, Object> sceneInfo = new LinkedHashMap<>();
    sceneInfo.put("sceneId", sceneId);
    sceneInfo.put("sceneName", sceneName);
    SseUtil.sendText(emitter, ChatMessageType.CONTEXT_ID, contextId);
    SseUtil.sendJson(emitter, ChatMessageType.SCENE, sceneInfo);
  }

  /**
   * 添加错误信息，仅记录，不发给前端
   */
  public void addErrorMessage(String msg) {
    this.chatMessageEvents.add(new ChatMessageEvent(sceneId, newMsgId(), ChatMessageType.ERROR, msg));
  }

  /**
   * 发送消息
   *
   * @param msgType 消息类型
   * @param msgContent 消息内容
   * @return 消息 ID, 不需要记录的消息返回 null
   */
  @SuppressWarnings("unchecked")
  public String sendMessage(ChatMessageType msgType, Object msgContent) {
    // 有些消息不需要记录
    if (!msgType.isPersist()) {
      SseUtil.sendJson(emitter, msgType, msgContent);
      return null;
    }
    Long msgId = newMsgId();
    // 页面、页面函数开启记忆时需要特殊处理
    if (msgType == ChatMessageType.PAGE || msgType == ChatMessageType.PAGE_FUNC) {
      Map<String, Object> map = (Map<String, Object>) msgContent;
      if (Boolean.TRUE.equals(map.get("memorized"))) {
        Object memoryContent = map.get("memoryContent");
        // memorized, memoryContent 会存储到单独的字段中，应避免重复存储到 msg_text 字段中
        // 但 memorized 需要发送给前端，不然前端无法判断是否开启了记忆
        map.remove("memoryContent");
        Map<String, Object> finalMsgContent = new LinkedHashMap<>(map);
        map.remove("memorized");
        this.chatMessageEvents.add(new ChatMessageEvent(sceneId, msgId, msgType, msgContent, memoryContent));
        SseUtil.sendJson(emitter, msgType, msgId.toString(), finalMsgContent);
        return msgId.toString();
      }
    }
    this.chatMessageEvents.add(createMessageEvent(msgType, msgContent, msgId));
    SseUtil.sendJson(emitter, msgType, msgId.toString(), msgContent);
    return msgId.toString();
  }

  /**
   * 发送文本消息
   *
   * @param msgType 消息类型
   * @param msgContent 消息内容
   * @return 消息 ID, 不需要记录的消息返回 null
   */
  public String sendTextMessage(ChatMessageType msgType, Object msgContent, String msgId) {
    Long id = StringUtils.isEmpty(msgId) ? newMsgId() : Long.valueOf(msgId);
    ChatMessageEvent event = new ChatMessageEvent(sceneId, id, msgType, msgContent);
    this.chatMessageEvents.add(event);
    SseUtil.sendJson(emitter, msgType, id.toString(), msgContent);
    return id.toString();
  }

  /**
   * 设置回复的下载配置
   */
  public void setDownloadInfo(Long msgId, String fileType, String content) {
    ChatMessageEvent event = IterableUtils.find(chatMessageEvents, e -> msgId.equals(e.getMsgId()));
    if (event != null) {
      event.setDownloadType(fileType);
      event.setDownloadContent(content);
    }
  }

  /**
   * 设置回复的输出内容格式
   */
  public void setContentType(Long msgId, ContentTypeConfig contentType) {
    ChatMessageEvent event = IterableUtils.find(chatMessageEvents, e -> msgId.equals(e.getMsgId()));
    if (event != null) {
      event.setContentType(contentType.getContentType());
      event.setParagraphGroup(contentType.getGroup());
      event.setParagraphSortby(StringUtils.isNumeric(contentType.getSortby()) ? Integer.parseInt(contentType.getSortby()) : null);
    }
  }

  /**
   * 记录流式输出的消息
   *
   * @param msgId 消息 ID
   * @param startTime 开始时间
   * @param answer 回复内容
   */
  public void addStreamMessage(Long msgId, Date startTime, AnswerDTO answer) {
    List<ChatMessageEvent> events = new ArrayList<>();
    ChatMessageEvent event = new ChatMessageEvent(sceneId, msgId, ChatMessageType.TEXT, answer.getText());
    event.setStartTime(startTime);

    // 推理内容，先于文本记录
    if (StringUtils.isNotEmpty(answer.getReasoning())) {
      ChatMessageEvent reasoningEvent = new ChatMessageEvent(sceneId, newMsgId(), ChatMessageType.REASONING, answer.getReasoning());
      reasoningEvent.setRefMsgId(msgId);
      events.add(reasoningEvent);
    }

    // TODO 暂时特殊处理，后续需要优化判断方式
    if (answer.getMemoryContent() != null) {
      event.setMsgType(ChatMessageType.AGENT_REPLY);
      event.setMemorized(true);
      event.setMemoryContent(answer.getMemoryContent());
    }
    events.add(event);

    // 参考文档、追问需要分别记录一条消息
    List<ReferenceDocumentDTO> references = answer.getReferences();
    if (CollectionUtils.isNotEmpty(references)) {
      ChatMessageEvent referencesEvent = new ChatMessageEvent(sceneId, newMsgId(), ChatMessageType.REFERENCES, references);
      referencesEvent.setRefMsgId(msgId);
      events.add(referencesEvent);
    }
    List<String> questions = answer.getQuestions();
    if (CollectionUtils.isNotEmpty(questions)) {
      ChatMessageEvent questionsEvent = new ChatMessageEvent(sceneId, newMsgId(), ChatMessageType.QUESTIONS, questions);
      questionsEvent.setRefMsgId(msgId);
      events.add(questionsEvent);
    }
    if (StringUtils.isNotEmpty(answer.getChatLogId())) {
      Map<String, String> data = ImmutableMap.of("chatLogId", answer.getChatLogId());
      ChatMessageEvent chatLogEvent = new ChatMessageEvent(sceneId, newMsgId(), ChatMessageType.KNOWLEDGE_CHAT_LOG, data);
      chatLogEvent.setRefMsgId(msgId);
      events.add(chatLogEvent);
    }

    addStreamEvents(startTime, events);
  }

  /**
   * 添加流式输出的事件列表，根据 startTime 插入指定位置
   *
   * <p>有些流式输出在开始和结束之间可能会混入页面事件（比如自主规划模型的智能体使用流式函数调用时），需要将流式消息插入到正确的位置，以避免渲染历史消息时顺序不对</p>
   */
  private void addStreamEvents(Date startTime, List<ChatMessageEvent> events) {
    if (chatMessageEvents.isEmpty()) {
      chatMessageEvents.addAll(events);
      return;
    }
    // 如果第一条消息大于开始时间，直接插入到开头
    ChatMessageEvent first = chatMessageEvents.getFirst();
    if (ObjectUtils.getIfNull(first.getStartTime(), first.getCreateTime()).getTime() > startTime.getTime()) {
      chatMessageEvents.addAll(0, events);
      return;
    }
    // 如果最后一条消息小于开始时间，直接插入到结尾
    ChatMessageEvent last = chatMessageEvents.getLast();
    if (ObjectUtils.getIfNull(last.getStartTime(), last.getCreateTime()).getTime() <= startTime.getTime()) {
      chatMessageEvents.addAll(events);
      return;
    }
    // 插入到第一条大于开始时间的消息前
    for (int i = 0; i < chatMessageEvents.size(); i++) {
      ChatMessageEvent event = chatMessageEvents.get(i);
      if (ObjectUtils.getIfNull(event.getStartTime(), event.getCreateTime()).getTime() > startTime.getTime()) {
        chatMessageEvents.addAll(i, events);
        break;
      }
    }
  }


  /**
   * 发完对话完成消息
   */
  public void sendDoneMessage() {
    complete();
    if (emitter != null) {
      SseUtil.sendText(emitter, ChatMessageType.DONE, ChatConsts.COMPLETIONS_DONE);
      SseUtil.completeQuietly(emitter);
    }
  }

  /**
   * 发送就绪消息
   *
   * <p>用户手动进入场景，或者场景未生成回复内容时使用</p>
   */
  public void sendReadyMessage() {
    complete();
    SseUtil.sendText(emitter, ChatMessageType.READY, ChatConsts.COMPLETIONS_READY);
    SseUtil.completeQuietly(emitter);
  }

  /**
   * 发送退出场景消息
   */
  public void sendExitSceneMessage() {
    SseUtil.sendText(emitter, ChatMessageType.EXIT_SCENE, ChatConsts.COMPLETIONS_EXIT);
  }

  /**
   * 会话完成，保存消息记录到数据库
   */
  public void complete() {
    if (isComplete || ChatConsts.DEFAULT_SESSION_ID.equals(sessionId)) {
      return;
    }
    // 避免重复调用
    isComplete = true;
    Date endTime = new Date();

    List<ChatTraceLogDTO> traceLogs = traceLogBuilders.stream().map(ChatTraceLogBuilder::build).collect(Collectors.toList());
    // 异步保存，避免拖慢请求
    ThreadPools.getMsgSaver().submit(() -> {
      try {
        saveMessages(endTime, traceLogs);
      }
      catch (Exception e) {
        logger.error("Failed to save chat message: sessionId={}, transactionId={}", sessionId, transactionId, e);
      }
    });
  }

  /**
   * 保存会话消息
   */
  private void saveMessages(Date endTime, List<ChatTraceLogDTO> traceLogs) {
    // 通用智能体只需要更新会话结束时间、会话日志，不需要保存会话消息
    if (Boolean.TRUE.equals(isClaw)) {
      chatSessionService.updateSessionEndTime(sessionId, userId, endTime);
      // 保存日志
      saveChatTraceLogs(traceLogs);
      return;
    }

    Integer maxSort = sessionMsgMapper.getMaxSort(sessionId);
    AtomicInteger index = new AtomicInteger(Optional.ofNullable(maxSort).orElse(0));

    // 消息列表
    List<SessionMsgDTO> messages = new ArrayList<>();
    // 用户消息
    SessionMsgDTO userMessage = buildUserMessage(index, traceLogs.stream().anyMatch(l -> BaseConsts.STATE_FAIL.equals(l.getLogStatus())));
    if (userMessage != null) {
      messages.add(userMessage);
      userMsgId = userMessage.getMsgId();
    }
    for (ChatMessageEvent event : chatMessageEvents) {
      Object data = event.getData();
      SessionMsgDTO msg = newMsg(index, MessageRole.ASSISTANT.getCode(), event.getMsgId());
      msg.setSceneId(event.getSceneId() != null ? event.getSceneId() : sceneId);
      msg.setMsgType(event.getMsgType().getCode());
      msg.setMemory(event.isMemorized(), event.getMemoryContent());
      msg.setMsgText(data instanceof String ? (String) data : JsonUtil.toJsonString(data));
      msg.setDownloadType(event.getDownloadType());
      msg.setDownloadContent(event.getDownloadContent());
      msg.setContentType(event.getContentType());
      msg.setParagraphGroup(event.getParagraphGroup());
      msg.setParagraphSortby(event.getParagraphSortby());
      msg.setPlanId(event.getPlanId() != null ? event.getPlanId() : planId);
      msg.setBeginTime(event.getStartTime() != null ? event.getStartTime() : event.getCreateTime());
      msg.setEndTime(event.getCreateTime());
      msg.setRefMsgId(event.getRefMsgId());
      messages.add(msg);
    }

    // 保存消息列表
    if (!messages.isEmpty()) {
      chatSessionService.saveSessionMsgs(messages, userId);
      chatSessionService.updateSessionEndTime(sessionId, userId, endTime);
    }
    // 保存日志
    saveChatTraceLogs(traceLogs);
  }

  /**
   * 构造用户消息
   */
  @Nullable
  private SessionMsgDTO buildUserMessage(AtomicInteger index, boolean failed) {
    ChatRequestMessageDTO userMessage = request.getMessage();
    // 用户消息可能为空，比如用户手动切换场景触发的消息
    // 有文件时，即使消息内容为空也要保存，不然没法保存消息和文件的关联表
    if (StringUtils.isEmpty(userMessage.getContent()) && CollectionUtils.isEmpty(userMessage.getFileIds())) {
      return null;
    }
    SessionMsgDTO msg = newMsg(index, StringUtils.defaultIfEmpty(userMessage.getRole(), MessageRole.USER.getCode()), null);
    msg.setMemorized(BaseConsts.TRUE);
    msg.setRefMsgId(userMessage.getRefId());
    msg.setMsgType(StringUtils.isNotEmpty(userMessage.getType()) ? userMessage.getType() : ChatMessageType.INPUT.getCode());
    msg.setMsgText(userMessage.getContent());
    msg.setBeginTime(startTime);
    msg.setEndTime(startTime);
    msg.setExtParams(buildExtParams(userMessage));
    msg.setFileIds(userMessage.getFileIds());
    msg.setMsgStatus(failed ? BaseConsts.STATE_FAIL : BaseConsts.STATE_SUCCESS);
    return msg;
  }

  /**
   * 构造新消息对象
   */
  private SessionMsgDTO newMsg(AtomicInteger index, String role, @Nullable Long msgId) {
    SessionMsgDTO msg = new SessionMsgDTO();
    msg.setMsgId(msgId != null ? msgId : newMsgId());
    msg.setSessionId(sessionId);
    msg.setTransactionId(transactionId);
    msg.setBotId(botId);
    msg.setSceneId(sceneId);
    msg.setContextId(contextId);
    msg.setPlanId(planId);
    msg.setRole(role);
    msg.setSort(index.incrementAndGet());
    return msg;
  }

  /**
   * 构造消息的扩展参数 (JSON 字符串)
   */
  @Nullable
  public static String buildExtParams(ChatRequestMessageDTO chatMessageDTO) {
    String toolCallId = chatMessageDTO.getToolCallId();
    List<Long> fileIds = chatMessageDTO.getFileIds();
    Map<String, Object> params = getMessageParams(chatMessageDTO);
    if (StringUtils.isNotEmpty(toolCallId) || CollectionUtils.isNotEmpty(fileIds) || MapUtils.isNotEmpty(params)) {
      SessionMsgExtParamsDTO extParams = new SessionMsgExtParamsDTO();
      extParams.setToolCallId(StringUtils.isNotEmpty(toolCallId) ? toolCallId : null);
      extParams.setFileIds(CollectionUtils.isNotEmpty(fileIds) ? fileIds : null);
      extParams.setParams(MapUtils.isNotEmpty(params) ? params : null);
      return JsonUtil.toJsonStringCompact(extParams);
    }
    return null;
  }

  /**
   * 获取消息中的参数
   */
  private static Map<String, Object> getMessageParams(ChatRequestMessageDTO chatMessageDTO) {
    Map<String, Object> params = chatMessageDTO.getParams();
    // 前端的发送对话消息事件中未配置 params 时，可能会传 {"params":{},"planParams":null}}，这里特殊处理下，当作没有参数
    if (MapUtils.isNotEmpty(params)
      && Set.of("params", "planParams").equals(params.keySet())
      && ObjectUtils.isEmpty(params.get("params"))
      && ObjectUtils.isEmpty(params.get("planParams"))) {
      return null;
    }
    return params;
  }

  private void saveChatTraceLogs(List<ChatTraceLogDTO> traceLogs) {
    if (userMsgId != null && !traceLogs.isEmpty()) {
      chatSessionService.saveChatTraceLogs(userMsgId, traceLogs);
    }
  }

  /**
   * 根据不同的事件类型，构造事件
   */
  private ChatMessageEvent createMessageEvent(ChatMessageType msgType, Object msgContent, Long msgId) {
    ChatMessageEvent event = new ChatMessageEvent(sceneId, msgId, msgType, msgContent);
    if (msgType == ChatMessageType.WEB_PAGE_INFO || msgType == ChatMessageType.FILE_INFO) {
      // 网页、附件，尝试关联到上一个文本内容
      for (int i = this.chatMessageEvents.size() - 1; i >= 0; i--) {
        if (this.chatMessageEvents.get(i).getMsgType() == ChatMessageType.TEXT
          || this.chatMessageEvents.get(i).getMsgType() == ChatMessageType.AGENT_REPLY) {
          event.setRefMsgId(this.chatMessageEvents.get(i).getMsgId());
          break;
        }
      }
    }
    else if (msgType == ChatMessageType.CONFIRM_PLAN) {
      // 确认计划，需要记录对应的计划 ID
      SimplePlanDTO plan = (SimplePlanDTO) msgContent;
      event.setPlanId(plan.getPlanId());
    }
    return event;
  }
}
