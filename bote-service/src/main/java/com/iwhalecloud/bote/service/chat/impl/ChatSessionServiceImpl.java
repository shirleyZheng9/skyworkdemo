package com.iwhalecloud.bote.service.chat.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.google.common.io.FileBackedOutputStream;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.FileTypeUtil;
import com.iwhalecloud.bote.common.util.MemoryContentUtil;
import com.iwhalecloud.bote.common.util.SceneContextUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.consts.UserFeedbackEnum;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecordDTO;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.BtDcQaRecordManageMapper;
import com.iwhalecloud.bote.dto.base.FeedbackMessageDTO;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.dto.bot.SimplePlatBotInfoDTO;
import com.iwhalecloud.bote.dto.chat.ChatBotCfgDTO;
import com.iwhalecloud.bote.dto.chat.ChatMessageItemDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestDTO;
import com.iwhalecloud.bote.dto.chat.ChatTraceLogDTO;
import com.iwhalecloud.bote.dto.chat.SceneProcessDTO;
import com.iwhalecloud.bote.dto.chat.SessionDTO;
import com.iwhalecloud.bote.dto.chat.SessionGroupDTO;
import com.iwhalecloud.bote.dto.chat.export.SessionSceneDTO;
import com.iwhalecloud.bote.dto.chat.SessionMsgDTO;
import com.iwhalecloud.bote.dto.chat.SessionMsgFileDTO;
import com.iwhalecloud.bote.dto.chat.SimpleSessionGroupDTO;
import com.iwhalecloud.bote.dto.chat.UpdateMessageMemoryRequestDTO;
import com.iwhalecloud.bote.dto.chat.query.ChatMessageQueryParams;
import com.iwhalecloud.bote.dto.chat.query.ChatSessionQueryParams;
import com.iwhalecloud.bote.dto.chat.query.CreateSessionParams;
import com.iwhalecloud.bote.dto.chat.query.MessageGroupResponse;
import com.iwhalecloud.bote.dto.chat.vo.ChatTraceLogVO;
import com.iwhalecloud.bote.dto.chat.vo.SessionMsgVO;
import com.iwhalecloud.bote.entity.chat.SessionMsgTextEntity;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bote.mapper.bot.PlatBotInfoQueryMapper;
import com.iwhalecloud.bote.mapper.chat.ChatTraceLogMapper;
import com.iwhalecloud.bote.mapper.chat.SceneProcessMapper;
import com.iwhalecloud.bote.mapper.chat.SessionMapper;
import com.iwhalecloud.bote.mapper.chat.SessionMsgFileMapper;
import com.iwhalecloud.bote.mapper.chat.SessionMsgMapper;
import com.iwhalecloud.bote.mapper.chat.SessionMsgTextMapper;
import com.iwhalecloud.bote.service.base.IArchiveMessageService;
import com.iwhalecloud.bote.service.chat.IChatBotCfgService;
import com.iwhalecloud.bote.service.chat.IChatSessionService;
import com.iwhalecloud.bote.service.chat.helper.QuerySessionMsgHelper;
import com.iwhalecloud.bote.service.chat.helper.RuntimeSessionHelper;
import com.iwhalecloud.bote.service.chat.helper.SessionMessageExportHelper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 对话会话应用服务
 *
 * @author Admin
 */
@RequiredArgsConstructor
@Service
public class ChatSessionServiceImpl implements IChatSessionService {
  // @formatter:off
  private final Logger logger = LoggerFactory.getLogger(ChatSessionServiceImpl.class);
  private final BotQueryMapper botQueryMapper;
  private final PlatBotInfoQueryMapper platBotInfoQueryMapper;
  private final SessionMapper sessionMapper;
  private final SessionMsgMapper sessionMsgMapper;
  private final BtDcQaRecordManageMapper btDcQaRecordManageMapper;
  private final SessionMsgTextMapper sessionMsgTextMapper;
  private final SessionMsgFileMapper sessionMsgFileMapper;
  private final SceneProcessMapper sceneProcessMapper;
  private final ChatTraceLogMapper chatTraceLogMapper;
  private final QuerySessionMsgHelper querySessionMsgHelper;
  private final IArchiveMessageService archiveMessageService;
  private final RuntimeSessionHelper runtimeHelper;
  private final IChatBotCfgService chatBotCfgService;
  private final SessionMessageExportHelper sessionMessageExportHelper;
  // @formatter:on

  @Override
  public ResultVO<SessionDTO> findSession(Long sessionId) {
    return ResultVO.success(sessionMapper.getSession(sessionId));
  }

