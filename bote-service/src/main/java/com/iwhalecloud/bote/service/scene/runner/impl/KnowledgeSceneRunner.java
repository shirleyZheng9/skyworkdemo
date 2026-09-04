package com.iwhalecloud.bote.service.scene.runner.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bote.common.util.ScenePromptUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.listener.event.DocKnowledgeQaEventMessage;
import com.iwhalecloud.bote.dto.bot.BotSceneSkillDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.dto.chat.AnswerDTO;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeInfoDTO;
import com.iwhalecloud.bote.dto.knowledge.ResourceExtItem;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.scene.KnowledgeSceneSettingDTO;
import com.iwhalecloud.bote.dto.scene.SceneChatContext;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.mapper.scene.SceneQueryMapper;
import com.iwhalecloud.bote.service.orchestration.runner.step.KnowledgeChatStepRunner.SseInvokerHandler;
import com.iwhalecloud.bote.service.scene.runner.AbstractSceneRunner;
import com.iwhalecloud.bss.litchi.disruptor.DisruptorUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 知识问答智能体执行器
 *
 * @author bianjp
 * @since 2026-04-24
 */
@Service
@RequiredArgsConstructor
public class KnowledgeSceneRunner extends AbstractSceneRunner {
  private static final Logger logger = LoggerFactory.getLogger(KnowledgeSceneRunner.class);

  private final SceneQueryMapper sceneQueryMapper;

  /**
   * 执行知识问答场景
   */
  @Override
  public OrchestrationEngineResponse run(SimpleBotSceneDTO scene, SceneChatParamsDTO sceneChatParams) {
    long startTime = System.currentTimeMillis();
    // 知识库 ID 列表
    List<Long> knowledgeIds = new ArrayList<>();
    // WeKnora 知识库 ID 列表
    List<String> extKnowledgeIds = new ArrayList<>();
    String knowledgeType = "";
    // 知识库资源列表
    List<ResourceExtItem> resourceItems = new ArrayList<>();
    // 查询当前租户知识库配置
    KnowledgeInfoDTO knowledgeInfo = tenantSettingInfoCache.getKnowledgeInfo(sceneChatParams.getTenantId());
    if (StringUtils.isNotEmpty(knowledgeInfo.getKnowledgeType())) {
      List<BotSceneSkillDTO> sceneSkillDTOList = sceneQueryMapper.selectSkillsBySceneId(sceneChatParams.getTenantId(), sceneChatParams.getSceneId());
      Assert.notEmpty(sceneSkillDTOList, "智能体未关联知识库");
      Pair<List<Long>, List<ResourceExtItem>> pair = getKnowledgeExt(sceneSkillDTOList);
      knowledgeIds = pair.getLeft();
      resourceItems = pair.getRight();
    }
    else {
      List<BotSceneSkillDTO> botSceneSkills = sceneQueryMapper.selectKnowledgeSkillsBySceneId(sceneChatParams.getTenantId(),
        sceneChatParams.getSceneId());
      // 智能体关联的知识库技能
      if (CollectionUtils.isNotEmpty(botSceneSkills)) {
        if (botSceneSkills.getFirst() != null && StringUtils.isNotEmpty(botSceneSkills.getFirst().getSkillJson())) {
          Map<String, Object> skillJson = JsonUtil.parseJson(botSceneSkills.getFirst().getSkillJson(), new TypeReference<>() {
          });
          knowledgeType = MapUtils.getString(skillJson, "knowledgeType");
        }
        knowledgeIds = botSceneSkills.stream()
          .filter(Objects::nonNull)
          .map(BotSceneSkillDTO::getSkillId)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
        extKnowledgeIds = botSceneSkills.stream().map(BotSceneSkillDTO::getExtSkillId).toList();
        Assert.isTrue(CollectionUtils.isNotEmpty(knowledgeIds) || CollectionUtils.isNotEmpty(extKnowledgeIds),
          "智能体未关联知识库");
      }
    }
    // 知识问答配置
    String knowledgeSettingJson = sceneQueryMapper.selectKnowledgeSettingBySceneId(sceneChatParams.getTenantId(), sceneChatParams.getSceneId());
    KnowledgeSceneSettingDTO knowledgeSetting = JsonUtil.parseJson(knowledgeSettingJson, KnowledgeSceneSettingDTO.class);
    knowledgeSetting = knowledgeSetting != null ? knowledgeSetting : KnowledgeSceneSettingDTO.DEFAULT;
    // 提示词
    String promptTemplate = ScenePromptUtil.resolveScenePrompt(mockOrchestrationContext(sceneChatParams), sceneChatParams.getTenantId(), sceneChatParams.getSceneId(), null, Collections.emptyList());
    Long modelId = resolveModelId(sceneChatParams);
    SceneChatContext context = buildContext(scene, sceneChatParams, null, null);
    // 历史消息，列表需要拷贝一份，避免列表变化影响调用和入参日志
    List<Message> history = context.getMessages().size() > 1 ? new ArrayList<>(context.getMessages().subList(0, context.getMessages().size() - 1)) : Collections.emptyList();
    KnowledgeChatParamsDTO knowledgeChatParams = KnowledgeChatParamsDTO.builder()
      .tenantId(sceneChatParams.getTenantId())
      .clientId(sceneChatParams.getClientId())
      .advanceRecord(Boolean.TRUE)
      .botId(sceneChatParams.getBotId())
      .modelId(modelId)
      .knowledgeIds(knowledgeIds)
      .extKnowledgeIds(extKnowledgeIds)
      .knowledgeType(knowledgeType)
      .resourceItems(resourceItems)
      .question(sceneChatParams.getMessageContent())
      .promptTemplate(promptTemplate)
      .history(history)
      .withReferences(Boolean.TRUE.equals(knowledgeSetting.getReferencesEnabled()))
      .withQuestions(Boolean.TRUE.equals(knowledgeSetting.getQuestionsEnabled()))
      .withLog(Boolean.TRUE.equals(knowledgeSetting.getChatLogEnabled()))
      .thinkingStrategy(knowledgeSetting.getThinkingStrategy())
      .customModelConfig(sceneChatParams.getCustomModelConfig())
      .build();

    Optional<OrchestrationStepRunLog> log = context.newStepLog("knowledgeChat", "知识问答");
    log.ifPresent(l -> l.setInput(knowledgeChatParams));

    AnswerDTO answer = sceneChatParams.getReplyHandler().stream(new SseInvoker(new SseInvokerHandler(knowledgeChatParams)), null, null, null);
    // 记录完整回复内容到数据库
    context.addMessage(new AssistantMessage(answer.getText()));
    saveNewMessages(context);
    log.ifPresent(l -> l.succeed(answer.toMap()));
    OrchestrationEngineResponse response = new OrchestrationEngineResponse();
    response.setSuccess(true);
    response.setStepLogs(context.getStepLogs());
    response.setTimeSpent(System.currentTimeMillis() - startTime);
    if (sceneChatParams.getReplyHandler() != null) {
      response.setReplies(sceneChatParams.getReplyHandler().getReplies());
    }
    saveQuestionAndAnswerRecord(response.getTimeSpent(), answer, knowledgeChatParams);
    if (Boolean.TRUE.equals(sceneChatParams.getLogEnabled())) {
      flowTraceLogService.addLog(sceneChatParams, response, new Date(startTime));
    }
    return response;
  }

