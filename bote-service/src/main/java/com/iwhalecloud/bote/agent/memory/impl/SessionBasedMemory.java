package com.iwhalecloud.bote.agent.memory.impl;

import com.iwhalecloud.bote.agent.memory.ShortTermMemory;
import com.iwhalecloud.bote.agent.memory.helper.MemoryCompactor;
import com.iwhalecloud.bote.agent.memory.helper.ReminderGenerator;
import com.iwhalecloud.bote.agent.memory.service.MemoryDailyUpdater;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.enums.SystemReminderType;
import com.iwhalecloud.bote.dto.SystemReminder;
import com.iwhalecloud.bote.dto.agent.MemoryMessage;
import com.iwhalecloud.bote.dto.agent.MessageMetadata;
import com.iwhalecloud.bote.dto.chat.ChatRequestMessageDTO;
import com.iwhalecloud.bote.dto.chat.SessionMsgDTO;
import com.iwhalecloud.bote.dto.chat.SessionMsgExtParamsDTO;
import com.iwhalecloud.bote.dto.chat.vo.SessionMsgVO;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.ToolMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.mapper.chat.SessionMapper;
import com.iwhalecloud.bote.mapper.chat.SessionMsgMapper;
import com.iwhalecloud.bote.service.chat.IChatSessionService;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 基于会话表的短期记忆
 *
 * @author bianjp
 * @since 2026-03-09
 */
@SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
public class SessionBasedMemory implements ShortTermMemory {
  private static final Logger logger = LoggerFactory.getLogger(SessionBasedMemory.class);
  private static final SessionMapper sessionMapper = SpringUtil.getBean(SessionMapper.class);
  private static final SessionMsgMapper sessionMsgMapper = SpringUtil.getBean(SessionMsgMapper.class);
  private static final IChatSessionService chatSessionService = SpringUtil.getBean(IChatSessionService.class);
  private static final MemoryDailyUpdater memoryDailyUpdater = SpringUtil.getBean(MemoryDailyUpdater.class);
  /** 会话摘要消息的模板，发送给大模型使用 */
  private static final String SUMMARY_MESSAGE_TEMPLATE = """
    <previous-summary>
    %s
    </previous-summary>
     The above is a summary of our previous conversation. Use it as context to maintain continuity.""";

  /** 用户请求中的消息 */
  private final ChatRequestMessageDTO requestMessage;
  /** 大模型客户端 */
  private final LlmClient modelClient;
  /** 会话 ID */
  private final Long sessionId;
  /** 事务 ID */
  private final Long transactionId;
  /** 机器人 ID */
  private final Long botId;
  /** 智能体 ID (仅用于 claw 类型的智能体) */
  @Nullable
  private final Long sceneId;
  /** 用户 ID */
  private final Long userId;
  /** 用户所在空间 ID（用于每日记忆存储） */
  private final Long spaceId;
  /** 租户 ID（用于嵌入模型查找） */
  private final Long tenantId;
  /** 上下文 ID，主智能体为 null，子智能体、claw 类型的智能体为非空 */
  @Nullable
  private final String contextId;
  /** 系统提醒生成器列表 */
  private final List<ReminderGenerator> reminderGenerators;
  /** 消息列表 */
  private final List<MemoryMessage> memoryMessages = new ArrayList<>();
  /** 会话摘要 */
  private String compressedSummary;
  /** 用户消息 ID */
  @Getter
  private Long userMsgId;
  /** 消息序号，从 1 开始递增 */
  private AtomicInteger msgIndex;