  @Transactional
  @Override
  public ResultVO<SessionDTO> createSession(CreateSessionParams params) {
    Long botId = params.getBotId();
    Long platBotId = params.getPlatBotId();
    if (botId == null && platBotId == null) {
      botId = BaseConsts.BOTE_AI_ID;
    }
    if (Objects.equals(botId, BaseConsts.BOTE_AI_ID)) {
      platBotId = BaseConsts.BOTE_AI_ID;
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    if (platBotId != null && !Objects.equals(platBotId, BaseConsts.BOTE_AI_ID)) {
      // 第三方类型的广场应用，最多只记录一个会话
      SimplePlatBotInfoDTO dto = platBotInfoQueryMapper.getPlatBot(platBotId);
      Assert.notNull(dto, () -> "查询不到有效广场应用");
      if (BaseConsts.PLAT_BOT_TYPE_OTHER.equals(dto.getBotType())) {
        SessionDTO session = sessionMapper.getSessionByPlatBotId(params.getSpaceId(), platBotId, userId);
        if (session != null) {
          sessionMapper.updateSession(session);
          return ResultVO.success(session);
        }
      }
    }
    SessionDTO session = new SessionDTO();
    session.setSessionId(Sequences.BOT_SESSION_ID.next());
    session.setBotId(botId);
    session.setBotTenantId(params.getBotTenantId() == null ? params.getTenantId() : params.getBotTenantId());
    session.setTenantId(params.getTenantId());
    session.setExtSystemId(params.getExtSystemId());
    session.setPlatBotId(platBotId);
    session.setSpaceId(params.getSpaceId());
    session.setSessionTitle(ChatConsts.DEFAULT_SESSION_TITLE);
    session.setBeginTime(new Date());
    if (BooleanUtils.isTrue(params.getIsTest())) {
      session.setIsTest(BaseConsts.TRUE);
    }
    session.setStatusCd(BaseConsts.STATUS_CD_VALID);
    session.setCreatorId(userId);
    session.setUpdatorId(userId);
    sessionMapper.insertSession(session);
    return ResultVO.success(session);
  }

  @Transactional
  @Override
  public ResultVO<Void> deleteSession(Long sessionId) {
    SessionDTO session = sessionMapper.getSession(sessionId);
    // 校验会话创建人
    if (session == null || !Objects.equals(session.getCreatorId(), SessionUtil.getLoginInfo().getUserId())) {
      return ResultVO.fail("会话不存在");
    }
    archiveMessageService.archiveSession(sessionId);
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> clearSessions(@Nullable Long spaceId, Long tenantId, @Nullable Long botId, @Nullable Long botTenantId) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    archiveMessageService.archiveSessionsByTenantIdAndBotId(spaceId, tenantId, botId, botTenantId, userId);
    return ResultVO.success();
  }

  @Transactional
  @Override
  public ResultVO<Void> updateSessionTitle(Long sessionId, String title, Long userId) {
    SessionDTO session = new SessionDTO();
    session.setSessionId(sessionId);
    session.setSessionTitle(title);
    session.setUpdatorId(userId);
    sessionMapper.updateSession(session);
    return ResultVO.success();
  }

  @Override
  public SessionGroupDTO listSessions(ChatSessionQueryParams params) {
    params.setUserId(SessionUtil.getLoginInfo().getUserId());
    Long botId = params.getBotId();
    List<SessionDTO> sessions;
    if (Objects.equals(botId, BaseConsts.BOTE_AI_ID)) {
      // 博特 AI 会话，特殊处理
      params.setIsMark(false);
      params.setIsClose(false);
      sessions = sessionMapper.selectSessionList(params);
    }
    else {
      sessions = sessionMapper.listSessions(params);
    }
    if (CollectionUtils.isEmpty(sessions)) {
      return new SessionGroupDTO();
    }

    // 修改 updated_time 并排序
    for (SessionDTO session : sessions) {
      if (session.getLastMsgTime() != null) {
        session.setUpdatedTime(session.getLastMsgTime());
      }
    }
    sessions.sort(Comparator.comparing(SessionDTO::getUpdatedTime).reversed());

    long today = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH).getTime();
    long sevenDaysAgo = today - 86400000L * 7;
    long thirtyDaysAgo = today - 86400000L * 30;
    List<SessionDTO> todaySessions = new ArrayList<>();
    List<SessionDTO> sevenDaysAgoSessions = new ArrayList<>();
    List<SessionDTO> thirtyDaysAgoSessions = new ArrayList<>();
    List<SessionDTO> thirtyDaysAfterSessions = new ArrayList<>();
    for (SessionDTO session : sessions) {
      long updateTime = session.getUpdatedTime().getTime();
      if (updateTime > today) {
        todaySessions.add(session);
      }
      else if (updateTime > sevenDaysAgo) {
        sevenDaysAgoSessions.add(session);
      }
      else if (updateTime > thirtyDaysAgo) {
        thirtyDaysAgoSessions.add(session);
      }
      else {
        thirtyDaysAfterSessions.add(session);
      }
    }
    return new SessionGroupDTO(todaySessions, sevenDaysAgoSessions, thirtyDaysAgoSessions, thirtyDaysAfterSessions);
  }

