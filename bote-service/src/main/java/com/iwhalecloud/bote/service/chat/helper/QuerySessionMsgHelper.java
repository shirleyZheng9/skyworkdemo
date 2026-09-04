package com.iwhalecloud.bote.service.chat.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.cache.GeneraAgentIdCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.FileTypeUtil;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.dto.chat.SceneRecommendationDTO;
import com.iwhalecloud.bote.dto.chat.SessionDTO;
import com.iwhalecloud.bote.dto.chat.SessionMsgExtParamsDTO;
import com.iwhalecloud.bote.dto.chat.agent.AgentReplyA2uiPart;
import com.iwhalecloud.bote.dto.chat.agent.AgentReplyAssistantPart;
import com.iwhalecloud.bote.dto.chat.agent.AgentReplyPart;
import com.iwhalecloud.bote.dto.chat.agent.AgentReplyToolCallPart;
import com.iwhalecloud.bote.dto.chat.query.ChatMessageQueryParams;
import com.iwhalecloud.bote.dto.chat.vo.SessionMsgVO;
import com.iwhalecloud.bote.dto.planning.PlanRecordDTO;
import com.iwhalecloud.bote.dto.planning.PlanStepDTO;
import com.iwhalecloud.bote.dto.planning.SimplePlanDTO;
import com.iwhalecloud.bote.dto.planning.SimplePlanDTO.SimplePlanStepDTO;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.ToolMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bote.mapper.chat.SessionMapper;
import com.iwhalecloud.bote.mapper.chat.SessionMsgMapper;
import com.iwhalecloud.bote.mapper.planning.PlanManageMapper;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 查询会话消息辅助类
 *
 * @author Admin
 */
