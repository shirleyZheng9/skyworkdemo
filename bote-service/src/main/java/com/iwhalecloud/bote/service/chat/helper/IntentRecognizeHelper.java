package com.iwhalecloud.bote.service.chat.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.cache.ModelClientCache;
import com.iwhalecloud.bote.cache.SceneCache;
import com.iwhalecloud.bote.cache.SceneIntentCache;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.FreemarkerUtil;
import com.iwhalecloud.bote.common.util.SceneContextUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.bot.SceneIntentDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestMessageDTO;
import com.iwhalecloud.bote.dto.chat.ChatTraceLogDTO.ChatTraceLogBuilder;
import com.iwhalecloud.bote.dto.chat.IntentResultDTO;
import com.iwhalecloud.bote.dto.chat.SceneIntentResultDTO;
import com.iwhalecloud.bote.dto.chat.SceneProcessDTO;
import com.iwhalecloud.bote.dto.chat.query.SimpleUserMessageDTO;
import com.iwhalecloud.bote.dto.intent.IntentLogDTO;
import com.iwhalecloud.bote.dto.planning.SimplePlanDTO.SimplePlanStepDTO;
import com.iwhalecloud.bote.intent.ISceneIntentService;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bote.mapper.chat.SceneProcessMapper;
import com.iwhalecloud.bote.mapper.intent.IntentLogManageMapper;
import com.iwhalecloud.bote.service.bot.IBotQueryService;
import com.iwhalecloud.bote.service.chat.context.ChatContext;
import com.iwhalecloud.bote.service.model.helper.LargeModelAnswerHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 意图识别辅助类
 *
 * @author Admin
 */
@Component
@RequiredArgsConstructor
public class IntentRecognizeHelper {

  // @formatter:off
  private final IntentLogManageMapper intentLogMapper;
  private final SceneProcessMapper sceneProcessMapper;
  private final ISceneIntentService sceneIntentService;
  private final IBotQueryService botQueryService;
  private final BotQueryMapper botQueryMapper;
  private final LargeModelAnswerHelper modelAnswerHelper;
  private final QuerySessionMsgHelper querySessionMsgHelper;
  private final SceneCache sceneCache;
  private final TenantSettingInfoCache tenantSettingInfoCache;
  private final ModelClientCache modelClientCache;
  private final SceneIntentCache sceneIntentCache;
  private final CallModelHelper callModelHelper;
  // @formatter:on

  /**
   * 租户对话窗口，应用识别
   *
   * @param context 会话上下文
   * @param log 会话日志
   * @return 应用识别结果
   */
  @Nullable
  public SimpleBotDTO recognizeBot(ChatContext context, ChatTraceLogBuilder log) {
    Long tenantId = context.getTenantId();
    String message = context.getUserMessage().getContent();
    List<SimpleBotDTO> bots = botQueryService.queryBotListForIntent(tenantId);
    if (CollectionUtils.isEmpty(bots)) {
      log.addLog("当前租户未配置有效的智能应用");
      return null;
    }
    // 识别智能应用的提示词
    String template = SystemParameter.RECOGNIZE_BOT_PROMPT.getValueFromDb();
    Map<String, Object> params = new HashMap<>();
    params.put("bots", bots);
    String prompt = FreemarkerUtil.process(template, params);
    Map<String, Object> result = modelAnswerHelper.chat(tenantId, prompt, message, new TypeReference<Map<String, Object>>() {
    });
    Long botId;
    if (MapUtils.isNotEmpty(result) && result.containsKey("botId")) {
      botId = MapUtils.getLong(result, "botId");
      log.addLog("应用识别: 命中。应用 ", botId);
      return botQueryMapper.selectSimpleBot(context.getTenantId(), botId);
    }
    log.addLog("应用识别: 未命中");
    return null;
  }