  // -@cs[ParameterNumberCheck] Ignore intentionally
  @Builder
  public SessionBasedMemory(ChatRequestMessageDTO requestMessage, LlmClient modelClient,
                            Long sessionId, Long transactionId, Long botId, @Nullable Long sceneId, Long userId,
                            @Nullable String contextId,
                            Long spaceId, Long tenantId,
                            @Nullable List<ReminderGenerator> reminderGenerators) {
    this.requestMessage = requestMessage;
    this.modelClient = modelClient;
    this.sessionId = sessionId;
    this.transactionId = transactionId;
    this.botId = botId;
    this.sceneId = sceneId;
    this.userId = userId;
    this.spaceId = spaceId;
    this.tenantId = tenantId;
    this.contextId = StringUtils.isNotEmpty(contextId) ? contextId : null;
    this.reminderGenerators = reminderGenerators != null ? reminderGenerators : List.of();
  }

  /**
   * 初始化
   */
  public void init() {
    Integer maxSort;
    // claw 类型的智能体查询时不能加上 contextId 条件，要保证 sort 在整个智能应用下唯一(会话内可以切换不同智能体)。调试会话例外
    if (sceneId != null && !SceneConsts.TEST_CONVERSATION_ID.equals(sessionId)) {
      maxSort = sessionMsgMapper.getMaxSort(sessionId);
    }
    else {
      maxSort = sessionMsgMapper.getMaxSortByContext(sessionId, contextId);
    }
    msgIndex = new AtomicInteger(maxSort != null ? maxSort : 0);

    // 查询会话摘要
    compressedSummary = sessionMapper.selectCompressedSummary(sessionId);

    // 加载历史消息
    loadHistoryMessages();

    // 保存用户消息
    UserMessage userMessage = buildUserMessage(requestMessage.getContent(), requestMessage.getFileIds(), requestMessage.getParams());
    MemoryMessage memoryMessage = saveMessage(userMessage, null);
    userMsgId = memoryMessage.msgId();
    memoryMessages.add(memoryMessage);
  }

  /**
   * 加载历史消息
   */
  @SuppressWarnings("PMD.GuardLogStatement")
  private void loadHistoryMessages() {
    List<SessionMsgVO> sessionMsgList = sessionMsgMapper.selectMessagesBySessionId(sessionId, contextId);
    List<MemoryMessage> tmpMessages = new ArrayList<>(sessionMsgList.size());
    for (SessionMsgVO sessionMsg : sessionMsgList) {
      Message message = convertMessage(sessionMsg);
      tmpMessages.add(new MemoryMessage(sessionMsg.getId(), message, sessionMsg.getReminders()));
    }
    // 所有助手消息的 toolCallId 集合
    Set<String> toolCallIdsInAssistantMessages = tmpMessages.stream()
      .map(MemoryMessage::message)
      .filter(m -> m instanceof AssistantMessage && ((AssistantMessage) m).hasToolCall())
      .map(m -> ((AssistantMessage) m).getToolCall().getId())
      .collect(Collectors.toSet());
    // 所有工具消息的 toolCallId 集合
    Map<String, MemoryMessage> toolMessageMap = tmpMessages.stream()
      .filter(m -> m.message() instanceof ToolMessage)
      .collect(Collectors.toMap(m -> ((ToolMessage) m.message()).getToolCallId(), Function.identity()));
    for (MemoryMessage m : tmpMessages) {
      // 助手消息，直接添加，并添加对应的工具消息
      if (m.message() instanceof AssistantMessage assistantMessage) {
        memoryMessages.add(m);
        if (assistantMessage.hasToolCall()) {
          String toolCallId = assistantMessage.getToolCall().getId();
          MemoryMessage toolMessage = toolMessageMap.get(toolCallId);
          //noinspection ReplaceNullCheck
          if (toolMessage != null) {
            // 确保工具消息紧跟在助手消息之后，防止数据库中两条消息之间穿插了其它消息导致请求报错
            memoryMessages.add(toolMessage);
          }
          else {
            // 如果助手消息缺少对应的工具消息(可能是执行工具报错了导致未生成工具消息)，添加一个占位的工具消息，以避免大模型接口报错
            memoryMessages.add(new MemoryMessage(null, new ToolMessage(toolCallId, "Failed")));
          }
        }
      }
      // 工具消息，不需要添加，处理助手消息的分支中会添加
      else if (m.message() instanceof ToolMessage toolMessage) {
        // 如果工具消息缺少对应的助手消息，忽略工具消息以避免大模型接口报错。打印警告以方便后续排查（大概率是 bug）
        if (!toolCallIdsInAssistantMessages.contains(toolMessage.getToolCallId())) {
          logger.warn("Missing assistant message for tool message: sessionId={}, msgId={}, toolCallId={}", sessionId, m.msgId(), toolMessage.getToolCallId());
        }
      }
      // 其它消息直接添加
      else {
        memoryMessages.add(m);
      }
    }
  }

