package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.sse.event.QuestionsSseEvent;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeChatResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeInfoDTO;
import com.iwhalecloud.bote.dto.knowledge.ResourceExtItem;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.KnowledgeChatStep;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.service.chat.helper.CallKnowledgeHelper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 知识问答步骤执行器
 *
 * @author bianjp
 * @since 2024-12-18
 */
public class KnowledgeChatStepRunner extends AbstractKnowledgeStepRunner<KnowledgeChatStep> {
  private static final CallKnowledgeHelper callKnowledgeHelper = SpringUtil.getBean(CallKnowledgeHelper.class);
  private static final TenantSettingInfoCache tenantSettingInfoCache = SpringUtil.getBean(TenantSettingInfoCache.class);

  @Override
  protected void doRun(SceneOrchestrationContext context, KnowledgeChatStep step) {
    KnowledgeInfoDTO knowledgeInfo = tenantSettingInfoCache.getKnowledgeInfo(context.getTenantId());
    List<Long> knowledgeIds;
    List<ResourceExtItem> resourceItems = new ArrayList<>();
    if (StringUtils.isNotEmpty(knowledgeInfo.getKnowledgeType())) {
      knowledgeIds = resolveKnowledgeIdsFormExt(StringUtils.defaultString(step.getKnowledgeExt()));
      resourceItems = resolveResourceIdsFormExt(StringUtils.defaultString(step.getResourceExt()));
      Assert.isTrue(CollectionUtils.isNotEmpty(knowledgeIds) || CollectionUtils.isNotEmpty(resourceItems), "知识库和文档不能同时为空");
    }
    else {
      knowledgeIds = resolveKnowledgeIds(StringUtils.trimToEmpty(step.getKnowledgeId()));
    }
    List<Long> documentIds = resolveDocumentIds(StringUtils.trimToEmpty(step.getDocumentId()));
    Long modelId = getEffectiveModelId(context, step.getModelId());
    String question = resolveTemplate(step.getQuestion());
    Assert.hasLength(question, "问题不能为空");
    boolean stream = Boolean.TRUE.equals(step.getStream());
    // 只有流式输出支持参考文档、追问
    boolean withReferences = stream && Boolean.TRUE.equals(step.getWithReferences());
    boolean withQuestions = stream && Boolean.TRUE.equals(step.getWithQuestions());
    boolean withChatLog = stream && Boolean.TRUE.equals(step.getWithChatLog());

    KnowledgeChatParamsDTO params = KnowledgeChatParamsDTO.builder()
      .tenantId(context.getTenantId())
      .botId(context.getBotId())
      .modelId(modelId)
      .knowledgeIds(knowledgeIds)
      .documentIds(documentIds)
      .resourceItems(resourceItems)
      .question(question)
      .promptTemplate(resolveTemplate(step.getPromptContent()))
      .history(loadHistoryMessages(context, step.getMemory()))
      .withReferences(withReferences)
      .withQuestions(withQuestions)
      .withLog(withChatLog)
      .thinkingStrategy(step.getThinkingStrategy())
      .customModelConfig(step.getCustomModelConfig())
      .build();

    OrchestrationStepRunLog log = context.getLastStepRunLogOptional().orElse(null);
    if (log != null) {
      // 拷贝一份，避免后面的代码修改 params 影响入参日志。要避免在日志中包含无关信息（尤其不能泄露 modelInfo 中的密钥）
      log.setInput(JsonUtil.convert(params, new TypeReference<Map<String, Object>>() {
      }));
    }

    // 流式输出
    if (stream) {
      // 如果是任务工作流，阻塞等待。请求耗时太长时，使用流式调用可以避免网关超时
      if (!Boolean.TRUE.equals(context.getDsl().getChatflow())) {
        KnowledgeChatResponse response = knowledgeAnswerHelper.chatStreamByKnowledgeBlocking(params, null, SseUtil.requestListener);
        context.setStepOutput(step, buildOutput(response));
        return;
      }
      SseInvoker sseInvoker = new SseInvoker(new SseInvokerHandler(params));
      // 流式输出完成后，修改节点出参为最终输出内容
      sseInvoker.setFinishCallback(answer -> context.setStepOutput(step, answer.toMap(), log));
      sseInvoker.setErrorCallback(e -> {
        // 删除出参中的 SseInvoker, 避免在异常分支中重复触发
        context.setStepOutput(step, ImmutableMap.of("text", ""), log);
        processStreamException(context, step, e, log);
      });
      context.setStepOutput(step, ImmutableMap.of("text", sseInvoker));
    }
    else {
      KnowledgeChatResponse response = knowledgeAnswerHelper.chatByKnowledge(params);
      context.setStepOutput(step, buildOutput(response));
    }
  }

  /**
   * 构造出参
   */
  private Map<String, Object> buildOutput(KnowledgeChatResponse response) {
    Map<String, Object> output = new LinkedHashMap<>();
    if (StringUtils.isNotEmpty(response.getChatLogId())) {
      output.put("chatLogId", response.getChatLogId());
    }
    output.put("text", response.getAnswer());
    if (StringUtils.isNotEmpty(response.getReasoning())) {
      output.put("reasoning", response.getReasoning());
    }
    if (CollectionUtils.isNotEmpty(response.getReferences())) {
      output.put("references", response.getReferences());
    }
    return output;
  }

