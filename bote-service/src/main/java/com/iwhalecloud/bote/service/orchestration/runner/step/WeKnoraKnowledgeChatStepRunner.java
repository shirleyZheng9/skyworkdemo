package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.doc.module.knowledge.adapter.WeKnoraKnowledgeClient;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeChatResponse;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.WeKnoraKnowledgeChatStep;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.service.orchestration.runner.step.KnowledgeChatStepRunner.SseInvokerHandler;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * WeKnora 知识问答步骤执行器（仅流式 SSE）
 * <p>
 * 直接调用 {@link WeKnoraKnowledgeClient#chatStreamBlocking}；WeKnora 知识库 ID 由步骤配置
 * {@link WeKnoraKnowledgeChatStep#getWeKnoraKnowledgeBaseIds()} 传入，不查询博特知识库缓存。
 * </p>
 *
 * @author huangyunming
 * @since 2026-04-01
 */
public class WeKnoraKnowledgeChatStepRunner extends AbstractKnowledgeStepRunner<WeKnoraKnowledgeChatStep> {

  @Override
  protected void doRun(SceneOrchestrationContext context, WeKnoraKnowledgeChatStep step) {
    Assert.isTrue(step.getStream() == null || Boolean.TRUE.equals(step.getStream()),
      "WeKnora 知识问答仅支持流式，请将 stream 设为 true 或留空");

    List<String> weKnoraKbIds = resolveWeKnoraKnowledgeBaseIds(step);
    Assert.isTrue(CollectionUtils.isNotEmpty(weKnoraKbIds), "WeKnora 知识问答：WeKnora 知识库 ID 不能为空");

    Long modelId = getEffectiveModelId(context, step.getModelId());
    String question = resolveTemplate(step.getQuestion());
    Assert.hasLength(question, "问题不能为空");
    String conversationId = context.getRequest() == null ? null : String.valueOf(context.getRequest().getConversationId());
    // 调试时使用 contextId
    if ("1".equals(conversationId)) {
      conversationId = context.getRequest().getContextId();
    }
    // @formatter:off
    KnowledgeChatParamsDTO params = KnowledgeChatParamsDTO.builder()
      .tenantId(context.getTenantId())
      .botId(context.getBotId())
      .modelId(modelId)
      .conversationId(conversationId)
      .knowledgeIds(Collections.emptyList())
      .knowledgeList(Collections.emptyList())
      .knowledgeType(KnowledgeConsts.KNOWLEDGE_TYPE_WEKNORA)
      .extKnowledgeIds(weKnoraKbIds)
      .question(question)
      .promptTemplate(resolveTemplate(step.getPromptContent()))
      .history(loadHistoryMessages(context, step.getMemory()))
      .withReferences(step.getWithReferences())
      .withQuestions(step.getWithQuestions())
      .thinkingStrategy(step.getThinkingStrategy())
      .customModelConfig(step.getCustomModelConfig())
      .build();
    // @formatter:on
    context.setStepInputLog(params);

    WeKnoraKnowledgeClient weKnoraKnowledgeClient = SpringUtil.getBean(WeKnoraKnowledgeClient.class);

    // 任务工作流：阻塞式流式调用，避免网关超时
    if (!Boolean.TRUE.equals(context.getDsl().getChatflow())) {
      KnowledgeChatResponse response = weKnoraKnowledgeClient.chatStreamBlocking(params, null, SseUtil.requestListener);
      context.setStepOutput(step, buildOutput(response));
      return;
    }
    // 聊天工作流：异步流式输出
    OrchestrationStepRunLog log = context.getLastStepRunLogOptional().orElse(null);
    SseInvoker sseInvoker = new SseInvoker(new SseInvokerHandler(params));
    sseInvoker.setFinishCallback(answer -> context.setStepOutput(step, answer.toMap(), log));
    sseInvoker.setErrorCallback(e -> {
      context.setStepOutput(step, ImmutableMap.of("text", ""), log);
      processStreamException(context, step, e, log);
    });
    context.setStepOutput(step, ImmutableMap.of("text", sseInvoker));
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
  public Object runAsTool(SceneChatParamsDTO sceneChatParams, WeKnoraKnowledgeChatStep step, String toolCallId,
    @Nullable Map<String, Object> toolArguments, Optional<OrchestrationStepRunLog> log) {
    List<String> weKnoraKbIds = resolveWeKnoraKnowledgeBaseIds(step);
    Assert.isTrue(CollectionUtils.isNotEmpty(weKnoraKbIds), "WeKnora 知识问答：WeKnora 知识库 ID 不能为空");

    Long tenantId = sceneChatParams.getTenantId();
    Assert.notNull(tenantId, "调用 WeKnora 知识库时 tenantId 不可能为空");

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
    if (sceneChatParams.getSceneId() != null) {
      stream = true;
      withReferences = true;
      withQuestions = true;
      // 固定使用用户问句
      question = sceneChatParams.getMessageContent();
    }
    // 工作流的大模型节点调用知识库
    else {
      stream = false;
      withReferences = false;
      withQuestions = false;
      // 使用大模型构造的问题
      question = MapUtils.getString(toolArguments, "question");
    }
    Assert.hasLength(question, "WeKnora 知识问答的问题不能为空");
    log.ifPresent(l -> l.setInput(
      ImmutableMap.of("weKnoraKnowledgeBaseIds", weKnoraKbIds, "question", question)));
    // @formatter:off
    KnowledgeChatParamsDTO params = KnowledgeChatParamsDTO.builder()
      .tenantId(tenantId)
      .botId(sceneChatParams.getBotId())
      .modelId(modelId)
      .knowledgeIds(Collections.emptyList())
      .knowledgeList(Collections.emptyList())
      .knowledgeType(KnowledgeConsts.KNOWLEDGE_TYPE_WEKNORA)
      .extKnowledgeIds(weKnoraKbIds)
      .question(question)
      .withReferences(withReferences)
      .withQuestions(withQuestions)
      .withLog(false)
      .build();
    // @formatter:on
    if (stream) {
      SseInvoker sseInvoker = new SseInvoker(new SseInvokerHandler(params));
      return sceneChatParams.getReplyHandler().stream(sseInvoker, null, null, null);
    }
    return knowledgeAnswerHelper.chatByKnowledge(params).getAnswer();
  }

  private List<String> resolveWeKnoraKnowledgeBaseIds(WeKnoraKnowledgeChatStep step) {
    String raw = resolveTemplate(StringUtils.trimToEmpty(step.getWeKnoraKnowledgeBaseIds()));
    if (StringUtils.isBlank(raw)) {
      return Collections.emptyList();
    }
    return Arrays.stream(raw.split(",")).map(String::trim).filter(StringUtils::isNotEmpty).distinct()
      .collect(Collectors.toList());
  }
}