  /**
   * 将数据库中的消息记录转为大模型使用的消息对象
   */
  private Message convertMessage(SessionMsgVO sessionMsg) {
    if (MessageRole.USER.getCode().equals(sessionMsg.getRole())) {
      List<Long> fileIds = null;
      Map<String, Object> params = null;
      SessionMsgExtParamsDTO extParams = JsonUtil.parseJson(sessionMsg.getExtParams(), SessionMsgExtParamsDTO.class);
      if (extParams != null) {
        fileIds = extParams.getFileIds();
        params = extParams.getParams();
      }
      return buildUserMessage(sessionMsg.getContent(), fileIds, params);
    }
    else if (MessageRole.ASSISTANT.getCode().equals(sessionMsg.getRole())) {
      if (Strings.CS.startsWith(sessionMsg.getContent(), "{")) {
        return JsonUtil.parseJsonRequired(sessionMsg.getContent(), AssistantMessage.class);
      }
      return new AssistantMessage(StringUtils.defaultString(sessionMsg.getContent()));
    }
    else if (MessageRole.TOOL.getCode().equals(sessionMsg.getRole())) {
      return JsonUtil.parseJsonRequired(sessionMsg.getContent(), ToolMessage.class);
    }
    throw new BssException("未知的消息角色: msgId=" + sessionMsg.getId() + ", role=" + sessionMsg.getRole());
  }

  @Override
  public List<Message> getMessages() {
    List<Message> messages = new ArrayList<>(memoryMessages.size() + 1);
    int startIndex = 0;
    // 先添加系统消息
    if (memoryMessages.getFirst().message() instanceof SystemMessage) {
      messages.add(memoryMessages.getFirst().message());
      startIndex = 1;
    }
    // 摘要要放在系统消息后面
    if (StringUtils.isNotEmpty(compressedSummary)) {
      // 参考 https://github.com/agentscope-ai/ReMe/blob/v0.3.0.6b3/reme/memory/file_based/reme_in_memory_memory.py#L53
      UserMessage summaryMessage = new UserMessage(SUMMARY_MESSAGE_TEMPLATE.formatted(compressedSummary));
      messages.add(summaryMessage);
    }
    List<MemoryMessage> remainingMessages = memoryMessages.subList(startIndex, memoryMessages.size());
    int lastUserMessageIndex = remainingMessages.size() - 1 - IterableUtils.indexOf(remainingMessages.reversed(), m -> m.message() instanceof UserMessage);
    for (int i = 0; i < remainingMessages.size(); i++) {
      MemoryMessage m = remainingMessages.get(i);
      String reminderText = buildReminderText(m, i == remainingMessages.size() - 1, i == lastUserMessageIndex);
      Message message = m.message();
      if (StringUtils.isEmpty(reminderText)) {
        messages.add(message);
      }
      else if (message instanceof UserMessage) {
        messages.add(new UserMessage(reminderText));
        messages.add(message);
      }
      else if (message instanceof ToolMessage toolMessage) {
        messages.add(new ToolMessage(toolMessage.getToolCallId(), toolMessage.getContent() + "\n\n" + reminderText));
      }
    }
    return messages;
  }

