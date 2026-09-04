package com.iwhalecloud.bote.controller.model;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.cache.ModelClientCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.util.LlmTraceUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Hidden;
import java.util.Map;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模型代理接口
 *
 * <p>以标准的 OpenAI 接口协议暴露本平台配置的模型，目前仅提供给 DocChain 使用，以便 DocChain 能复用我们平台对接的非标准协议，实现知识问答功能。
 * 不要开放给其他平台、业务系统使用。</p>
 *
 * @author bianjp
 * @since 2025-05-21
 */
@RestController
@RequestMapping(BaseConsts.API_PREFIX + "modelProxy")
@Hidden
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class ModelProxyController {
  private static final Logger logger = LoggerFactory.getLogger(ModelProxyController.class);

  private final ModelClientCache modelClientCache;

  /**
   * 会话补全接口
   */
  @PostMapping(path = "v1/chat/completions", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
  @SuppressFBWarnings("CRLF_INJECTION_LOGS")
  public Object chatCompletion(@RequestBody String requestJson, @RequestParam(name = "traceId", required = false) String traceId,
                               @RequestParam(name = "maxTokens", required = false) Integer maxTokens,
                               @RequestParam(name = "temperature", required = false) Double temperature) {
    logger.trace("Received llm chat completion request: {}", requestJson);
    ChatCompletionRequest request;
    LlmClient client;
    // 解析请求，获取客户端实例
    try {
      request = JsonUtil.parseJsonRequired(requestJson, ChatCompletionRequest.class);
      client = getLlmClient(request.getModel());
      // 修改请求参数中的模型
      request.setModel(client.defaultModel());
    }
    catch (Exception e) {
      return handleException(requestJson, e);
    }

    // 提供给 DocChain 调用大模型时，需要透传 traceId: KnowledgeAnswerHelper#setModelInfo
    request.setTraceId(traceId);
    LlmTraceUtil.setTraceId(traceId);

    // 设置自定义的最大令牌数和温度参数
    request.setMaxTokens(maxTokens != null ? maxTokens : request.getMaxTokens());
    request.setTemperature(temperature != null ? temperature : request.getTemperature());

    // 非流式
    if (!Boolean.TRUE.equals(request.getStream())) {
      try {
        return client.chatCompletion(request);
      }
      catch (Exception e) {
        return handleException(requestJson, e);
      }
    }

    // 流式
    return SseUtil.createSseEmitter(null, emitter -> {
      try {
        Consumer<ChatCompletionResponse> eventHandler = p -> SseUtil.sendJson(emitter, (String) null, null, p);
        client.chatCompletionStreamBlocking(request, eventHandler, null);
        SseUtil.sendText(emitter, (String) null, ChatConsts.COMPLETIONS_DONE);
      }
      catch (BssException e) {
        logger.error("Failed to invoke llm stream: request={}, error={}", requestJson, e.getMessage());
        SseUtil.sendJson(emitter, (String) null, null, buildError(e.getMessage()));
      }
      catch (Exception e) {
        logger.error("Failed to invoke llm stream: request={}", requestJson, e);
        SseUtil.sendJson(emitter, (String) null, null, buildError(ExpUtil.getMsg(e)));
      }
      finally {
        SseUtil.completeQuietly(emitter);
      }
    });
  }

  /**
   * 处理异常
   */
  private ResponseEntity<Map<String, Object>> handleException(String request, Exception e) {
    HttpStatus status;
    String errorMsg;
    if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
      status = HttpStatus.BAD_REQUEST;
      errorMsg = e.getMessage();
      logger.warn("Failed to invoke llm: request={}", request, e);
    }
    else {
      logger.error("Failed to invoke llm: request={}", request, e);
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      errorMsg = ExpUtil.getMsg(e);
    }
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(buildError(errorMsg));
  }

  /**
   * 构造错误响应
   */
  private Map<String, Object> buildError(@Nullable String message) {
    return ImmutableMap.of("error", ImmutableMap.of("message", StringUtils.isNotEmpty(message) ? message : "Unknown error"));
  }

  /**
   * 获取大语言模型客户端
   */
  private LlmClient getLlmClient(String model) {
    Assert.hasLength(model, "模型不能为空");
    String[] pieces = StringUtils.split(model, '/');
    long tenantId;
    long modelId;
    if (pieces.length == 1 && StringUtils.isNumeric(pieces[0])) {
      tenantId = -1L;
      modelId = Long.parseLong(pieces[0]);
    }
    else {
      // 租户 ID 可能是 -1
      Assert.isTrue(pieces.length == 2 && NumberUtils.isCreatable(pieces[0]) && StringUtils.isNumeric(pieces[1]), "模型名称格式错误，应为 modelId 或 tenantId/modelId");
      modelId = Long.parseLong(pieces[1]);
      tenantId = Long.parseLong(pieces[0]);
    }
    return modelClientCache.getLlmClient(tenantId, modelId);
  }

}