  @Override
  public List<SimpleSessionGroupDTO> querySessionList(Long tenantId) {
    return doQuerySession(null, tenantId, false, false, false);
  }

  @Override
  public List<SimpleSessionGroupDTO> querySessionForRuntime(ChatSessionQueryParams params) {
    Long spaceId = params.getSpaceId();
    boolean isMark = ChatConsts.CHAT_BOT_ATTR_MARK.equals(params.getActionType());
    List<SimpleSessionGroupDTO> sessions = doQuerySession(spaceId, null, isMark, true, true);
    if (CollectionUtils.isEmpty(sessions)) {
      List<SimpleSessionGroupDTO> list = new ArrayList<>();
      runtimeHelper.setDefualtPlatBot(list, list, isMark);
      // 设置通用智能体的 isBoteClaw 属性
      list.stream().filter(session -> BaseConsts.BOTE_AI_ID.equals(session.getBotId()))
        .forEach(session -> session.setIsBoteClaw(BaseConsts.TRUE));
      return list;
    }
    List<ChatBotCfgDTO> cfgs = chatBotCfgService.qeuryBotCfgList(spaceId, SessionUtil.getLoginInfo().getUserId());
    for (SimpleSessionGroupDTO group : sessions) {
      // 组装应用配置
      runtimeHelper.setBotCfg(group, cfgs);
      // 组装最后一条消息内容
      SessionMsgVO message = sessionMsgMapper.selectLastMessageForShow(group.getSessionId(), ChatMessageType.allowLastMsgCode());
      if (message != null) {
        group.setLastMsgText(
          StringUtils.abbreviate(message.getContent(), SystemParameter.CHAT_MSG_TEXT_ABBREVIATE_SIZE.getRequiredIntegerValueFromDb()));
      }
    }
    List<SimpleSessionGroupDTO> list = new ArrayList<>(sessions.size());
    // 根据置顶数据，调整会话排序
    List<SimpleSessionGroupDTO> tops = runtimeHelper.queryTop(sessions, cfgs);
    if (CollectionUtils.isNotEmpty(tops)) {
      list.addAll(tops);
    }
    // 补充预置平台应用
    runtimeHelper.setDefualtPlatBot(list, sessions, isMark);
    for (SimpleSessionGroupDTO session : sessions) {
      if (!list.contains(session)) {
        list.add(session);
      }
    }
    // 设置通用智能体的 isBoteClaw 属性
    if (CollectionUtils.isNotEmpty(list)) {
      list.stream().filter(session -> BaseConsts.BOTE_AI_ID.equals(session.getBotId()))
        .forEach(session -> session.setIsBoteClaw(BaseConsts.TRUE));
    }
    return list;
  }

  @Override
  public String getLastMsgText(Long sessionId) {
    // 组装最后一条消息内容
    SessionMsgVO message = sessionMsgMapper.selectLastMessageForShow(sessionId, ChatMessageType.allowLastMsgCode());
    String msgText = "";
    if (message != null) {
      msgText = StringUtils.abbreviate(message.getContent(), SystemParameter.CHAT_MSG_TEXT_ABBREVIATE_SIZE.getRequiredIntegerValueFromDb());
    }
    return msgText;
  }

  private List<SimpleSessionGroupDTO> doQuerySession(@Nullable Long spaceId, @Nullable Long tenantId, boolean isMark, boolean isClose, boolean isRuntime) {
    ChatSessionQueryParams params = new ChatSessionQueryParams();
    params.setSpaceId(spaceId);
    params.setTenantId(tenantId);
    params.setUserId(SessionUtil.getLoginInfo().getUserId());
    params.setIsMark(isMark);
    params.setIsClose(isClose);
    params.setIsTest(false);
    params.setIsRuntime(isRuntime);
    List<SessionDTO> sessions = sessionMapper.selectSessionList(params);
    if (CollectionUtils.isEmpty(sessions)) {
      return Collections.emptyList();
    }
    List<SimpleBotDTO> bots = fetchBotsForSessions(sessions);
    List<SimplePlatBotInfoDTO> platBots = fetchPlatBotsForSessions(sessions);
    List<SimpleSessionGroupDTO> list = new ArrayList<>();
    for (SessionDTO session : sessions) {
      if (isBoteAiConfigFlatSession(isClose, session)) {
        list.add(buildBoteAiFlatSessionGroup(session, bots, platBots));
      }
      else {
        addDedupedSessionGroup(list, session, bots, platBots);
      }
    }
    return list;
  }

