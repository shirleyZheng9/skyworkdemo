package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.util.SceneContextUtil;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.bot.query.BotSceneQueryParams;
import com.iwhalecloud.bote.dto.chat.ReplyDTO;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ArgsSchema;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BaseInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Bot;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BotInfoType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Content;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ContentType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTarget;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetExecuteResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetInputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetOutputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRunError;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRunStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetVersion;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExecuteEvalTargetParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListSourceParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListSourceResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.UserInfo;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bote.service.bot.IBotSceneManageService;
import com.iwhalecloud.bote.service.orchestration.reply.handlers.NonStreamFlowReplyHandler;
import com.iwhalecloud.bote.service.scene.ISceneChatService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 智能体源评估目标操作服务实现
 */
@Service
@RequiredArgsConstructor
public class BotSourceEvalTargetServiceImpl extends AbstractSourceEvalTargetServiceImpl {
  private static final Logger logger = LoggerFactory.getLogger(BotSourceEvalTargetServiceImpl.class);
  private final ISceneChatService sceneChatService;
  private final IBotSceneManageService botSceneManageService;

  @Override
  public EvalTargetType evalType() {
    return EvalTargetType.BOT;
  }

  @Override
  public void validateInput(Long spaceId, List<ArgsSchema> inputSchema, EvalTargetInputData input) {
    input.validateInputSchema(inputSchema);
  }

  @Override
  public EvalTargetExecuteResult execute(Long spaceId, ExecuteEvalTargetParam param) {
    long startTime = System.currentTimeMillis();
    EvalTargetOutputData outputData = new EvalTargetOutputData();
    EvalTargetRunStatus runStatus = EvalTargetRunStatus.SUCCESS;
    try {
      SceneChatParamsDTO sceneChatParams = buildSceneChatParams(spaceId, param);
      OrchestrationEngineResponse response = sceneChatService.run(sceneChatParams);
      processExecuteResult(response, outputData, (NonStreamFlowReplyHandler) sceneChatParams.getReplyHandler());
    }
    catch (Exception e) {
      handleExecutionError(e, outputData);
      runStatus = EvalTargetRunStatus.FAIL;
    }
    finally {
      setExecutionTime(outputData, startTime);
    }
    return buildExecuteResult(outputData, runStatus);
  }

  private SceneChatParamsDTO buildSceneChatParams(Long spaceId, ExecuteEvalTargetParam param) {
    Long botId = Long.parseLong(param.getSourceTargetId());
    SceneChatParamsDTO sceneChatParams = new SceneChatParamsDTO();
    sceneChatParams.setTenantId(spaceId);
    sceneChatParams.setSceneId(botId);
    if (param.getInput() != null && param.getInput().getInputFields() != null) {
      Map<String, Content> inputFields = param.getInput().getInputFields();
      Content messageContent = inputFields.get("input");
      if (messageContent != null && messageContent.getText() != null) {
        sceneChatParams.setMessageContent(messageContent.getText());
      }
      Map<String, Object> params = new HashMap<>();
      for (Map.Entry<String, Content> entry : inputFields.entrySet()) {
        if (!"message".equals(entry.getKey()) && entry.getValue() != null) {
          params.put(entry.getKey(), entry.getValue().getText());
        }
      }
      sceneChatParams.setParams(params);
    }
    NonStreamFlowReplyHandler replyHandler = new NonStreamFlowReplyHandler(SceneContextUtil.newContextId());
    sceneChatParams.setReplyHandler(replyHandler);
    sceneChatParams.setConversationId(ChatConsts.EVAL_SESSION_ID);
    sceneChatParams.setDebug(false);
    sceneChatParams.setDebugInnerService(false);
    sceneChatParams.setLogEnabled(false);
    sceneChatParams.setContextId(SceneContextUtil.newContextId());
    sceneChatParams.setConversationId(ChatConsts.WECHAT_SESSION_ID);
    sceneChatParams.setHistoryMessagesLoader(ArrayList::new);
    return sceneChatParams;
  }

