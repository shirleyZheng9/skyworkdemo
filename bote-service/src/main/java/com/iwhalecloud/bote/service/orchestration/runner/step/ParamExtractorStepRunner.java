package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.util.JsonSchemaUtil;
import com.iwhalecloud.bote.common.util.ParamConverterUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.ParamExtractorStep;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.helper.MarkdownHelper;
import com.iwhalecloud.bote.llm.helper.ParamExtractorHelper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * 参数提取步骤执行器
 *
 * @author bianjp
 * @since 2024-09-04
 */
public class ParamExtractorStepRunner extends AbstractLlmStepRunner<ParamExtractorStep> {
  /** 只有一个参数表达式的匹配模式 */
  private static final Pattern singleParamPattern = Pattern.compile("\\s*\\$\\{([.\\w]+)}\\s*");

  @Override
  protected void doRun(SceneOrchestrationContext context, ParamExtractorStep step) {
    // 参数结构，虚拟根节点
    ParameterSpec spec = step.getParameters();
    Assert.notNull(spec, "参数提取的参数不能为空");
    Assert.isTrue(spec.isObject(), "参数提取的参数根节点必须是对象");
    // 获取模型 ID
    Long modelId = getEffectiveModelId(context, step.getModelId());
    LlmClient modelClient = modelClientCache.getLlmClient(context.getTenantId(), modelId);
    // 输入
    Object input = resolveInput(step);
    // 指令
    String instruction = resolvePrompt(context.getTenantId(), modelId, step.getPromptId(), step.getPromptParameters(), step.getInstruction());
    // 组装消息列表
    List<Message> history = loadHistoryMessages(context, step.getMemory());
    List<Message> messages = ParamExtractorHelper.buildMessages(input, instruction, JsonSchemaUtil.convert(spec), history);

    // 记录节点入参日志
    Optional<OrchestrationStepRunLog> logOptional = context.getLastStepRunLogOptional();
    logOptional.ifPresent(log -> {
      Map<String, Object> params = new LinkedHashMap<>();
      params.put("modelId", modelId);
      params.put("modelName", modelClient.defaultModel());
      params.put("messages", formatMessagesForDebugLog(messages));
      log.setInput(params);
    });

    // 调用大模型
    ChatCompletionRequest request = ChatCompletionRequest.builder().tenantId(context.getTenantId()).messages(messages).customModelConfig(step.getCustomModelConfig()).build();
    ChatCompletionResponse response = modelClient.chatCompletion(request, SseUtil.requestListener);
    logOptional.ifPresent(log -> log.addLog("%s", JsonUtil.toJsonString(response)));
    String content = response.getMessageContent();
    JsonNode node = MarkdownHelper.parseJsonAndAutoFix(content, modelClient);
    Map<String, Object> result = node != null ? JsonUtil.convert(node, new TypeReference<Map<String, Object>>() {
    }) : Collections.emptyMap();
    logger.debug("extract params success: input={}, result={}", input, result);

    // 转换出参结构
    Map<String, Object> convertedResult;
    if (MapUtils.isNotEmpty(result)) {
      // 过滤掉值为空的参数。通义千问有时会返回空对象作为参数值，需要过滤掉以避免转换参数类型报错
      convertedResult = result.entrySet().stream()
        .filter(e -> ObjectUtils.isNotEmpty(e.getValue()))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
      convertedResult = ParamConverterUtil.convertObject("", spec, convertedResult);
    }
    else {
      convertedResult = result;
    }

    // 设置节点出参
    context.setStepOutput(step, convertedResult);
  }

  /**
   * 解析输入
   */
  private Object resolveInput(ParamExtractorStep step) {
    Object input;
    // 如果输入文本中只有一个参数引用表达式，直接使用这个表达式的值，不转为字符串
    // 复杂类型使用 toString() 转字符串的效果不太好，可能会影响参数提取效果。参数提取本来就是以 JSON 格式发给大模型，传复杂类型也没问题，不把 input 转为 JSON 字符串还能减少 token 消耗
    Matcher matcher = singleParamPattern.matcher(step.getInput());
    if (matcher.matches()) {
      String expression = matcher.group(1);
      input = resolveTemplateParam(expression);
      input = input == null ? "" : input;
    }
    else {
      input = StringUtils.defaultString(resolveTemplate(step.getInput()));
    }
    return input;
  }

}