  private List<SimpleBotDTO> fetchBotsForSessions(List<SessionDTO> sessions) {
    Set<Long> botIds = sessions.stream().map(SessionDTO::getBotId).filter(Objects::nonNull).collect(Collectors.toSet());
    if (CollectionUtils.isEmpty(botIds)) {
      return new ArrayList<>();
    }
    return botQueryMapper.selectBotListByIds(null, botIds);
  }

  private List<SimplePlatBotInfoDTO> fetchPlatBotsForSessions(List<SessionDTO> sessions) {
    Set<Long> platBotIds = sessions.stream().map(SessionDTO::getPlatBotId).filter(Objects::nonNull).collect(Collectors.toSet());
    if (CollectionUtils.isEmpty(platBotIds)) {
      return new ArrayList<>();
    }
    return platBotInfoQueryMapper.selectPlatBotListByIds(platBotIds);
  }

  private static boolean isBoteAiConfigFlatSession(boolean isClose, SessionDTO session) {
    return !isClose && Objects.equals(session.getBotId(), BaseConsts.BOTE_AI_ID);
  }

  private SimpleSessionGroupDTO buildBoteAiFlatSessionGroup(SessionDTO session, List<SimpleBotDTO> bots, List<SimplePlatBotInfoDTO> platBots) {
    SimpleSessionGroupDTO group = new SimpleSessionGroupDTO();
    group.setIsBot(false);
    group.setEnable(true);
    group.setSessionId(session.getSessionId());
    group.setSessionTitle(session.getSessionTitle());
    group.setUpdatedTime(session.getUpdatedTime());
    group.setTenantId(session.getBotTenantId());
    group.setPlatBotId(session.getPlatBotId());
    SimpleBotDTO bot = IterableUtils.find(bots,
      p -> Objects.equals(p.getBotId(), session.getBotId()) && Objects.equals(p.getBotId(), BaseConsts.BOTE_AI_ID));
    group.setBotName(resolveBoteAiFlatBotName(session, platBots, bot));
    group.setIsPlatformPublish(session.getIsPlatformPublish());
    return group;
  }

  @Nullable
  private static String resolveBoteAiFlatBotName(SessionDTO session, List<SimplePlatBotInfoDTO> platBots, SimpleBotDTO bot) {
    if (session.getPlatBotId() != null) {
      SimplePlatBotInfoDTO platBot = IterableUtils.find(platBots, p -> Objects.equals(p.getPlatBotId(), session.getPlatBotId()));
      if (platBot != null) {
        return platBot.getBotName();
      }
      return bot != null ? bot.getBotName() : null;
    }
    return bot != null ? bot.getBotName() : null;
  }

  private void addDedupedSessionGroup(List<SimpleSessionGroupDTO> list, SessionDTO session, List<SimpleBotDTO> bots,
    List<SimplePlatBotInfoDTO> platBots) {
    boolean exists = IterableUtils.matchesAny(list,
      p -> Objects.equals(p.getBotId(), session.getBotId()) && Objects.equals(p.getPlatBotId(), session.getPlatBotId()));
    if (exists) {
      return;
    }
    SimpleSessionGroupDTO group = buildSessionGroup(session, bots, platBots);
    if (group != null) {
      list.add(group);
    }
  }

  /**
   * 标记已失效的应用，剔除不存在的应用
   */
  @Nullable
  private SimpleSessionGroupDTO buildSessionGroup(SessionDTO session, List<SimpleBotDTO> bots, List<SimplePlatBotInfoDTO> platBots) {
    SimpleSessionGroupDTO group = new SimpleSessionGroupDTO();
    group.setSessionId(session.getSessionId());
    group.setUpdatedTime(session.getUpdatedTime());
    group.setBotId(session.getBotId());
    group.setPlatBotId(session.getPlatBotId());
    group.setIsPlatformPublish(session.getIsPlatformPublish());
    SimpleBotDTO bot = IterableUtils.find(bots,
      p -> Objects.equals(p.getBotId(), session.getBotId()) && (Objects.equals(p.getBotId(), BaseConsts.BOTE_AI_ID) || Objects.equals(p.getTenantId(),
        session.getBotTenantId())));
    if (session.getPlatBotId() != null) {
      SimplePlatBotInfoDTO platBot = IterableUtils.find(platBots, p -> Objects.equals(p.getPlatBotId(), session.getPlatBotId()));
      if (platBot != null) {
        group.setIsBot(BaseConsts.PLAT_BOT_TYPE_PLATFORM.equals(platBot.getBotType()));
        group.setEnable(BaseConsts.BOT_STATUS_PUBLISH.equals(platBot.getStatus()));
        group.setBotName(platBot.getBotName());
        group.setPlatBotType(platBot.getBotType());
        group.setPlatReqUrl(platBot.getReqUrl());
        group.setTenantId(platBot.getOwnerTenantId());
      }
      else {
        if (bot == null) {
          return null;
        }
        group.setIsBot(true);
        group.setEnable(BaseConsts.BOT_STATUS_PUBLISH.equals(bot.getBotStatus()));
        group.setTenantId(bot.getTenantId());
        group.setBotName(bot.getBotName());
        group.setPlatBotType(null);
        group.setPlatReqUrl(null);
        group.setIsBoteClaw(BaseConsts.DATA_FROM_PORTAL_BOT.equals(bot.getDataFrom()) ? BaseConsts.TRUE : BaseConsts.FALSE);
      }
    }
    else {
      if (bot == null) {
        return null;
      }
      group.setIsBot(true);
      group.setEnable(BaseConsts.BOT_STATUS_PUBLISH.equals(bot.getBotStatus()));
      group.setTenantId(bot.getTenantId());
      group.setBotName(bot.getBotName());
      group.setSessionId(session.getSessionId());
      group.setUpdatedTime(session.getUpdatedTime());
      group.setIsBoteClaw(BaseConsts.DATA_FROM_PORTAL_BOT.equals(bot.getDataFrom()) ? BaseConsts.TRUE : BaseConsts.FALSE);
    }
    return group;
  }