  private void processExecuteResult(OrchestrationEngineResponse response, EvalTargetOutputData outputData, NonStreamFlowReplyHandler replyHandler) {
    if (Boolean.TRUE.equals(response.getSuccess())) {
      // 从场景聊天参数中获取回复处理器
      if (replyHandler == null) {
        return;
      }
      // 获取回复列表
      List<ReplyDTO> replies = replyHandler.getReplies();
      if (CollectionUtils.isEmpty(replies)) {
        return;
      }
      // 查找文本类型的回复
      Optional<ReplyDTO> textReply = replies.stream()
        .filter(reply -> ChatMessageType.TEXT.equals(reply.getType()) && StringUtils.isNotBlank(reply.getText()))
        .findFirst();
      Map<String, Content> outputFields = new HashMap<>();
      Content content = new Content();
      content.setContentType(ContentType.TEXT);
      // response.getOutput() 是 Map<String, Object>，需要转换为字符串
      content.setText(textReply.map(ReplyDTO::getText).orElse(null));
      outputFields.put("actual_output", content);
      outputData.setOutputFields(outputFields);
    }
    else {
      throw new BssException(response.getFailMsg());
    }
  }

  private void handleExecutionError(Exception e, EvalTargetOutputData outputData) {
    logger.error("执行智能体失败", e);
    EvalTargetRunError error = new EvalTargetRunError();
    error.setCode(500);
    error.setMessage(e.getMessage());
    outputData.setEvalTargetRunError(error);
  }

  private void setExecutionTime(EvalTargetOutputData outputData, long startTime) {
    long timeCostMs = System.currentTimeMillis() - startTime;
    outputData.setTimeConsumingMs(timeCostMs);
  }

  private EvalTargetExecuteResult buildExecuteResult(EvalTargetOutputData outputData, EvalTargetRunStatus runStatus) {
    EvalTargetExecuteResult result = new EvalTargetExecuteResult();
    result.setOutputData(outputData);
    result.setStatus(runStatus);
    return result;
  }

  @Override
  public EvalTarget buildBySource(Long spaceId, String sourceTargetId, String sourceTargetVersion) {
    Long botId = Long.parseLong(sourceTargetId);
    String userId = SessionContext.getCurrentUserId();
    EvalTarget evalTarget = new EvalTarget();
    evalTarget.setSpaceId(spaceId);
    evalTarget.setSourceTargetId(sourceTargetId);
    evalTarget.setEvalTargetType(EvalTargetType.BOT);
    EvalTargetVersion version = new EvalTargetVersion();
    version.setSpaceId(spaceId);
    version.setSourceTargetVersion(sourceTargetVersion);
    version.setEvalTargetType(EvalTargetType.BOT);
    Bot bot = new Bot();
    bot.setBotId(botId);
    bot.setBotVersion(sourceTargetVersion);
    bot.setBotInfoType(BotInfoType.PRODUCT_BOT);
    version.setBot(bot);
    List<ArgsSchema> inputSchema = new ArrayList<>();
    ArgsSchema messageSchema = new ArgsSchema();
    messageSchema.setKey("message");
    messageSchema.setSupportContentTypes(List.of(ContentType.TEXT));
    messageSchema.setJsonSchema("{\"type\":\"string\"}");
    inputSchema.add(messageSchema);
    version.setInputSchema(inputSchema);
    List<ArgsSchema> outputSchema = new ArrayList<>();
    ArgsSchema outputArgsSchema = new ArgsSchema();
    outputArgsSchema.setKey("output");
    outputArgsSchema.setSupportContentTypes(List.of(ContentType.TEXT, ContentType.MULTI_PART));
    outputArgsSchema.setJsonSchema("{\"type\":\"string\"}");
    outputSchema.add(outputArgsSchema);
    version.setOutputSchema(outputSchema);
    BaseInfo baseInfo = new BaseInfo();
    UserInfo createdBy = new UserInfo();
    UserInfo updatedBy = new UserInfo();
    createdBy.setUserId(userId);
    updatedBy.setUserId(userId);
    baseInfo.setCreatedBy(createdBy);
    baseInfo.setUpdatedBy(updatedBy);
    version.setBaseInfo(baseInfo);
    evalTarget.setEvalTargetVersion(version);
    evalTarget.setBaseInfo(baseInfo);
    return evalTarget;
  }