  private Pair<List<Long>, List<ResourceExtItem>> getKnowledgeExt(List<BotSceneSkillDTO> sceneSkillDTOList) {
    String skillJson = sceneSkillDTOList.getFirst().getSkillJson();
    boolean isDoc = skillJson != null && skillJson.contains("docId");
    List<Long> knowledgeIds = new ArrayList<>();
    List<ResourceExtItem> sourceItems = new ArrayList<>();
    for (BotSceneSkillDTO skill : sceneSkillDTOList) {
      if (isDoc) {
        ResourceExtItem item = new ResourceExtItem();
        item.setResourceWid(skill.getSkillId().toString());
        item.setResourceType(KnowledgeConsts.KNOW_BASE_RESOURCE);
        sourceItems.add(item);
      }
      else {
        knowledgeIds.add(skill.getSkillId());
      }
    }
    return Pair.of(knowledgeIds, sourceItems);
  }


  /**
   * 保存消息记录
   */
  private void saveQuestionAndAnswerRecord(Long timeSpent, AnswerDTO answer, KnowledgeChatParamsDTO knowledgeChatParams) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    try {
      DocKnowledgeQaEventMessage eventMessage = new DocKnowledgeQaEventMessage();
      eventMessage.setChatLogId(answer.getChatLogId());
      eventMessage.setActionType(DocBaseConsts.KNOWLEDGE_QA_TYPE_QA);
      eventMessage.setText(answer.getText());
      eventMessage.setClientId(answer.getMsgId());
      eventMessage.setKnowledgeIds(knowledgeChatParams.getKnowledgeIds());
      eventMessage.setTenantId(knowledgeChatParams.getTenantId());
      eventMessage.setQuestion(knowledgeChatParams.getQuestion());
      eventMessage.setBotId(knowledgeChatParams.getBotId());
      eventMessage.setUserId(userId);
      eventMessage.setReferences(answer.getReferences());
      eventMessage.setTimeSpent(timeSpent);

      DisruptorUtil.getInstance().produce(eventMessage); // ← 异步发送
    }
    catch (Exception e) {
      logger.error("记录飞轮记录的时候报错", e);
    }
  }

}