  @Override
  @Transactional
  public ResultVO<Void> saveMockMessage(ChatRequestDTO request) {
    SessionMsgVO message = sessionMsgMapper.selectLastMessage(request.getSessionId(), request.getContextId());
    int sort = 1;
    if (message != null) {
      sort = message.getSort() + 1;
    }
    String role = request.getMessage().getRole();
    String msgType = ChatMessageType.TEXT.getCode();
    if (role == null) {
      role = MessageRole.USER.getCode();
    }
    if (MessageRole.USER.getCode().equals(role)) {
      msgType = ChatMessageType.INPUT.getCode();
    }
    SessionMsgDTO msg = new SessionMsgDTO();
    msg.setMsgId(Sequences.BOT_SESSION_MSG_ID.next());
    msg.setSessionId(request.getSessionId());
    msg.setTransactionId(IDUtils.nextId());
    msg.setSceneId(request.getSceneId());
    msg.setContextId(request.getContextId());
    msg.setRole(role);
    msg.setSort(sort);
    msg.setMsgType(msgType);
    msg.setMemorized(BaseConsts.TRUE);
    msg.setMsgText(request.getMessage().getContent());
    msg.setMsgStatus(BaseConsts.STATE_SUCCESS);
    msg.setBeginTime(new Date());
    msg.setEndTime(new Date());

    saveSessionMsgs(Collections.singletonList(msg), SessionUtil.getLoginInfo().getUserId());
    updateSessionEndTime(request.getSessionId(), SessionUtil.getLoginInfo().getUserId(), new Date());
    return ResultVO.success();
  }

  @Override
  public ResultVO<MessageGroupResponse> listMessages(Long tenantId, Long sessionId) {
    MessageGroupResponse response = new MessageGroupResponse();
    response.setMessages(querySessionMsgHelper.execute(tenantId, sessionId));
    setLastScene(sessionId, response);
    return ResultVO.success(response);
  }

  @Override
  public ResultVO<MessageGroupResponse> queryMessagePage(ChatMessageQueryParams queryParams) {
    PageInfo<SessionMsgVO> pageInfo = querySessionMsgHelper.execute(queryParams);
    if (CollectionUtils.isNotEmpty(pageInfo.getList())) {
      pageInfo.getList().forEach(sessionMsgVO -> {
        if (ChatMessageType.FILE_INFO.getCode().equals(sessionMsgVO.getType()) && StringUtils.isNotEmpty(sessionMsgVO.getContent())) {
          sessionMsgVO.setFileInfos(JsonUtil.parseJson(sessionMsgVO.getContent(), new TypeReference<List<FileInfoDTO>>() { }));
        }
      });
    }
    MessageGroupResponse response = new MessageGroupResponse();
    response.setMessagePage(pageInfo);
    if (pageInfo.isIsFirstPage() || (queryParams.getLastMsgSort() != null && queryParams.getLastMsgSort() <= 1)) {
      setLastScene(queryParams.getSessionId(), response);
    }
    return ResultVO.success(response);
  }

  @Override
  public PageInfo<ChatMessageItemDTO> qryMessagesPage(ChatMessageQueryParams queryParams) {
    //noinspection resource
    return sessionMsgMapper.selectMessagesPage(queryParams, queryParams.buildRowBounds()).toPageInfo();
  }