@RequiredArgsConstructor
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class QuerySessionMsgHelper {
  private static final Logger logger = LoggerFactory.getLogger(QuerySessionMsgHelper.class);

  private final BotQueryMapper botQueryMapper;
  private final SessionMapper sessionMapper;
  private final SessionMsgMapper sessionMsgMapper;
  private final PlanManageMapper planManageMapper;
  private final IFileStoreService fileStoreService;
  private final GeneraAgentIdCache generaAgentIdCache;

  public List<SessionMsgVO> execute(Long tenantId, Long sessionId) {
    List<SessionMsgVO> list = sessionMsgMapper.listSessionMsgs(sessionId);
    List<SessionMsgVO> messages = buildMessage(list);
    fillBotInfo(tenantId, messages);
    return messages;
  }

  public PageInfo<SessionMsgVO> execute(ChatMessageQueryParams queryParams) {
    // 按事务 ID 分页查询。一个事务下的多条消息可能需要合并为一条消息，如果按消息粒度分页会导致查到的事务不完整
    //noinspection resource
    PageInfo<Long> transactionIdsPageInfo = sessionMsgMapper.selectTransactionIdsPage(queryParams, queryParams.buildRowBounds()).toPageInfo();
    List<Long> transactionIds = ListUtils.emptyIfNull(transactionIdsPageInfo.getList());
    if (transactionIds.isEmpty()) {
      PageInfo<SessionMsgVO> pageInfo = new PageInfo<>(List.of());
      // 保留前端传递的分页参数，避免前端切换会话时异常
      pageInfo.setPageNum(ObjectUtils.getIfNull(queryParams.getPageNum(), 1));
      pageInfo.setPageSize(ObjectUtils.getIfNull(queryParams.getPageSize(), 20));
      pageInfo.setHasNextPage(false);
      return pageInfo;
    }

    // 查询事务下的所有消息
    List<SessionMsgVO> messages = sessionMsgMapper.selectMessagesByTransactionIds(queryParams, transactionIds);

    if (!isBoteClaw(queryParams)) {
      // 处理关联消息，会修改 messages 列表
      wrapRelatedMessage(messages, queryParams.getSessionId());
      // 纠正消息排序
      messages.sort(Comparator.comparing(SessionMsgVO::getSort));
    }

    // 构造消息列表
    messages = buildMessage(messages);

    // 填充应用信息
    fillBotInfo(queryParams.getTenantId(), messages);
    // 倒序排序
    messages.sort(Comparator.comparing(SessionMsgVO::getSort).reversed());
    PageInfo<SessionMsgVO> pageInfo = transactionIdsPageInfo.convert(id -> null);
    pageInfo.setList(messages);
    return pageInfo;
  }

  /**
   * 检查是否是 BoteClaw (运行态智能应用)
   */
  private boolean isBoteClaw(ChatMessageQueryParams queryParams) {
    Long botId;
    if (queryParams.getBotId() != null) {
      botId = queryParams.getBotId();
    }
    else {
      SessionDTO session = sessionMapper.getSession(queryParams.getSessionId());
      Assert.notNull(session, () -> "查询不到有效的会话，sessionId=" + queryParams.getSessionId());
      botId = session.getBotId();
      if (session.getSpaceId() == null) {
        return false;
      }
    }
    return generaAgentIdCache.isBoteClaw(botId);
  }

  /**
   * 处理关联消息
   * <p>1.文本类型，处理关联的网页、附件、追问、参考文档</p>
   * <p>2.confirm类型，处理关联的计划消息</p>
   * <p>3.段落类型，整合同分组内容</p>
   */
  private void wrapRelatedMessage(List<SessionMsgVO> messages, Long sessionId) {
    // 调整计划相关消息
    wrapPlanMessage(messages, sessionId);
    // 整合段落消息
    wrapParagraphMessage(messages, sessionId);
  }

  /**
   * 构造上下文消息，注意 token 数量过大问题
   */
  public List<Message> getHistory(Long sessionId, String contextId) {
    int limitMinute = SystemParameter.CHAT_MESSSGE_HIS_LIMIT_MINUTE.getRequiredIntegerValueFromDb();
    long cutoffMillis = limitMinute * 60L * 1000L;
    Date date = new Date(System.currentTimeMillis() - cutoffMillis);
    List<Message> history = sessionMsgMapper.selectHistoryTextMessagesByContextId(sessionId, contextId, date).stream()
      .filter(m -> StringUtils.isNotEmpty(m.getMsgText()))
      .map(m -> MessageRole.ASSISTANT.getCode().equals(m.getMsgRole()) ? new AssistantMessage(m.getMsgText()) : new UserMessage(m.getMsgText()))
      .collect(Collectors.toList());
    // 临时限制对话条数，后续再完善 token 数量计算策略。FIXME
    return history.size() > 10 ? history.subList(history.size() - 10, history.size()) : history;
  }

  private List<SessionMsgVO> buildMessage(List<SessionMsgVO> messages) {
    // 忽略空消息
    messages = messages.stream().filter(p -> StringUtils.isNotEmpty(p.getType()))
      .filter(p -> StringUtils.isNotEmpty(p.getContent()) || StringUtils.isNotEmpty(p.getExtParams()))
      .collect(Collectors.toList());
    if (messages.isEmpty()) {
      return Collections.emptyList();
    }

    // 解析扩展参数
    messages.forEach(this::parseExtParams);

    // 处理通用智能体消息
    messages = mergeGeneralAgentMessages(messages);

    // 过滤后的消息列表
    List<SessionMsgVO> finalMessages = new ArrayList<>(messages.size() + 1);

    for (SessionMsgVO msg : messages) {
      // 忽略不应该单独显示的消息
      if (skipMessage(msg, messages)) {
        continue;
      }
      // 解析页面
      if (ChatMessageType.PAGE.getCode().equals(msg.getType())) {
        if (msg.getPage() == null) {
          msg.setPage(JsonUtil.parseJson(msg.getContent(), new TypeReference<>() {
          }));
        }
        msg.setContent(null);
      }
      // 解析推荐场景
      else if (ChatMessageType.SELECT_SCENE.getCode().equals(msg.getType())) {
        parseRecommendedScenes(msg);
      }
      else if (ChatMessageType.AGENT_REPLY.getCode().equals(msg.getType())) {
        parseAgentReplyParts(msg);
      }
      finalMessages.add(msg);
    }
    return finalMessages;
  }

  /**
   * 合并普通会话中的通用智能体消息
   */
  private List<SessionMsgVO> mergeGeneralAgentMessages(List<SessionMsgVO> allMessages) {
    if (allMessages.stream().noneMatch(this::isGeneralAgentMessage)) {
      return allMessages;
    }

    Map<Long, List<SessionMsgVO>> groupedMessages = allMessages.stream().collect(Collectors.groupingBy(SessionMsgVO::getTransactionId));
    List<SessionMsgVO> mergedMessages = new ArrayList<>(allMessages.size());
    for (List<SessionMsgVO> transaction : groupedMessages.values()) {
      // 非通用智能体消息
      if (transaction.stream().noneMatch(this::isGeneralAgentMessage)) {
        mergedMessages.addAll(transaction);
        continue;
      }

      List<SessionMsgVO> transactionMessages = transaction;
      // 第一条可能是用户消息，也可能是定时任务发送的助手消息
      SessionMsgVO firstMsg = transactionMessages.getFirst();
      if (MessageRole.USER.getCode().equals(firstMsg.getRole())) {
        // 如果是用户消息，需要忽略操作 A2UI 卡片产生的消息
        if (!ChatMessageType.A2UI.getCode().equals(firstMsg.getType()) && (StringUtils.isNotEmpty(firstMsg.getContent()) || CollectionUtils.isNotEmpty(firstMsg.getFileIds()))) {
          mergedMessages.add(firstMsg);
        }
        transactionMessages = transactionMessages.subList(1, transactionMessages.size());
      }

      // 合并 assistant 和 tool 消息
      SessionMsgVO[] assistantAndToolMessages = transactionMessages.stream()
        .filter(m -> !ChatMessageType.ERROR.getCode().equals(m.getType()))
        .filter(m -> MessageRole.ASSISTANT.getCode().equals(m.getRole()) || MessageRole.TOOL.getCode().equals(m.getRole()))
        .toArray(SessionMsgVO[]::new);
      if (assistantAndToolMessages.length > 0) {
        mergedMessages.addAll(mergeAssistantAndToolMessages(assistantAndToolMessages));
      }

      // 添加错误消息
      transactionMessages.stream().filter(m -> ChatMessageType.ERROR.getCode().equals(m.getType())).forEach(mergedMessages::add);
    }
    return mergedMessages;
  }

  /**
   * 检查是否是通用智能体产生的消息
   */
  private boolean isGeneralAgentMessage(SessionMsgVO msg) {
    return "general".equals(msg.getType()) || ChatMessageType.A2UI.getCode().equals(msg.getType());
  }

  /**
   * 合并 assistant 和 tool 消息
   */
  private List<SessionMsgVO> mergeAssistantAndToolMessages(SessionMsgVO[] messages) {
    List<AgentReplyPart> replyParts = new ArrayList<>();
    List<SessionMsgVO> mergedMessages = new ArrayList<>();
    for (int i = 0; i < messages.length; i++) {
      SessionMsgVO msg = messages[i];
      if (MessageRole.TOOL.getCode().equals(msg.getRole())) {
        parseToolMessage(msg, replyParts, mergedMessages);
        continue;
      }
      AssistantMessage assistantMessage = JsonUtil.parseJsonRequired(msg.getContent(), AssistantMessage.class);
      String reasoning = assistantMessage.getReasoningContent();
      String content = assistantMessage.getContent();
      // 文本回复
      if (StringUtils.isNotEmpty(reasoning) || StringUtils.isNotEmpty(content)) {
        AgentReplyAssistantPart part = new AgentReplyAssistantPart();
        part.setReasoning(reasoning);
        part.setContent(content);
        replyParts.add(part);
      }
      // 工具调用
      if (assistantMessage.hasToolCall()) {
        if (showToolCall(msg)) {
          replyParts.add(buildToolCallPart(messages, i, assistantMessage.getToolCall()));
        }
      }
    }
    if (!replyParts.isEmpty()) {
      SessionMsgVO first = messages[0];
      first.setAgentReplyParts(replyParts);
      first.setContent(null);
      first.setType(ChatMessageType.AGENT_REPLY.getCode());
      mergedMessages.addFirst(first);
    }

    return mergedMessages;
  }

  /**
   * 处理通用智能体的工具消息
   */
  private void parseToolMessage(SessionMsgVO msg, List<AgentReplyPart> replyParts, List<SessionMsgVO> mergedMessages) {
    SessionMsgExtParamsDTO extParams = JsonUtil.parseJson(msg.getExtParams(), SessionMsgExtParamsDTO.class);
    if (extParams == null || !ObjectUtils.isNotEmpty(extParams.getEventData())) {
      return;
    }
    try {
      if (ChatMessageType.A2UI.getCode().equals(extParams.getEventType())) {
        replyParts.add(new AgentReplyA2uiPart(JsonUtil.convert(extParams.getEventData(), new TypeReference<>() {
        })));
      }
      else if (ChatMessageType.PAGE.getCode().equals(extParams.getEventType())) {
        msg.setType(ChatMessageType.PAGE.getCode());
        msg.setRole(MessageRole.ASSISTANT.getCode());
        msg.setPage(JsonUtil.convert(extParams.getEventData(), new TypeReference<>() {
        }));
        mergedMessages.add(msg);
      }
    }
    catch (Exception e) {
      logger.warn("Failed to parse msg event data: msgId={}", msg.getId(), e);
    }
  }

  /**
   * 是否显示工具调用
   */
  private boolean showToolCall(SessionMsgVO msg) {
    SessionMsgExtParamsDTO extParams = JsonUtil.parseJson(msg.getExtParams(), SessionMsgExtParamsDTO.class);
    return extParams == null || !Boolean.TRUE.equals(extParams.getHideToolCall());
  }

  /**
   * 构造工具调用部分
   */
  private AgentReplyToolCallPart buildToolCallPart(SessionMsgVO[] messages, int i, ToolCall toolCall) {
    ToolMessage toolMessage;
    SessionMsgExtParamsDTO extParams;
    if (messages.length > i + 1 && MessageRole.TOOL.getCode().equals(messages[i + 1].getRole())) {
      toolMessage = JsonUtil.parseJsonRequired(messages[i + 1].getContent(), ToolMessage.class);
      extParams = JsonUtil.parseJson(messages[i + 1].getExtParams(), SessionMsgExtParamsDTO.class);
    }
    else {
      toolMessage = null;
      extParams = null;
    }
    AgentReplyToolCallPart part = new AgentReplyToolCallPart();
    part.setToolName(toolCall.getFunction().getName());
    part.setInput(JsonUtil.parseJson(toolCall.getFunction().getArguments(), Object.class));
    part.setOutput(toolMessage != null ? toolMessage.getContent() : null);
    part.setSuccess(extParams != null ? extParams.getSuccess() : null);
    part.setSpentTime(extParams != null ? extParams.getSpentTime() : null);
    return part;
  }

  /**
   * 填充应用信息
   */
  private void fillBotInfo(Long tenantId, List<SessionMsgVO> messages) {
    List<SimpleBotDTO> bots = queryBots(tenantId, messages);
    if (bots.isEmpty()) {
      return;
    }
    Map<Long, SimpleBotDTO> botMap = bots.stream().collect(Collectors.toMap(SimpleBotDTO::getBotId, Function.identity(), (a, b) -> a));
    for (SessionMsgVO msg : messages) {
      if (msg.getBotId() != null) {
        SimpleBotDTO bot = botMap.get(msg.getBotId());
        if (bot != null) {
          msg.setBotName(bot.getBotName());
          msg.setOwnerTenantId(bot.getTenantId());
        }
      }
    }
  }

  private List<SimpleBotDTO> queryBots(@Nullable Long tenantId, List<SessionMsgVO> messages) {
    Set<Long> botIds = messages.stream().map(SessionMsgVO::getBotId).filter(Objects::nonNull).collect(Collectors.toSet());
    if (CollectionUtils.isEmpty(botIds)) {
      return Collections.emptyList();
    }
    List<SimpleBotDTO> bots = botQueryMapper.selectBotListByIds(tenantId, botIds);
    if (tenantId == null) {
      return bots;
    }
    Set<Long> ids = bots.stream().map(SimpleBotDTO::getBotId).collect(Collectors.toSet());
    // 应用可能非当前租户下的，差集计算
    Set<Long> differenceIds = new HashSet<>(botIds);
    differenceIds.removeAll(ids);
    if (CollectionUtils.isNotEmpty(differenceIds)) {
      bots = new ArrayList<>(bots);
      bots.addAll(botQueryMapper.selectBotListByIds(null, differenceIds));
    }
    return bots;
  }

  /**
   * 检查是否应该忽略消息
   */
  private boolean skipMessage(SessionMsgVO msg, List<SessionMsgVO> allMessages) {
    // 处理关联消息
    // TODO 怎么将数据设置到页面消息中作为表单的初始值
    if (msg.getRefId() != null) {
      SessionMsgVO refMsg = IterableUtils.find(allMessages, m -> msg.getRefId().equals(m.getId()));
      if (refMsg != null) {
        // 如果关联的是页面消息，表示是页面中的表单提交结果，不需要显示
        if (ChatMessageType.PAGE.getCode().equals(refMsg.getType())) {
          return true;
        }
        // 如果是参考文档消息，将参考文档设置到关联的文本消息中
        if (ChatMessageType.REFERENCES.getCode().equals(msg.getType())) {
          refMsg.setReferences(JsonUtil.parseJson(msg.getContent(), new TypeReference<>() {
          }));
          return true;
        }
      }
    }
    // 忽略前面未能处理到的参考文档消息（可能是异常数据）
    return ChatMessageType.REFERENCES.getCode().equals(msg.getType());
  }

  /**
   * 解析推荐场景
   */
  private void parseRecommendedScenes(SessionMsgVO msg) {
    // 兼容历史数据（content 只有文本提示，不是 JSON）
    if (!Strings.CS.startsWith(msg.getContent(), "{")) {
      return;
    }
    // 忽略 JSON 解析失败
    SceneRecommendationDTO recommendation = JsonUtil.parseJson(msg.getContent(), SceneRecommendationDTO.class);
    if (recommendation != null && StringUtils.isNotEmpty(recommendation.getText())) {
      msg.setContent(recommendation.getText());
      msg.setRecommendedScenes(recommendation.getScenes());
    }
  }

  /**
   * 解析工作流 Agent 节点回复的片段列表
   */
  private void parseAgentReplyParts(SessionMsgVO msg) {
    if (Strings.CS.startsWith(msg.getContent(), "[")) {
      msg.setAgentReplyParts(JsonUtil.parseJsonRequired(msg.getContent(), new TypeReference<>() {
      }));
      msg.setContent(null);
    }
  }

  /**
   * 解析扩展属性
   */
  private void parseExtParams(SessionMsgVO vo) {
    if (!Strings.CS.startsWith(vo.getExtParams(), "{")) {
      return;
    }

    SessionMsgExtParamsDTO extParams = JsonUtil.parseJsonRequired(vo.getExtParams(), SessionMsgExtParamsDTO.class);
    vo.setToolCallId(extParams.getToolCallId());
    vo.setFileIds(extParams.getFileIds());

    // 消息体移除扩展属性内容
    if (StringUtils.isNotEmpty(vo.getContent())) {
      vo.setContent(vo.getContent().replace(vo.getExtParams(), ""));
    }

    // 补充附件信息，用于历史消息附件渲染
    if (CollectionUtils.isNotEmpty(vo.getFileIds())) {
      List<FileInfoVO> files = fileStoreService.getFileInfoByIds(vo.getFileIds());
      for (FileInfoVO file : CollectionUtils.emptyIfNull(files)) {
        file.setIsPicture(FileTypeUtil.isPicture(file.getFileType()));
      }
      vo.setFiles(files);
    }
  }

  /**
   * 计划相关会话，需要收集完整的计划消息
   */
  private void wrapPlanMessage(List<SessionMsgVO> messages, Long sessionId) {
    Set<Long> planIds = messages.stream().map(SessionMsgVO::getPlanId).filter(Objects::nonNull).collect(Collectors.toSet());
    if (CollectionUtils.isEmpty(planIds)) {
      return;
    }
    // 需要收集所有计划相关的消息
    List<SessionMsgVO> allMessages = sessionMsgMapper.selectSessionMessageForPlan(sessionId, planIds);
    List<Long> ids = messages.stream().map(SessionMsgVO::getId).toList();
    for (SessionMsgVO msg : allMessages) {
      if (!ids.contains(msg.getId())) {
        messages.add(msg);
      }
    }

    // 纠正消息排序
    messages.sort(Comparator.comparing(SessionMsgVO::getSort));

    // 将计划相关的消息，整合到 confirmPlan 类型的消息中
    List<SessionMsgVO> planMessages = messages.stream().filter(p -> ChatMessageType.CONFIRM_PLAN.getCode().equals(p.getType())).toList();
    for (SessionMsgVO message : planMessages) {
      SimplePlanDTO plan = JsonUtil.parseJson(message.getContent(), SimplePlanDTO.class);
      if (plan == null) {
        continue;
      }
      // 以数据库记录的最终数据，更新计划状态
      PlanRecordDTO record = planManageMapper.getPlanRecord(plan.getTenantId(), plan.getPlanId());
      if (record == null) {
        // 针对没有启动的计划，需要特殊处理
        message.setPlan(JsonUtil.parseJson(message.getContent(), SimplePlanDTO.class));
        continue;
      }
      plan.setStatus(record.getStatus());
      List<PlanStepDTO> steps = planManageMapper.selectPlanStepList(plan.getTenantId(), plan.getPlanId());
      List<SimplePlanStepDTO> simplePlanSteps = new ArrayList<>(steps.size());
      plan.setSteps(simplePlanSteps);
      for (PlanStepDTO step : steps) {
        SimplePlanStepDTO simplePlanStep = new SimplePlanStepDTO();
        simplePlanStep.setAgentId(step.getAgentId());
        simplePlanStep.setAgentName(step.getAgentName());
        simplePlanStep.setStepStatus(step.getStepStatus());
        simplePlanStep.setMessages(messages.stream().filter(p -> Objects.equals(plan.getPlanId(), p.getPlanId()))
          .filter(p -> Objects.equals(step.getAgentId(), p.getSceneId())).collect(Collectors.toList()));
        if (StringUtils.isNotEmpty(step.getFlowStepJson())) {
          simplePlanStep.setFlowSteps(JsonUtil.parseJson(step.getFlowStepJson(), new TypeReference<>() {
          }));
        }
        simplePlanSteps.add(simplePlanStep);
      }
      // 更新消息内容
      message.setPlan(plan);
    }

    // 剔除所有计划相关的消息
    messages.removeIf(p -> p.getPlanId() != null && !ChatMessageType.CONFIRM_PLAN.getCode().equals(p.getType()));
  }

  private void wrapParagraphMessage(List<SessionMsgVO> messages, Long sessionId) {
    Set<Long> transactionIds = messages.stream()
      .filter(p -> ChatMessageType.TEXT.getCode().equals(p.getType()))
      .filter(p -> BaseConsts.REPLY_CONTENT_TYPE_PARAGRAPH.equals(p.getContentType()))
      .map(SessionMsgVO::getTransactionId)
      .collect(Collectors.toSet());
    if (CollectionUtils.isEmpty(transactionIds)) {
      return;
    }
    // 收集搜集所有段落消息
    List<SessionMsgVO> allMessages = sessionMsgMapper.selectParagraphSessionMessage(sessionId, transactionIds);
    for (SessionMsgVO msg : allMessages) {
      if (!IterableUtils.matchesAny(messages, p -> Objects.equals(msg.getId(), p.getId()))) {
        messages.add(msg);
      }
    }

    List<Long> excludeIds = new ArrayList<>();
    Map<String, List<SessionMsgVO>> group = messages.stream()
      .filter(p -> BaseConsts.REPLY_CONTENT_TYPE_PARAGRAPH.equals(p.getContentType()))
      .collect(Collectors.groupingBy(p -> p.getTransactionId() + "-" + p.getParagraphGroup()));
    for (Entry<String, List<SessionMsgVO>> entry : group.entrySet()) {
      if (entry.getValue().size() == 1) {
        continue;
      }
      List<String> keys = Arrays.asList(entry.getKey().split("-"));
      if (keys.size() > 1 && StringUtils.isNotEmpty(keys.get(1))) {
        // 存在明确的分组，按照段落顺序排序
        entry.getValue().sort(Comparator.comparing(SessionMsgVO::getSort));
        entry.getValue().sort(Comparator.comparing(p -> ObjectUtils.getIfNull(p.getParagraphSortby(), Integer.MIN_VALUE)));
        collectParagraph(entry.getValue(), excludeIds);
      }
      else {
        // 没有明确的分组，根据 sort 的连续性，作为分组依据
        entry.getValue().sort(Comparator.comparing(SessionMsgVO::getSort));
        // 根据 sort 连续性进行分组
        List<List<SessionMsgVO>> continuousGroups = groupByContinuousSort(entry.getValue());
        // 对每个连续组进行整合
        for (List<SessionMsgVO> list : continuousGroups) {
          collectParagraph(list, excludeIds);
        }
      }
    }
    // 排除已经被整合的段落消息
    if (!excludeIds.isEmpty()) {
      messages.removeIf(msg -> excludeIds.contains(msg.getId()));
    }
  }

  /**
   * 按照分组，整合段落信息，相关文本整合到最后一条消息
   */
  private void collectParagraph(List<SessionMsgVO> paragraphs, List<Long> excludeIds) {
    if (paragraphs.size() == 1) {
      return;
    }
    StringBuilder str = new StringBuilder();
    for (SessionMsgVO msg : paragraphs) {
      str.append(msg.getContent());
      excludeIds.add(msg.getId());
    }
    SessionMsgVO lastMsg = paragraphs.getLast();
    excludeIds.remove(lastMsg.getId());
    lastMsg.setContent(str.toString());
  }

  /**
   * 根据 sort 的连续性进行分组 连续的 sort 值会被分到同一组
   */
  private List<List<SessionMsgVO>> groupByContinuousSort(List<SessionMsgVO> paragraphs) {
    List<List<SessionMsgVO>> groups = new ArrayList<>();
    if (paragraphs.isEmpty()) {
      return groups;
    }
    List<SessionMsgVO> currentGroup = new ArrayList<>();
    currentGroup.add(paragraphs.getFirst());
    for (int i = 1; i < paragraphs.size(); i++) {
      SessionMsgVO current = paragraphs.get(i);
      SessionMsgVO previous = paragraphs.get(i - 1);
      // 判断 sort 是否连续（允许间隔为1）
      if (current.getSort() != null && previous.getSort() != null && current.getSort() - previous.getSort() <= 1) {
        // 连续，加入当前组
        currentGroup.add(current);
      }
      else {
        // 不连续，结束当前组，开始新组
        if (!currentGroup.isEmpty()) {
          groups.add(new ArrayList<>(currentGroup));
        }
        currentGroup.clear();
        currentGroup.add(current);
      }
    }
    // 添加最后一组
    if (!currentGroup.isEmpty()) {
      groups.add(currentGroup);
    }
    return groups;
  }
}