  /**
   * 智能体识别
   *
   * @param context 会话上下文
   * @param log 会话日志
   * @return 智能体识别结果
   */
  public IntentResultDTO recognizeAgent(ChatContext context, ChatTraceLogBuilder log) {
    Long tenantId = context.getTenantId();
    Long sessionId = context.getSessionId();
    ChatRequestMessageDTO userMessage = context.getUserMessage();
    String userContent = userMessage.getContent();
    Long userSceneId = context.getRequest().getSceneId();
    String userContextId = context.getRequest().getContextId();
    SceneProcessDTO lastScene = getLastScene(sessionId, userContextId);

    boolean jumpScene = false;
    SceneIntentResultDTO enterScene = null;
    SceneIntentResultDTO exitScene = null;
    // 用户手动退出智能体
    if (Objects.equals(ChatConsts.EXIT_SCENE_ID, userSceneId)) {
      log.addLog("退出智能体，无需识别");
      exitScene = doExitScene(context, lastScene);
    }
    // 用户在智能体中发送消息，检查是否需要跳到其它智能体
    else if (userSceneId != null) {
      log.addLog("请求中包含智能体，检查是否需要二次识别、跳转");
      Triple<Boolean, SceneIntentResultDTO, SceneIntentResultDTO> result = computeEnterScene(context.getRequest(), lastScene, log);
      enterScene = result.getMiddle();
      exitScene = result.getRight();
      jumpScene = result.getLeft();
    }
    // 根据消息内容识别智能体
    else if (StringUtils.isNotBlank(userContent)) {
      boolean exitModel = true;
      if (StringUtils.isNotEmpty(userContextId)) {
        // 用户在大模型对话中发起消息，检查是否跳出，并进行智能体识别
        exitModel = callModelHelper.analyzeExit(context, log);
      }
      if (exitModel) {
        log.addLog("根据消息内容识别");
        boolean planable = context.getRequest().getPlanable() == null || BooleanUtils.isTrue(context.getRequest().getPlanable());
        SimpleUserMessageDTO message = new SimpleUserMessageDTO();
        message.setContent(userContent);
        message.setParams(userMessage.getParams());
        message.setPlanable(planable);
        enterScene = doRecognize(tenantId, context.getRequest().getBotId(), message, log);
        if (enterScene != null) {
          jumpScene = true;
        }
      }
    }

    if (jumpScene) {
      // 进入新智能体，记录智能体进度
      saveSceneProcess(tenantId, context.getBotId(), sessionId, enterScene);
    }
    return handleIntentResult(tenantId, enterScene, exitScene);
  }

  /**
   * 识别计划
   *
   * @param context 会话上下文
   * @param log 会话日志
   * @return 智能体识别结果
   */
  public IntentResultDTO recognizePlan(ChatContext context, ChatTraceLogBuilder log) {
    SimpleUserMessageDTO message = new SimpleUserMessageDTO();
    message.setParams(context.getUserMessage().getParams());
    message.setBusiInfos(context.getRequest().getPlanParams().getBusiInfos());
    SceneIntentResultDTO result = sceneIntentService.recognizePlan(context.getTenantId(), context.getBotId(), message, Optional.of(log));
    return new IntentResultDTO(result, null);
  }

  private SceneIntentResultDTO doExitScene(ChatContext context, @Nullable SceneProcessDTO lastScene) {
    Long lastSceneId = context.getRequest().getLastSceneId();
    if (lastSceneId == null) {
      lastSceneId = Optional.ofNullable(lastScene).map(SceneProcessDTO::getSceneId).orElse(null);
    }
    String lastContextId = context.getRequest().getLastContextId();
    if (StringUtils.isEmpty(lastContextId)) {
      lastContextId = Optional.ofNullable(lastScene).map(SceneProcessDTO::getContextId).orElse(null);
    }
    // 退出上一个智能体
    SceneIntentResultDTO exitScene = new SceneIntentResultDTO(lastSceneId, null);
    // 标记上一个对话智能体已完成
    finishChatScene(context.getSessionId(), lastContextId);
    return exitScene;
  }

  @Nullable
  private SceneProcessDTO getLastScene(Long sessionId, String contextId) {
    SceneProcessDTO scene = sceneProcessMapper.selectLastScene(sessionId, contextId);
    if (scene != null && ChatConsts.CHAT_SCENE_STATUS_RUNNING.equals(scene.getChatStatus())) {
      return scene;
    }
    return null;
  }

  /**
   * 用户消息带有智能体 ID，计算进入、退出智能体、是否智能体跳变等指标
   */
  private Triple<Boolean, SceneIntentResultDTO, SceneIntentResultDTO> computeEnterScene(ChatRequestDTO request, @Nullable SceneProcessDTO lastScene,
    ChatTraceLogBuilder log) {
    String userContent = request.getMessage().getContent();
    Long userSceneId = request.getSceneId();
    String userSceneName = request.getSceneName();
    String userContextId = request.getContextId();
    Long lastSceneId = Optional.ofNullable(lastScene).map(SceneProcessDTO::getSceneId).orElse(null);
    String lastContextId = Optional.ofNullable(lastScene).map(SceneProcessDTO::getContextId).orElse(null);

    boolean jumpScene;
    SceneIntentResultDTO exitScene = null;
    SceneIntentResultDTO enterScene = new SceneIntentResultDTO(userSceneId, userSceneName);
    enterScene.setQuestion(userContent);
    enterScene.setContextId(userContextId);
    if (!Objects.equals(userSceneId, lastSceneId)) {
      jumpScene = true;
      if (lastSceneId != null) {
        log.addLog("退出上一个智能体");
        exitScene = new SceneIntentResultDTO(lastSceneId, null);
        enterScene.setContextId(SceneContextUtil.newContextId());
      }
      else {
        enterScene.setContextId(StringUtils.isNotEmpty(userContextId) ? userContextId : SceneContextUtil.newContextId());
      }
    }
    else {
      if (StringUtils.isEmpty(userContextId)) {
        if (StringUtils.isNotEmpty(lastContextId)) {
          enterScene.setContextId(lastContextId);
        }
        else {
          enterScene.setContextId(SceneContextUtil.newContextId());
        }
      }
      jumpScene = doSecondaryRecognize(request, enterScene, log);
    }
    return Triple.of(jumpScene, enterScene, exitScene);
  }