  @Override
  public Object runAsTool(SceneChatParamsDTO sceneChatParams, KnowledgeChatStep step, String toolCallId, @Nullable Map<String, Object> toolArguments,
                          Optional<OrchestrationStepRunLog> log) {
    // 知识库 ID 必然是常量值，且是单个值
    KnowledgeInfoDTO knowledgeInfo = tenantSettingInfoCache.getKnowledgeInfo(sceneChatParams.getTenantId());
    List<Long> knowledgeIds;
    List<ResourceExtItem> resourceItems = new ArrayList<>();
    if (StringUtils.isNotEmpty(knowledgeInfo.getKnowledgeType())) {
      knowledgeIds = resolveKnowledgeIdsFormExt(StringUtils.defaultString(step.getKnowledgeExt()));
      resourceItems = resolveResourceIdsFormExt(StringUtils.defaultString(step.getResourceExt()));
      Assert.isTrue(!knowledgeIds.isEmpty() || !resourceItems.isEmpty(), "知识库和文档不能同时为空");
    }
    else {
      knowledgeIds = resolveKnowledgeIds(StringUtils.trimToEmpty(step.getKnowledgeId()));
    }
    Long tenantId = sceneChatParams.getTenantId();
    Assert.notNull(tenantId, "调用知识库时 tenantId 不可能为空");
    Long modelId = getEffectiveModelId(tenantId, step.getModelId());
    // 是否使用流式输出
    boolean stream;
    // 是否开启参考文档
    boolean withReferences;
    // 是否开启追问
    boolean withQuestions;
    // 问句
    String question;

    // 简单场景调用知识库
    if (sceneChatParams.getSceneId() != null && !sceneChatParams.isGeneralAgent()) {
      stream = true;
      withReferences = true;
      withQuestions = true;
      // 固定使用用户问句
      question = sceneChatParams.getMessageContent();
    }
    // 工作流的大模型节点调用知识库、通用智能体调用知识库
    else {
      stream = false;
      withReferences = false;
      withQuestions = false;
      // 使用大模型构造的问题
      question = MapUtils.getString(toolArguments, "question");
    }
    Assert.isTrue(StringUtils.isNotEmpty(question), "知识问答的问题不能为空");
    log.ifPresent(l -> l.setInput(ImmutableMap.of("knowledgeId", knowledgeIds, "question", question)));

    KnowledgeChatParamsDTO params = KnowledgeChatParamsDTO.builder()
      .tenantId(tenantId)
      .botId(sceneChatParams.getBotId())
      .modelId(modelId)
      .knowledgeIds(knowledgeIds)
      .resourceItems(resourceItems)
      .question(question)
      .withReferences(withReferences)
      .withQuestions(withQuestions)
      .build();

    // 流式输出
    if (stream) {
      SseInvoker sseInvoker = new SseInvoker(new SseInvokerHandler(params));
      return sceneChatParams.getReplyHandler().stream(sseInvoker, null, null, null);
    }
    return knowledgeAnswerHelper.chatByKnowledge(params).getAnswer();
  }

  /**
   * 流式调用处理器
   */
  @RequiredArgsConstructor
  @SuppressWarnings("PMD.GuardLogStatement")
  public static class SseInvokerHandler implements Consumer<Consumer<SseEvent>> {
    private static final Logger logger = LoggerFactory.getLogger(SseInvokerHandler.class);

    private final KnowledgeChatParamsDTO params;

    @Override
    public void accept(Consumer<SseEvent> partialHandler) {
      Consumer<SseEvent> wrappedPartialHandler = wrapPartialHandler(partialHandler);

      // 调用知识库
      KnowledgeChatResponse response;
      try {
        response = knowledgeAnswerHelper.chatStreamByKnowledgeBlocking(params, wrappedPartialHandler, SseUtil.requestListener);
      }
      catch (BssException e) {
        throw e;
      }
      catch (Exception e) {
        logger.error("Knowledge chat failed", e);
        throw new BssException("知识问答失败: " + ExpUtil.getMsg(e), e);
      }

      // 成功时生成追问的问题
      if (params.isWithQuestions() && CollectionUtils.isEmpty(response.getQuestions())) {
        try {
          List<String> questions = callKnowledgeHelper.generateRelatedQuestions(params.getTenantId(), response.getAnswer());
          if (!questions.isEmpty()) {
            partialHandler.accept(SseEvent.ofQuestions(questions));
          }
        }
        catch (Exception e2) {
          // 追问不太重要，忽略失败，避免影响主要功能
          logger.error("Failed to generate related questions: tenantId={}, botId={}", params.getTenantId(), params.getBotId(), e2);
        }
      }
    }

    /**
     * 封装片段处理器
     */
    private Consumer<SseEvent> wrapPartialHandler(Consumer<SseEvent> partialHandler) {
      if (params.isWithQuestions()) {
        return partialHandler;
      }
      // 未开启追问时，忽略追问事件
      return event -> {
        if (!(event instanceof QuestionsSseEvent)) {
          partialHandler.accept(event);
        }
      };
    }
  }
}