  @Override
  public void exportSessionMessages(ChatMessageQueryParams params, HttpServletResponse response) {
    doExportExcel(response, "sessionMessages-" + System.currentTimeMillis() + ".xlsx", params, (excelWriter, queryParams) -> {
      // 会话列表
      sessionMessageExportHelper.exportSessionList(excelWriter, params, queryParams);
      // 会话详情列表
      sessionMessageExportHelper.exportSessionDetailList(excelWriter, params, queryParams);
    });
  }

  @Override
  public void exportSessionDetails(ChatMessageQueryParams params, HttpServletResponse response) {
    doExportExcel(response, "sessionDetail-" + System.currentTimeMillis() + ".xlsx", params, (excelWriter, queryParams) -> {
      // 会话详情列表
      sessionMessageExportHelper.exportSessionDetailList(excelWriter, params, queryParams);
    });
  }

  /**
   * 通用Excel导出方法
   */
  @SuppressWarnings("UnstableApiUsage")
  private void doExportExcel(HttpServletResponse response, String fileName, ChatMessageQueryParams params,
    BiConsumer<ExcelWriter, List<Object>> exporter) {
    // 直接写入响应的输出流存在问题：
    //    1.出错时可能已经写入一些数据，无法再内部转发到导出错误页面，无法向用户展示错误信息
    //    2.有些导出器会自动关闭输出流，有些不会。而输出流关闭之后就无法再设置或修改响应头
    // 因此先写入一个临时的输出流，成功之后再复制到响应的输出流
    FileBackedOutputStream fbos = new FileBackedOutputStream(1048576 * 3, true);
    try {
      // 先生成Excel文件到临时流
      try (OutputStream os = fbos; ExcelWriter excelWriter = EasyExcel.write(os).build()) {
        // 构造查询参数
        List<Object> queryParams = sessionMessageExportHelper.buildQueryParams(params);
        exporter.accept(excelWriter, queryParams);
      }
      // 成功后再写入响应
      response.setCharacterEncoding(StandardCharsets.UTF_8.name());
      response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
      response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString());
      try (InputStream is = fbos.asByteSource().openStream(); OutputStream os = response.getOutputStream()) {
        IOUtils.copy(is, os);
      }
    }
    catch (Exception e) {
      logger.error("导出Excel失败", e);
      throw new BssException("导出Excel失败", e);
    }
    finally {
      try {
        fbos.reset();
      }
      catch (IOException ignored) {
        // 忽略重置异常
      }
    }
  }

  @Override
  public List<ChatTraceLogVO> qryTraceLogs(Long msgId) {
    List<ChatTraceLogVO> logs = chatTraceLogMapper.selectByMsgId(msgId);
    for (ChatTraceLogVO log : logs) {
      // 在后端解析 JSON 字符串，里面可能有长整型数值，如果由前端解析可能会失真
      if (Strings.CS.startsWith(log.getInputJson(), "{")) {
        log.setInput(JsonUtil.parseJsonRequired(log.getInputJson(), new TypeReference<Map<String, Object>>() {
        }));
      }
      if (StringUtils.isNotEmpty(log.getOutputJson())) {
        if (log.getOutputJson().startsWith("{")) {
          log.setOutput(JsonUtil.parseJsonRequired(log.getOutputJson(), new TypeReference<Map<String, Object>>() {
          }));
        }
        else {
          // 出参可能是字符串
          log.setOutput(log.getOutputJson());
        }
      }
      log.setInputJson(null);
      log.setOutputJson(null);
    }
    return logs;
  }

  @Transactional
  @Override
  public ResultVO<Void> updateLikeType(Long msgId, @Nullable String likeType) {
    sessionMsgMapper.updateSessionMsgLikeType(msgId, likeType, SessionUtil.getLoginInfo().getUserId());
    BtDcQaRecordDTO recordDTO = new BtDcQaRecordDTO();
    recordDTO.setSessionId(String.valueOf(msgId));
    if (ChatConsts.LIKE_TYPE_1.equals(likeType)) {
      recordDTO.setUserFeedback(UserFeedbackEnum.LIKE.getCode());
    }
    else if (ChatConsts.LIKE_TYPE_2.equals(likeType)) {
      recordDTO.setUserFeedback(UserFeedbackEnum.DISLIKE.getCode());
    }
    recordDTO.setFeedbackTime(new Date());
    TenantContextHolder.setIgnore(true);
    btDcQaRecordManageMapper.updateUserFeedback(recordDTO);
    return ResultVO.success();
  }

  @Override
  @Transactional
  public void updateMessageMemory(UpdateMessageMemoryRequestDTO request) {
    SessionMsgDTO msg = sessionMsgMapper.selectMsgById(request.getMsgId(), Boolean.TRUE.equals(request.getAppend()));
    Assert.notNull(msg, "消息不存在");
    Assert.isTrue(SessionUtil.getLoginInfo().getUserId().equals(msg.getCreatorId()), "不允许修改其它用户的消息");
    Assert.isTrue(ChatMessageType.PAGE.getCode().equals(msg.getMsgType()) || ChatMessageType.PAGE_FUNC.getCode().equals(msg.getMsgType()),
      "只能更新页面/页面函数消息的记忆内容");
    // 消息未开启记忆时不处理
    if (!BaseConsts.TRUE.equals(msg.getMemorized())) {
      return;
    }

    Object memoryContent;
    // 替换
    if (!Boolean.TRUE.equals(request.getAppend())) {
      memoryContent = request.getContent();
    }
    // 追加
    else {
      Object oldContent = MemoryContentUtil.parseMemoryContent(msg.getMemoryContent());
      memoryContent = MemoryContentUtil.appendMemoryContent(oldContent, request.getContent());
    }
    String memoryContentStr = MemoryContentUtil.toString(memoryContent);
    int affectedRows = sessionMsgTextMapper.updateMemoryContent(request.getMsgId(), memoryContentStr);
    Assert.isTrue(affectedRows > 0, "更新消息记忆内容失败");
  }

  @Transactional
  @Override
  public void saveSessionMsgs(List<SessionMsgDTO> msgs, Long userId) {
    // 消息文本
    List<SessionMsgTextEntity> texts = new ArrayList<>(msgs.size());
    // 消息附件
    List<SessionMsgFileDTO> files = new ArrayList<>();
    for (SessionMsgDTO msg : msgs) {
      msg.setStatusCd(BaseConsts.STATUS_CD_VALID);
      msg.setCreatorId(userId);
      msg.setUpdatorId(userId);
      msg.setMemorized(ObjectUtils.getIfNull(msg.getMemorized(), BaseConsts.TRUE));

      SessionMsgTextEntity text = new SessionMsgTextEntity();
      text.setMsgId(msg.getMsgId());
      // 空字符串当作 null 存储，以确保其它数据库和 Oracle 存储的数据一致（Oracle 不支持空字符串，会自动转为 null），方便查询（查询时不需要判断空字符串）
      text.setMsgText(StringUtils.defaultIfEmpty(msg.getMsgText(), null));
      text.setExtParams(msg.getExtParams());
      text.setMemoryContent(StringUtils.defaultIfEmpty(msg.getMemoryContent(), null));
      text.setDownloadType(msg.getDownloadType());
      // 下载内容和消息内容相同时，不需要单独存储，以减少存储空间占用
      if (!Objects.equals(msg.getMsgText(), msg.getDownloadContent())) {
        text.setDownloadContent(StringUtils.defaultIfEmpty(msg.getDownloadContent(), null));
      }
      text.setContentType(msg.getContentType());
      text.setParagraphGroup(msg.getParagraphGroup());
      text.setParagraphSortby(msg.getParagraphSortby());
      texts.add(text);

      if (CollectionUtils.isNotEmpty(msg.getFileIds())) {
        for (Long fileId : msg.getFileIds()) {
          SessionMsgFileDTO file = new SessionMsgFileDTO();
          file.setRelaId(Sequences.CHAT_MESSAGE_FILE_ID.next());
          file.setSessionId(msg.getSessionId());
          file.setMsgId(msg.getMsgId());
          file.setFileId(fileId);
          files.add(file);
        }
      }
    }

    sessionMsgMapper.batchInsertSessionMsg(msgs);
    sessionMsgTextMapper.batchInsertSessionMsgText(texts);
    if (!files.isEmpty()) {
      sessionMsgFileMapper.batchInsertSessionMsgFile(files);
    }
  }

  @Override
  @Transactional
  public void updateSessionEndTime(Long sessionId, Long userId, Date endTime) {
    SessionDTO session = new SessionDTO();
    session.setSessionId(sessionId);
    session.setEndTime(endTime);
    session.setUpdatorId(userId);
    sessionMapper.updateSession(session);
  }

  @Override
  @Transactional
  public void saveChatTraceLogs(Long msgId, List<ChatTraceLogDTO> logs) {
    for (int i = 0; i < logs.size(); i++) {
      ChatTraceLogDTO log = logs.get(i);
      log.setLogId(IDUtils.nextId());
      log.setMsgId(msgId);
      log.setSort(i + 1);
    }
    chatTraceLogMapper.batchInsert(logs);
  }

  @Override
  public List<SessionMsgFileDTO> listMessageFiles(Long sessionId) {
    List<SessionMsgFileDTO> sessionMsgFiles = sessionMsgFileMapper.selectSessionMsgFiles(sessionId);
    if (CollectionUtils.isEmpty(sessionMsgFiles)) {
      return Collections.emptyList();
    }
    for (SessionMsgFileDTO sessionMsgFile : sessionMsgFiles) {
      sessionMsgFile.setIsPicture(FileTypeUtil.isPicture(sessionMsgFile.getFileType()));
    }
    return sessionMsgFiles;
  }

  @Override
  public List<SceneProcessDTO> listRunningScenes(Long sessionId) {
    List<SceneProcessDTO> list = sceneProcessMapper.selectRunningScene(sessionId);
    if (CollectionUtils.isEmpty(list)) {
      return Collections.emptyList();
    }
    List<SceneProcessDTO> scenes = new ArrayList<>();
    for (SceneProcessDTO scene : list) {
      // 相同场景只保留最近一个
      if (IterableUtils.matchesAny(scenes, p -> Objects.equals(p.getSceneId(), scene.getSceneId()))) {
        continue;
      }
      scenes.add(scene);
    }
    return scenes;
  }

  @Override
  @Transactional
  public void suspendRunningScene(SceneProcessDTO scene) {
    if (sceneProcessMapper.existsRunningScene(scene)) {
      return;
    }
    // 新增会话默认带出的智能体，如果挂起，需要补充一个执行中记录
    scene.setId(Sequences.CHAT_SCENE_PROCESS_ID.next());
    scene.setChatStatus(ChatConsts.CHAT_SCENE_STATUS_RUNNING);
    scene.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    scene.setStatusCd(BaseConsts.STATUS_CD_VALID);
    sceneProcessMapper.insertSceneProcess(scene);
  }

  private void setLastScene(Long sessionId, MessageGroupResponse response) {
    // 提取上一个未完成的场景信息
    SceneProcessDTO lastScene = sceneProcessMapper.selectLastRunningScene(sessionId);
    if (lastScene != null) {
      response.setSceneId(lastScene.getSceneId());
      response.setSceneName(lastScene.getSceneName());
      response.setContextId(lastScene.getContextId());
    }
    else if (response.getMessagePage() == null || response.getMessagePage().getSize() == 0) {
      // 新建会话，尝试提取机器人配置的默认智能体
      SceneProcessDTO scene = sceneProcessMapper.selectDefaultSceneBySessionId(sessionId);
      if (scene != null) {
        response.setSceneId(scene.getSceneId());
        response.setSceneName(scene.getSceneName());
        response.setContextId(SceneContextUtil.newContextId());
        response.setAutoStartEnabled(true);
      }
      else {
        List<SceneProcessDTO> scenes = sceneProcessMapper.selectSceneListBySessionId(sessionId);
        // 如果当前应用只有一个有效的，也置为默认智能体
        if (CollectionUtils.isNotEmpty(scenes) && scenes.size() == 1) {
          response.setSceneId(scenes.get(0).getSceneId());
          response.setSceneName(scenes.get(0).getSceneName());
          response.setContextId(SceneContextUtil.newContextId());
          response.setAutoStartEnabled(true);
        }
      }
    }
  }

  @Override
  public PageInfo<SessionSceneDTO> querySessionRecordPage(ChatMessageQueryParams queryParams) {
    PageInfo<SessionSceneDTO> pageInfo = sessionMapper.selectSessionScenePage(queryParams, queryParams.buildRowBounds()).toPageInfo();
    if (CollectionUtils.isEmpty(pageInfo.getList())) {
      return pageInfo;
    }
    // 生成唯一ID,用于前端列表展示
    pageInfo.getList().forEach(session -> {
      session.setId(UUID.randomUUID().toString().replace("-", ""));
    });
    return pageInfo;
  }

  @Override
  public List<SessionSceneDTO> querySessionSceneList(Long sessionId) {
    return sessionMapper.selectSceneBySessionId(sessionId);
  }

  @Override
  @Transactional
  public ResultVO<Void> feedbackMessage(FeedbackMessageDTO feedbackMessage) {
    // 记录反馈信息
    Long messageId = feedbackMessage.getMessageId();
    String feedbackReason = feedbackMessage.getFeedbackReason();
    sessionMsgMapper.updateFeedbackMessage(messageId, feedbackReason, SessionUtil.getLoginInfo().getUserId());

    // 同步更新文档中心知识飞轮
    BtDcQaRecordDTO record = new BtDcQaRecordDTO();
    record.setSessionId(String.valueOf(messageId));
    record.setFeedbackReason(feedbackReason);
    record.setFeedbackTime(new Date());
    record.setFeedbackType(feedbackMessage.getFeedbackType());
    TenantContextHolder.setIgnore(true);
    btDcQaRecordManageMapper.updateBtDcQaRecord(record);
    return ResultVO.success();
  }
}