  /**
   * 进行意图识别
   */
  @Nullable
  private SceneIntentResultDTO doRecognize(Long tenantId, @Nullable Long botId, SimpleUserMessageDTO message, ChatTraceLogBuilder log) {
    if (botId == null) {
      // 首页大模型对话，不做意图识别
      return null;
    }
    SceneIntentResultDTO result = sceneIntentService.recognize(tenantId, botId, message, Optional.of(log));
    if (result == null) {
      log.addLog("意图识别: 未命中");
      return null;
    }
    if (result.getSceneId() != null) {
      log.addLog("意图识别: 命中单个智能体。智能体: %s，问句: %s", result.getSceneName(), result.getQuestion());
      result.setContextId(SceneContextUtil.newContextId());
      result.setHit(true);
      // 记录意图命中日志
      saveIntentLog(tenantId, botId, result.getSceneId(), result.getQuestion());
    }
    else {
      String agentNames = result.getPlan().getSteps().stream().map(SimplePlanStepDTO::getAgentName).collect(Collectors.joining(","));
      log.addLog("意图识别: 命中多个智能体。执行计划: %s", agentNames);
    }
    return result;
  }

  /**
   * 智能体内，根据用户输入信息，检测是否需要智能体跳变，触发二次意图识别
   * <p>1. 添加全局开关</p>
   * <p>2. 例外非用户手工输入消息</p>
   * <p>3. 例外用户上传附件</p>
   * <p>4. 只处理带有可跳变标签的</p>
   * <p>5. 例外执行计划智能体</p>
   */
  private boolean doSecondaryRecognize(ChatRequestDTO request, SceneIntentResultDTO enterScene, ChatTraceLogBuilder log) {
    // 检查是否应该跳过
    if (shouldSkipSecondaryRecognize(request, enterScene, log)) {
      return false;
    }
    log.addLog("开始二次意图识别。问句: %s", enterScene.getQuestion());
    boolean planable = request.getPlanable() == null || BooleanUtils.isTrue(request.getPlanable());
    SimpleUserMessageDTO message = new SimpleUserMessageDTO();
    message.setContent(enterScene.getQuestion());
    message.setParams(request.getMessage().getParams());
    message.setPlanable(planable);
    SceneIntentResultDTO result = doRecognize(request.getTenantId(), request.getBotId(), message, log);
    if (result != null) {
      if (!Objects.equals(enterScene.getSceneId(), result.getSceneId())) {
        log.addLog("二次意图识别: 切换智能体 %s -> %s", enterScene.getSceneName(), result.getSceneName());
        // 检查到智能体跳变
        enterScene.setSceneId(result.getSceneId());
        enterScene.setSceneName(result.getSceneName());
        enterScene.setContextId(result.getContextId());
        return true;
      }
      else {
        log.addLog("二次意图识别: 智能体不变");
      }
    }
    else {
      log.addLog("二次意图识别: 未命中");
    }
    return false;
  }

  /**
   * 检查是否应该跳过二次意图识别
   */
  private boolean shouldSkipSecondaryRecognize(ChatRequestDTO request, SceneIntentResultDTO enterScene, ChatTraceLogBuilder log) {
    boolean skip = !ChatMessageType.INPUT.getCode().equals(request.getMessage().getType()) || StringUtils.isBlank(enterScene.getQuestion())
      || enterScene.getQuestion().contains(ChatConsts.PARAM_FILE_ID) || request.getPlanParams() != null;
    if (skip) {
      log.addLog("非普通消息，跳过二次意图识别");
      return true;
    }
    SimpleBotSceneDTO scene = sceneCache.getScene(request.getTenantId(), enterScene.getSceneId());
    if (Boolean.TRUE.equals(scene.getJumpEnabled())) {
      // 结合上下文，判断是否需要切换智能体
      List<Message> history = querySessionMsgHelper.getHistory(request.getSessionId(), request.getContextId());
      Long modelId = tenantSettingInfoCache.getModelId(request.getTenantId());
      LlmClient client = modelClientCache.getLlmClient(request.getTenantId(), modelId);
      if (analyze(request, enterScene, client, history, enterScene.getQuestion())) {
        log.addLog("智能体包含可跳变标签，结合上下文分析，需要二次意图识别");
        return false;
      }
    }
    return true;
  }

