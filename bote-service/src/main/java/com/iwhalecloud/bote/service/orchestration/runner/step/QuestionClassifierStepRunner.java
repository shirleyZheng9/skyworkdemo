package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.QuestionClassifierStep;
import com.iwhalecloud.bote.dto.orchestration.step.QuestionClassifierStep.QuestionClassification;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.helper.MarkdownHelper;
import com.iwhalecloud.bote.llm.helper.QuestionClassifierHelper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 问题分类步骤执行器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class QuestionClassifierStepRunner extends AbstractLlmStepRunner<QuestionClassifierStep> {

  @Nullable
  @Override
  protected String doRunWithReturn(SceneOrchestrationContext context, QuestionClassifierStep step) {
    return classifyQuestion(context, step, context.getLastStepRunLogOptional().orElse(null));
  }

  /**
   * 对问题进行分类
   *
   * @return 返回下一步节点编码
   */
  @Nullable
  private String classifyQuestion(SceneOrchestrationContext context, QuestionClassifierStep step, @Nullable OrchestrationStepRunLog log) {
    // 获取模型 ID
    Long modelId = getEffectiveModelId(context, step.getModelId());
    LlmClient modelClient = modelClientCache.getLlmClient(context.getTenantId(), modelId);
    // 构造问题分类参数
    String text = StringUtils.defaultString(resolveTemplate(step.getQuestion()));
    String instruction = resolvePrompt(context.getTenantId(), modelId, step.getPromptId(), step.getPromptParameters(), step.getInstruction());
    // 构造消息列表
    List<String> categoryNames = step.getClassifications().stream().map(QuestionClassification::getName).collect(Collectors.toList());
    List<Message> history = loadHistoryMessages(context, step.getMemory());
    List<Message> messages = QuestionClassifierHelper.buildMessages(text, instruction, categoryNames, history);

    // 记录节点入参日志
    if (log != null) {
      Map<String, Object> params = new LinkedHashMap<>();
      params.put("modelId", modelId);
      params.put("modelName", modelClient.defaultModel());
      params.put("messages", formatMessagesForDebugLog(messages));
      log.setInput(params);
    }

    // 调用大模型
    ChatCompletionRequest request = ChatCompletionRequest.builder().tenantId(context.getTenantId()).messages(messages).customModelConfig(step.getCustomModelConfig()).build();
    ChatCompletionResponse response = modelClient.chatCompletion(request, SseUtil.requestListener);
    if (log != null) {
      log.addLog("%s", JsonUtil.toJsonString(response));
    }
    String content = response.getMessageContent();
    Map<String, Object> result = MarkdownHelper.parseJson(content);
    logger.debug("question classifier success: text={}, result={}", text, result);

    String category = MapUtils.getString(result, "category");
    // 设置节点出参。分类结果可能是 null, 因此不能使用 ImmutableMap
    Map<String, String> output = new LinkedHashMap<>();
    output.put("name", category);
    context.setStepOutput(step, output);

    // 找出下一步
    return getNextStep(step, category);
  }

  /**
   * 根据分类结果获取下一步步骤编码
   */
  @Nullable
  public static String getNextStep(QuestionClassifierStep step, @Nullable String category) {
    // 未识别到分类，或者识别到的分类不正确时，都执行 else 分支
    String next;
    if (StringUtils.isNotEmpty(category)) {
      QuestionClassification classification = IterableUtils.find(step.getClassifications(), c -> category.equals(c.getName()));
      next = classification != null ? classification.getNext() : step.getElseStep();
    }
    else {
      next = step.getElseStep();
    }
    return next;
  }

  @Override
  public void debugStep(SceneOrchestrationContext context, QuestionClassifierStep step, OrchestrationStepRunLog log) {
    classifyQuestion(context, step, log);
  }
}