  /**
   * 构造系统提醒文本
   */
  @Nullable
  private String buildReminderText(MemoryMessage m, boolean isLast, boolean isLastUserMessage) {
    List<SystemReminder> reminders = m.reminders();
    if (reminders == null || reminders.isEmpty()) {
      return null;
    }
    // 如果不是最后一个消息，则只保留持久化的系统提醒
    if (!isLast) {
      // 特殊处理会话状态提醒，需要针对最后一条用户消息保留（这样就不需要在工具消息中添加会话状态提醒）
      reminders = reminders.stream()
        .filter(r -> r.type().isPersistent() || (isLastUserMessage && r.type() == SystemReminderType.SESSION_STATE_REMINDER))
        .toList();
    }
    return reminders.stream().map(SystemReminder::text).collect(Collectors.joining("\n\n"));
  }

  @Override
  public List<ToolCall> getToolCalls() {
    return memoryMessages.stream()
      .filter(m -> m.message() instanceof AssistantMessage assistantMessage && assistantMessage.hasToolCall())
      .map(m -> ((AssistantMessage) m.message()).getToolCall())
      .toList();
  }

  @Override
  public void addMessage(Message message, @Nullable MessageMetadata metadata) {
    // 忽略用户消息，已在 init 方法中保存
    if (message instanceof UserMessage) {
      return;
    }
    // 系统消息需要作为第一条消息，且不需要保存
    if (message instanceof SystemMessage) {
      MemoryMessage memoryMessage = new MemoryMessage(null, message);
      if (memoryMessages.isEmpty()) {
        memoryMessages.add(memoryMessage);
      }
      else if (memoryMessages.getFirst().message() instanceof SystemMessage) {
        memoryMessages.set(0, memoryMessage);
      }
      else {
        memoryMessages.addFirst(memoryMessage);
      }
      return;
    }
    memoryMessages.add(saveMessage(message, metadata));
  }

  @Override
  public void compressIfNecessary() {
    // 参考 https://github.com/agentscope-ai/CoPaw/blob/v0.0.6/src/copaw/agents/hooks/memory_compaction.py#L43
    Pair<String, List<MemoryMessage>> result = MemoryCompactor.autoCompact(modelClient, memoryMessages, compressedSummary);
    if (result != null) {
      compressedSummary = result.getLeft();
      List<MemoryMessage> compressedMessages = result.getRight();
      List<Long> msgIds = compressedMessages.stream().map(MemoryMessage::msgId).filter(Objects::nonNull).toList();
      memoryMessages.removeAll(compressedMessages);
      sessionMapper.updateCompressedSummary(sessionId, compressedSummary);
      sessionMsgMapper.updateMsgCompressed(msgIds);

      // 触发记忆压缩时，同时异步将被压缩的对话内容总结到每日记忆文件（MEMORY-YYYY-MM-DD.md）
      if (spaceId != null || tenantId != null) {
        memoryDailyUpdater.asyncUpdateDailyMemory(modelClient, compressedMessages, spaceId, botId, userId, tenantId);
      }
    }
  }

  /**
   * 保存消息
   */
  @SuppressWarnings("IfCanBeSwitch")
  private MemoryMessage saveMessage(Message message, @Nullable MessageMetadata metadata) {
    SessionMsgDTO msg;
    if (message instanceof UserMessage) {
      // 用户消息只会有一条，可以从上下文中获取，以便获取原始消息内容
      msg = newMsg(MessageRole.USER.getCode());
      // role=user 表示是 a2ui 卡片、页面交互产生的消息
      if (MessageRole.TOOL.getCode().equals(requestMessage.getRole())) {
        msg.setMsgType(ChatMessageType.A2UI.getCode().equals(requestMessage.getType()) ? ChatMessageType.A2UI.getCode() : ChatMessageType.PAGE.getCode());
      }
      msg.setMsgText(requestMessage.getContent());
      msg.setExtParams(ChatContext.buildExtParams(requestMessage));
      msg.setFileIds(requestMessage.getFileIds());
      msg.setMsgStatus(BaseConsts.STATE_SUCCESS);
    }
    else if (message instanceof AssistantMessage) {
      msg = newMsg(MessageRole.ASSISTANT.getCode());
      msg.setMsgText(JsonUtil.toJsonString(message));
    }
    else if (message instanceof ToolMessage) {
      msg = newMsg(MessageRole.TOOL.getCode());
      msg.setMsgText(JsonUtil.toJsonString(message));
    }
    else {
      throw new BssException("未知的消息类型: " + message.getRole());
    }
    applyMessageMetadata(msg, metadata);
    msg.setMsgType(ObjectUtils.getIfNull(msg.getMsgType(), "general"));
    msg.setMemorized(BaseConsts.TRUE);
    if (msg.getBeginTime() == null) {
      msg.setBeginTime(new Date());
    }
    if (msg.getEndTime() == null) {
      msg.setEndTime(msg.getBeginTime());
    }
    List<SystemReminder> reminders = generateReminders(message, msg);
    chatSessionService.saveSessionMsgs(List.of(msg), userId);
    return new MemoryMessage(msg.getMsgId(), message, reminders);
  }