  /**
   * 分析是否需要切换智能体处理
   */
  private boolean analyze(ChatRequestDTO chatRequest, SceneIntentResultDTO enterScene, LlmClient client, List<Message> history, String query) {
    List<SceneIntentDTO> scenes = sceneIntentCache.getScenes(chatRequest.getTenantId(), chatRequest.getBotId());
    SceneIntentDTO scene = IterableUtils.find(scenes, p -> Objects.equals(p.getSceneId(), enterScene.getSceneId()));
    List<SceneIntentDTO> otherScenes = scenes.stream().filter(p -> !Objects.equals(p.getSceneId(), enterScene.getSceneId()))
      .collect(Collectors.toList());
    String template = SystemParameter.ANALYZE_SECONDARY_RECOGNIZE_PROMPT.getValueFromDb();
    Map<String, Object> params = new HashMap<>(2);
    params.put("question", query);
    params.put("scene", scene);
    params.put("scenes", otherScenes);
    String prompt = FreemarkerUtil.process(template, params);

    ChatCompletionRequest request = ChatCompletionRequest.builder()
      .addMessages(history)
      .addUserMessage(prompt)
      .build();
    ChatCompletionResponse response = client.chatCompletion(request);
    String content = response.getMessageContent();
    return StringUtils.isNotEmpty(content) && content.contains("1");
  }

  /**
   * 处理意图识别结果
   */
  private IntentResultDTO handleIntentResult(Long tenantId, @Nullable SceneIntentResultDTO enterScene, @Nullable SceneIntentResultDTO exitScene) {
    IntentResultDTO result = new IntentResultDTO(enterScene, exitScene);
    if (enterScene != null && StringUtils.isEmpty(enterScene.getSceneName())) {
      enterScene.setSceneName(getSceneName(tenantId, enterScene.getSceneId()));
    }
    if (exitScene != null && StringUtils.isEmpty(exitScene.getSceneName())) {
      exitScene.setSceneName(getSceneName(tenantId, exitScene.getSceneId()));
    }
    return result;
  }

  /**
   * 根据智能体 ID 获取智能体意图信息
   */
  @Nullable
  private String getSceneName(Long tenantId, @Nullable Long sceneId) {
    if (sceneId == null || Objects.equals(sceneId, -1L)) {
      return null;
    }
    return sceneCache.getSceneName(tenantId, sceneId);
  }

  /**
   * 记录意图识别日志
   */
  private void saveIntentLog(Long tenantId, Long botId, Long sceneId, String userContent) {
    IntentLogDTO log = new IntentLogDTO();
    log.setLogId(Sequences.CHAT_INTENT_LOG_ID.next());
    log.setTenantId(tenantId);
    log.setBotId(botId);
    log.setSceneId(sceneId);
    log.setContent(userContent);
    log.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    log.setStatusCd(BaseConsts.STATUS_CD_VALID);
    log.setMarkStatus(BaseConsts.FALSE);
    intentLogMapper.insertIntentLog(log);
  }

  private void saveSceneProcess(Long tenantId, Long botId, Long sessionId, SceneIntentResultDTO enterScene) {
    if (enterScene.getPlan() != null) {
      return;
    }
    SimpleBotSceneDTO scene = sceneCache.getScene(tenantId, enterScene.getSceneId());
    // 需要确认启动智能体，跳过
    if (BooleanUtils.isTrue(enterScene.getHit()) && BooleanUtils.isTrue(scene.getConfirmEnabled())) {
      return;
    }
    // 清理命中的标识
    enterScene.setHit(false);
    // 记录会话智能体日志
    SceneProcessDTO sceneLog = new SceneProcessDTO();
    sceneLog.setId(Sequences.CHAT_SCENE_PROCESS_ID.next());
    sceneLog.setTenantId(tenantId);
    sceneLog.setBotId(botId);
    sceneLog.setSessionId(sessionId);
    sceneLog.setSceneId(enterScene.getSceneId());
    sceneLog.setSceneName(scene.getSceneName());
    sceneLog.setContextId(enterScene.getContextId());
    sceneLog.setChatStatus(ChatConsts.CHAT_SCENE_STATUS_RUNNING);
    sceneLog.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    sceneLog.setStatusCd(BaseConsts.STATUS_CD_VALID);
    sceneProcessMapper.insertSceneProcess(sceneLog);
  }

  private void finishChatScene(Long sessionId, @Nullable String contextId) {
    if (StringUtils.isEmpty(contextId)) {
      return;
    }
    SceneProcessDTO log = new SceneProcessDTO();
    log.setSessionId(sessionId);
    log.setContextId(contextId);
    log.setChatStatus(ChatConsts.CHAT_SCENE_STATUS_FINISH);
    log.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    sceneProcessMapper.updateChatSceneStatus(log);
  }
}
