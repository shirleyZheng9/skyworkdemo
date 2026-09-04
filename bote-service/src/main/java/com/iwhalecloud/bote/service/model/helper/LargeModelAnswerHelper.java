package com.iwhalecloud.bote.service.model.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.cache.ModelClientCache;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.lang.reflect.Type;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 大模型问答辅助类
 *
 * @author chen.linfa
 * @since 2024-11-12
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class LargeModelAnswerHelper {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final ModelClientCache modelClientCache;

  private final TenantSettingInfoCache tenantSettingInfoCache;

  /**
   * 调用大模型问答，获取输出中的 JSON 内容
   *
   * @param tenantId 租户 ID
   * @param prompt 系统提示词
   * @param message 用户消息内容
   * @param type 输出数据类型
   * @return 输出数据
   */
  @Nullable
  public <V> V chat(Long tenantId, String prompt, String message, Class<V> type) {
    return chat(tenantId, prompt, message, new TypeReference<V>() {
      @Override
      public Type getType() {
        return type;
      }
    });
  }

  /**
   * 大模型问答，输出 JSON 格式内容
   *
   * @param tenantId 租户 ID
   * @param prompt 系统提示词
   * @param message 用户消息内容
   * @param type 输出数据类型
   * @return 输出数据
   */
  @Nullable
  public <V> V chat(Long tenantId, String prompt, String message, TypeReference<V> type) {
    ChatCompletionResponse response = chat(tenantId, prompt, message);
    return parseJson(response, type);
  }

  /**
   * 大模型问答
   */
  public ChatCompletionResponse chat(Long tenantId, String prompt, String message) {
    Long modelId = tenantSettingInfoCache.getModelId(tenantId);
    // 调用大模型
    ChatCompletionRequest request = ChatCompletionRequest.builder()
      .addSystemMessage(prompt)
      .addUserMessage(message)
      .tenantId(tenantId)
      .sourceFrom(ModelConsts.SOURCE_LLM)
      .build();
    LlmClient modelClient = modelClientCache.getLlmClient(tenantId, modelId);
    return modelClient.chatCompletion(request, SseUtil.requestListener);
  }

  /**
   * 生成代码
   */
  public String generateCode(Long tenantId, String prompt, String message) {
    ChatCompletionResponse response = chat(tenantId, prompt, message);
    String content = StringUtils.trimToEmpty(response.getMessageContent());
    // 去掉可能存在的代码块标记
    if (content.startsWith(ModelConsts.BACKTICK) && content.endsWith(ModelConsts.BACKTICK)) {
      content = content.substring(content.indexOf('\n') + 1, content.lastIndexOf(ModelConsts.BACKTICK)).trim();
    }
    return content;
  }

  /**
   * 大模型问答：直接返回大模型输出内容，不做任何格式转换
   */
  public String chatForContent(Long tenantId, String prompt, String message) {
    ChatCompletionResponse response = chat(tenantId, prompt, message);
    return StringUtils.trimToEmpty(response.getMessageContent());
  }

  @Nullable
  private <V> V parseJson(ChatCompletionResponse response, TypeReference<V> type) {
    String content = response.getMessageContent();
    if (content.contains(ModelConsts.JSON_PREFIX)) {
      content = StringUtils.substringBetween(content, ModelConsts.JSON_PREFIX, ModelConsts.JSON_SUFFIX);
    }
    else if (content.contains(ModelConsts.CODE_PREFIX)) {
      content = StringUtils.substringBetween(content, ModelConsts.CODE_PREFIX, ModelConsts.JSON_SUFFIX);
    }
    content = content.trim().replace(ModelConsts.BACKTICK, "");
    if (StringUtils.isEmpty(content) || !content.startsWith("{")) {
      // 输出内容格式错误
      logger.error("Failed to parse json. message={}", response.getMessageContent());
      return null;
    }
    return JsonUtil.parseJson(content, type);
  }

  /**
   * 使用平台预置模型进行问答，输出 JSON 格式内容
   *
   * @param prompt 系统提示词
   * @param message 用户消息内容
   * @param type 输出数据类型
   * @return 输出数据
   */
  @Nullable
  public <V> V chat(String prompt, String message, TypeReference<V> type) {
    // 获取平台通用对话功能使用的大模型
    String modelIdStr = SystemParameter.COMMON_CHAT_MODEL_ID.getValueFromDb();
    Assert.hasLength(modelIdStr, "未配置平台通用对话功能使用的大模型 ID");
    LlmClient llmClient = modelClientCache.getLlmClient(BaseConsts.DEFAULT_TENANT_ID, Long.parseLong(modelIdStr));
    // 调用大模型生成智能体配置
    ChatCompletionRequest request = ChatCompletionRequest.builder()
      .addSystemMessage(prompt)
      .addUserMessage(message)
      .build();
    ChatCompletionResponse response = llmClient.chatCompletion(request);
    return parseJson(response, type);
  }
}