  /**
   * 处理消息元数据
   */
  private static void applyMessageMetadata(SessionMsgDTO msg, @Nullable MessageMetadata metadata) {
    if (metadata == null) {
      return;
    }
    if (metadata.msgType() != null) {
      msg.setMsgType(metadata.msgType().getCode());
    }
    msg.setBeginTime(metadata.startTime());
    msg.setEndTime(metadata.endTime());
    if (metadata.extParams() != null) {
      msg.setExtParams(JsonUtil.toJsonString(metadata.extParams()));
    }
  }

  /**
   * 生成系统提醒
   */
  private List<SystemReminder> generateReminders(Message message, SessionMsgDTO msg) {
    if (!reminderGenerators.isEmpty() && (message instanceof UserMessage || message instanceof ToolMessage)) {
      List<SystemReminder> reminders = reminderGenerators.stream()
        .map(g -> g.generate(memoryMessages, message))
        .filter(Objects::nonNull)
        .toList();
      if (!reminders.isEmpty()) {
        msg.addReminders(reminders);
        return reminders;
      }
    }
    return List.of();
  }

  /**
   * 保存错误信息
   *
   * @param error 错误信息
   * @param stacktrace 异常堆栈
   */
  public void saveError(String error, @Nullable String stacktrace) {
    Date now = new Date();
    List<SessionMsgDTO> messages = new ArrayList<>();
    SessionMsgDTO msg = newMsg(MessageRole.ASSISTANT.getCode());
    msg.setMsgText(error);
    msg.setMsgType(ChatMessageType.ERROR.getCode());
    msg.setBeginTime(now);
    msg.setEndTime(now);
    messages.add(msg);
    if (StringUtils.isNotEmpty(stacktrace)) {
      SessionMsgDTO msg2 = newMsg(MessageRole.ASSISTANT.getCode());
      msg2.setMsgText(error);
      msg2.setMsgType(ChatMessageType.EXCEPTION.getCode());
      msg2.setBeginTime(now);
      msg2.setEndTime(now);
      messages.add(msg2);
    }
    chatSessionService.saveSessionMsgs(messages, userId);
    // 将用户消息改为失败状态
    if (userMsgId != null) {
      sessionMsgMapper.updateMsgStatus(userMsgId, BaseConsts.STATE_FAIL);
    }
  }

  /**
   * 构造新消息对象
   */
  private SessionMsgDTO newMsg(String role) {
    SessionMsgDTO msg = new SessionMsgDTO();
    msg.setMsgId(newMsgId());
    msg.setSessionId(sessionId);
    msg.setTransactionId(transactionId);
    msg.setBotId(botId);
    msg.setContextId(contextId);
    msg.setRole(role);
    msg.setSort(msgIndex.incrementAndGet());
    return msg;
  }

  /**
   * 生成新消息 ID
   *
   * @return 消息 ID
   */
  public Long newMsgId() {
    return Sequences.BOT_SESSION_MSG_ID.next();
  }

}
