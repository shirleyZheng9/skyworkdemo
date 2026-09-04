package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeChatResponse;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.KnowledgeGraphKnowledgeChatStep;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.service.orchestration.runner.step.KnowledgeChatStepRunner.SseInvokerHandler;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * knowledgeGraph 知识问答步骤执行器
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
public class KnowledgeGraphKnowledgeChatStepRunner extends AbstractKnowledgeStepRunner<KnowledgeGraphKnowledgeChatStep> {

  @Override
  protected void doRun(SceneOrchestrationContext context, KnowledgeGraphKnowledgeChatStep step) {
    String knowledgeGraphName = step.getKnowledgeGraphName();
    Assert.isTrue(StringUtils.isNotEmpty(knowledgeGraphName), "knowledgeGraph 知识库不能为空");
    String question = resolveTemplate(step.getQuestion());
    Assert.hasLength(question, "问题不能为空");
    String conversationId = context.getRequest() == null ? null : String.valueOf(context.getRequest().getConversationId());
    // 调试时使用 contextId
    if ("1".equals(conversationId)) {
      conversationId = context.getRequest().getContextId();
    }
    Long modelId = getEffectiveModelId(context, step.getModelId());
    // @formatter:off
    KnowledgeChatParamsDTO params = KnowledgeChatParamsDTO.builder()
      .tenantId(context.getTenantId())
      .botId(context.getBotId())
      .modelId(modelId)
      .extKnowledgeIds(List.of(knowledgeGraphName))
      .question(question)
      .knowledgeType(KnowledgeConsts.KNOWLEDGE_TYPE_KNOWLEDGE_GRAPH)
      .history(null)
      .promptTemplate(resolveTemplate(step.getPromptContent()))
      .withReferences(Boolean.TRUE.equals(step.getWithReferences()))
      .withQuestions(Boolean.TRUE.equals(step.getWithQuestions()))
      .withLog(Boolean.TRUE.equals(step.getWithChatLog()))
      .conversationId(conversationId)
      .build();
    // @formatter:on
    context.setStepInputLog(params);
    boolean stream = Boolean.TRUE.equals(step.getStream());
    if (!stream) {
      context.setStepOutput(step, buildOutput(knowledgeAnswerHelper.chatByKnowledge(params)));
      return;
    }
    // 非 chatflow 模式下，直接返回结果
    if (!Boolean.TRUE.equals(context.getDsl().getChatflow())) {
      KnowledgeChatResponse response = knowledgeAnswerHelper.chatStreamByKnowledgeBlocking(params, null, SseUtil.requestListener);
      context.setStepOutput(step, buildOutput(response));
      return;
    }
    OrchestrationStepRunLog log = context.getLastStepRunLogOptional().orElse(null);
    SseInvoker sseInvoker = new SseInvoker(new SseInvokerHandler(params));
    sseInvoker.setFinishCallback(answer -> context.setStepOutput(step, answer.toMap(), log));
    sseInvoker.setErrorCallback(e -> {
      context.setStepOutput(step, ImmutableMap.of("text", ""), log);
      processStreamException(context, step, e, log);
    });
    context.setStepOutput(step, ImmutableMap.of("text", sseInvoker));
  }

  @Override
  public Object runAsTool(SceneChatParamsDTO sceneChatParams, KnowledgeGraphKnowledgeChatStep step, String toolCallId,
                          @Nullable Map<String, Object> toolArguments, Optional<OrchestrationStepRunLog> log) {
    Assert.isTrue(StringUtils.isNotEmpty(step.getKnowledgeGraphName()), "knowledgeGraph 知识库不能为空");
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
    log.ifPresent(l -> l.setInput(ImmutableMap.of("knowledgeGraphName", step.getKnowledgeGraphName(), "question", question)));
    Assert.hasLength(question, "knowledgeGraph 知识问答的问题不能为空");
    // @formatter:off
    KnowledgeChatParamsDTO params = KnowledgeChatParamsDTO.builder()
      .tenantId(sceneChatParams.getTenantId())
      .botId(sceneChatParams.getBotId())
      .extKnowledgeIds(List.of(step.getKnowledgeGraphName()))
      .knowledgeType(KnowledgeConsts.KNOWLEDGE_TYPE_KNOWLEDGE_GRAPH)
      .question(question)
      .withReferences(withReferences)
      .withQuestions(withQuestions)
      .conversationId(sceneChatParams.getConversationId() == null ? null : String.valueOf(sceneChatParams.getConversationId()))
      .build();
    // @formatter:on
    // 流式输出
    if (stream) {
      SseInvoker sseInvoker = new SseInvoker(new SseInvokerHandler(params));
      return sceneChatParams.getReplyHandler().stream(sseInvoker, null, null, null);
    }
    return knowledgeAnswerHelper.chatByKnowledge(params).getAnswer();
  }

  /**
   * 构造步骤输出结构
   */
  private Map<String, Object> buildOutput(KnowledgeChatResponse response) {
    Map<String, Object> output = new LinkedHashMap<>();
    output.put("text", response.getAnswer());
    if (CollectionUtils.isNotEmpty(response.getReferences())) {
      output.put("references", response.getReferences());
    }
    return output;
  }
}