  @Override
  public ListSourceResult listSource(ListSourceParam param) {
    int page = buildPageByCursor(param.getCursor());
    BotSceneQueryParams queryParams = new BotSceneQueryParams();
    queryParams.setTenantId(param.getSpaceId());
    queryParams.setSearchContent(param.getKeyWord());
    queryParams.setPageNum(page);
    queryParams.setPageSize(param.getPageSize() != null ? param.getPageSize() : 20);
    PageInfo<BotSceneDTO> pageInfo = botSceneManageService.queryScenePage(queryParams);
    List<EvalTarget> targets = new ArrayList<>();
    for (BotSceneDTO scene : pageInfo.getList()) {
      EvalTarget target = new EvalTarget();
      target.setSpaceId(param.getSpaceId());
      target.setSourceTargetId(String.valueOf(scene.getSceneId()));
      target.setEvalTargetType(EvalTargetType.BOT);
      EvalTargetVersion version = new EvalTargetVersion();
      version.setSpaceId(param.getSpaceId());
      Bot bot = new Bot();
      bot.setBotId(scene.getSceneId());
      bot.setBotName(scene.getSceneName());
      bot.setDescription(scene.getSceneDesc());
      bot.setBotInfoType(BotInfoType.PRODUCT_BOT);
      version.setBot(bot);
      target.setEvalTargetVersion(version);
      targets.add(target);
    }
    return buildListSourceResult(targets, pageInfo, page);
  }

  @Override
  public void packSourceVersionInfo(Long spaceId, List<EvalTarget> targets) {
    if (targets == null || targets.isEmpty()) {
      return;
    }
    Map<Long, BotSceneDTO> botMap = buildBotMap(spaceId, targets);
    updateTargetsWithBotInfo(targets, botMap);
  }

  private Map<Long, BotSceneDTO> buildBotMap(Long spaceId, List<EvalTarget> targets) {
    Map<Long, BotSceneDTO> botMap = new HashMap<>();
    for (EvalTarget target : targets) {
      if (!isValidBotTarget(target)) {
        continue;
      }
      Long botId = parseBotId(target.getSourceTargetId());
      if (botId != null && !botMap.containsKey(botId)) {
        BotSceneDTO scene = botSceneManageService.getSceneInfo(spaceId, botId);
        if (scene != null) {
          botMap.put(botId, scene);
        }
      }
    }
    return botMap;
  }

  private boolean isValidBotTarget(EvalTarget target) {
    return target.getEvalTargetType() == EvalTargetType.BOT
      && target.getEvalTargetVersion() != null
      && target.getEvalTargetVersion().getBot() != null;
  }

  private Long parseBotId(String sourceTargetId) {
    try {
      return Long.parseLong(sourceTargetId);
    }
    catch (NumberFormatException e) {
      logger.warn("解析Bot ID失败: {}", sourceTargetId, e);
      return null;
    }
  }

  private void updateTargetsWithBotInfo(List<EvalTarget> targets, Map<Long, BotSceneDTO> botMap) {
    for (EvalTarget target : targets) {
      if (isValidBotTarget(target)) {
        updateTargetWithBotInfo(target, botMap);
      }
    }
  }

  private void updateTargetWithBotInfo(EvalTarget target, Map<Long, BotSceneDTO> botMap) {
    Long botId = parseBotId(target.getSourceTargetId());
    if (botId == null) {
      return;
    }
    BotSceneDTO scene = botMap.get(botId);
    if (scene != null) {
      updateTargetWithValidBot(target, scene);
    }
    else {
      markTargetAsDeleted(target);
    }
  }

  private void updateTargetWithValidBot(EvalTarget target, BotSceneDTO scene) {
    target.getEvalTargetVersion().getBot().setBotName(scene.getSceneName());
    if (scene.getSceneDesc() != null) {
      target.getEvalTargetVersion().getBot().setDescription(scene.getSceneDesc());
    }
  }

  private void markTargetAsDeleted(EvalTarget target) {
    if (target.getBaseInfo() == null) {
      target.setBaseInfo(new BaseInfo());
    }
    target.getBaseInfo().setDeletedAt(1L);
  }
}
