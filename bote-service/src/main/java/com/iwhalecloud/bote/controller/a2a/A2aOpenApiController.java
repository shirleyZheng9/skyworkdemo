package com.iwhalecloud.bote.controller.a2a;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.A2aConsts;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.dto.a2a.PublishA2aAgentRequest;
import com.iwhalecloud.bote.dto.a2a.UnpublishA2aAgentRequest;
import com.iwhalecloud.bote.service.a2a.IA2aOpenApiService;
import com.iwhalecloud.bote.service.a2a.helper.A2aAgentExecutor.InvokeA2aContextParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.a2a.server.ServerCallContext;
import io.a2a.server.requesthandlers.RequestHandler;
import io.a2a.spec.AgentCard;
import io.a2a.spec.CancelTaskRequest;
import io.a2a.spec.DeleteTaskPushNotificationConfigRequest;
import io.a2a.spec.GetAuthenticatedExtendedCardRequest;
import io.a2a.spec.GetTaskPushNotificationConfigRequest;
import io.a2a.spec.GetTaskRequest;
import io.a2a.spec.IdJsonMappingException;
import io.a2a.spec.InternalError;
import io.a2a.spec.InvalidParamsError;
import io.a2a.spec.InvalidParamsJsonMappingException;
import io.a2a.spec.InvalidRequestError;
import io.a2a.spec.JSONParseError;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.JSONRPCErrorResponse;
import io.a2a.spec.JSONRPCRequest;
import io.a2a.spec.JSONRPCResponse;
import io.a2a.spec.ListTaskPushNotificationConfigRequest;
import io.a2a.spec.MethodNotFoundError;
import io.a2a.spec.MethodNotFoundJsonMappingException;
import io.a2a.spec.NonStreamingJSONRPCRequest;
import io.a2a.spec.SendMessageRequest;
import io.a2a.spec.SendStreamingMessageRequest;
import io.a2a.spec.SendStreamingMessageResponse;
import io.a2a.spec.SetTaskPushNotificationConfigRequest;
import io.a2a.spec.StreamingJSONRPCRequest;
import io.a2a.spec.TaskResubscriptionRequest;
import io.a2a.spec.TaskStatusUpdateEvent;
import io.a2a.spec.UnsupportedOperationError;
import io.a2a.transport.jsonrpc.handler.JSONRPCHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * A2A 开放接口
 *
 * @author bianjp
 * @since 2025-09-09
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "a2a", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "A2A 开放接口")
public class A2aOpenApiController {
  private static final Logger logger = LoggerFactory.getLogger(A2aOpenApiController.class);

  private final IA2aOpenApiService a2aOpenApiService;
  private final IRefreshCacheService refreshCacheService;
  private final RequestHandler requestHandler;

  @Operation(summary = "发布 A2A 智能体", description = "外系统将智能体发布到博特使用")
  @PostMapping("agent/publish")
  @IgnoreSession
  @IgnoreSign
  public ResultVO<Void> publishAgent(@Valid @RequestBody PublishA2aAgentRequest request,
                                     @RequestHeader(A2aConsts.A2A_AUTH_HEADER) String apiKey) {
    ResultVO<Void> result = a2aOpenApiService.publishAgent(request, apiKey);
    if (result.isSuccess()) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_SCENE_INTENT, request.getTenantId().toString());
    }
    return result;
  }

  @Operation(summary = "取消发布 A2A 智能体", description = "外系统取消发布已经发布到博特平台的智能体")
  @PostMapping("agent/unpublish")
  @IgnoreSession
  @IgnoreSign
  public ResultVO<Void> unpublishAgent(@Valid @RequestBody UnpublishA2aAgentRequest request,
                                       @RequestHeader(A2aConsts.A2A_AUTH_HEADER) String apiKey) {
    ResultVO<Void> result = a2aOpenApiService.unpublishAgent(request, apiKey);
    if (result.isSuccess()) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_SCENE_INTENT, request.getTenantId().toString());
    }
    return result;
  }

  @Operation(summary = "获取智能体卡片")
  @GetMapping(path = "agent/{tenantId}/{agentId}/.well-known/agent-card.json")
  @IgnoreSign
  public AgentCard getAgentCard(@PathVariable("tenantId") Long tenantId,
                                @PathVariable("agentId") Long agentId,
                                HttpServletResponse response) throws IOException {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(agentId, "智能体 ID 不能为空");
    AgentCard agentCard = a2aOpenApiService.getAgentCard(tenantId, agentId);
    if (agentCard == null) {
      response.sendError(HttpStatus.NOT_FOUND.value(), "智能体不存在");
      return null;
    }
    return agentCard;
  }

  @Operation(summary = "调用智能体")
  @PostMapping(path = "agent/{tenantId}/{agentId}",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
  @SuppressWarnings("PMD.AvoidCatchingThrowable")
  public Object invokeAgent(@PathVariable("tenantId") Long tenantId,
                            @PathVariable("agentId") Long agentId,
                            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                              required = true,
                              content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = JSONRPCRequest.class))
                              }) @RequestBody String body) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(agentId, "智能体 ID 不能为空");
    Assert.hasLength(body, "请求体不能为空");

    // 实现逻辑参考 https://github.com/a2aproject/a2a-java/blob/main/reference/jsonrpc/src/main/java/io/a2a/server/apps/quarkus/A2AServerRoutes.java

    // 是否流式
    boolean isStreaming = false;
    // SSE 触发器
    SseEmitter emitter = null;
    // 非流式请求的响应
    JSONRPCResponse<?> nonStreamingResponse = null;
    // 错误响应
    JSONRPCErrorResponse error = null;
    // 捕获所有异常，根据是否流式按不同的方式返回异常
    try {
      JsonNode json = JsonUtil.getObjectMapper().readTree(body);
      isStreaming = isStreamingRequest(json);
      // 无法直接反序列化为 JSONRPCRequest，必须检查是否流式后再分别反序列化为不同类型
      JSONRPCRequest<?> request = parseRequest(isStreaming, json);
      AgentCard agentCard = a2aOpenApiService.getAgentCard(tenantId, agentId);
      if (agentCard == null) {
        error = new JSONRPCErrorResponse(request.getId(), new JSONRPCError(404, "智能体不存在", null));
      }
      else {
        JSONRPCHandler handler = new JSONRPCHandler(agentCard, requestHandler, ThreadPools.getA2a());
        Map<String, Object> invokeA2aContext = Map.of(InvokeA2aContextParams.STATE_KEY, new InvokeA2aContextParams(tenantId, agentId, isStreaming));
        ServerCallContext context = new ServerCallContext(null, invokeA2aContext, Set.of());
        if (isStreaming) {
          String clientId = Objects.toString(request.getId(), null);
          emitter = SseUtil.createSseEmitter(clientId, sseEmitter ->
            processStreamingRequest((StreamingJSONRPCRequest<?>) request, handler, context, sseEmitter));
        }
        else {
          nonStreamingResponse = processNonStreamingRequest((NonStreamingJSONRPCRequest<?>) request, handler, context);
        }
      }
    }
    catch (JsonProcessingException e) {
      logger.warn("Illegal a2a request body: body={}", body, e);
      error = handleJsonProcessingException(e);
    }
    catch (Throwable e) {
      logger.error("Failed to process a2a request: body={}", body, e);
      error = new JSONRPCErrorResponse(new InternalError(ExpUtil.getMsg(e)));
    }

    // 流式请求
    if (isStreaming) {
      if (error != null) {
        emitter = sendSseError(emitter, error);
      }
      return emitter;
    }
    // 非流式请求
    return error != null ? error : nonStreamingResponse;
  }

  /**
   * 解析请求对象
   */
  @SuppressWarnings("PMD.PreserveStackTrace")
  private JSONRPCRequest<?> parseRequest(boolean isStreaming, JsonNode json) throws JsonProcessingException {
    try {
      if (isStreaming) {
        return JsonUtil.convert(json, StreamingJSONRPCRequest.class);
      }
      return JsonUtil.convert(json, NonStreamingJSONRPCRequest.class);
    }
    catch (IllegalArgumentException e) {
      // JsonUtil#convert 会把 JsonProcessingException 封装为 IllegalArgumentException, 还原为原始异常以便外层处理
      if (e.getCause() instanceof JsonProcessingException) {
        throw (JsonProcessingException) e.getCause();
      }
      throw e;
    }
  }

  /**
   * 发送流式的错误
   */
  @SuppressWarnings("PMD.AvoidCatchingThrowable")
  private SseEmitter sendSseError(SseEmitter emitter, JSONRPCErrorResponse error) {
    if (emitter == null) {
      emitter = new SseEmitter();
    }
    try {
      emitter.send(error, MediaType.APPLICATION_JSON);
    }
    catch (Throwable e) {
      logger.warn("Failed to send SSE error", e);
    }
    SseUtil.completeQuietly(emitter);
    return emitter;
  }

  /**
   * 检查是否是流式请求
   */
  private boolean isStreamingRequest(JsonNode requestBody) {
    String method = requestBody.path("method").asText();
    return SendStreamingMessageRequest.METHOD.equals(method) || TaskResubscriptionRequest.METHOD.equals(method);
  }

  /**
   * 处理 JSON 解析异常
   */
  private JSONRPCErrorResponse handleJsonProcessingException(JsonProcessingException exception) {
    Object id = null;
    JSONRPCError jsonRpcError;
    if (exception.getCause() instanceof JsonParseException) {
      jsonRpcError = new JSONParseError();
    }
    else if (exception instanceof JsonEOFException) {
      jsonRpcError = new JSONParseError(exception.getMessage());
    }
    else if (exception instanceof MethodNotFoundJsonMappingException err) {
      id = err.getId();
      jsonRpcError = new MethodNotFoundError();
    }
    else if (exception instanceof InvalidParamsJsonMappingException err) {
      id = err.getId();
      jsonRpcError = new InvalidParamsError();
    }
    else if (exception instanceof IdJsonMappingException err) {
      id = err.getId();
      jsonRpcError = new InvalidRequestError();
    }
    else {
      jsonRpcError = new InvalidRequestError();
    }
    return new JSONRPCErrorResponse(id, jsonRpcError);
  }


  /**
   * 处理非流式请求
   */
  private JSONRPCResponse<?> processNonStreamingRequest(NonStreamingJSONRPCRequest<?> request, JSONRPCHandler handler, ServerCallContext context) {
    return switch (request) {
      case GetTaskRequest req -> handler.onGetTask(req, context);
      case CancelTaskRequest req -> handler.onCancelTask(req, context);
      case SetTaskPushNotificationConfigRequest req -> handler.setPushNotificationConfig(req, context);
      case GetTaskPushNotificationConfigRequest req -> handler.getPushNotificationConfig(req, context);
      case SendMessageRequest req -> handler.onMessageSend(req, context);
      case ListTaskPushNotificationConfigRequest req -> handler.listPushNotificationConfig(req, context);
      case DeleteTaskPushNotificationConfigRequest req -> handler.deletePushNotificationConfig(req, context);
      case GetAuthenticatedExtendedCardRequest req -> handler.onGetAuthenticatedExtendedCardRequest(req, context);
      //noinspection UnnecessaryDefault
      default -> new JSONRPCErrorResponse(request.getId(), new UnsupportedOperationError());
    };
  }

  /**
   * 处理流式请求
   */
  private void processStreamingRequest(StreamingJSONRPCRequest<?> request, JSONRPCHandler handler, ServerCallContext context, SseEmitter emitter) {
    switch (request) {
      case SendStreamingMessageRequest req ->
        handler.onMessageSendStream(req, context).subscribe(new StreamingMessageResponseSubscriber(request, emitter));
      case TaskResubscriptionRequest req ->
        handler.onResubscribeToTask(req, context).subscribe(new StreamingMessageResponseSubscriber(request, emitter));
      default -> {
        try {
          emitter.send(new JSONRPCErrorResponse(request.getId(), new UnsupportedOperationError()), MediaType.APPLICATION_JSON);
        }
        catch (Exception e) {
          logger.error("Failed to send SSE error", e);
        }
        finally {
          SseUtil.completeQuietly(emitter);
        }
      }
    }
  }

  /**
   * 订阅流式响应
   */
  private record StreamingMessageResponseSubscriber(StreamingJSONRPCRequest<?> request,
                                                    SseEmitter emitter) implements Subscriber<SendStreamingMessageResponse> {
    @Override
    public void onSubscribe(Subscription subscription) {
      subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(SendStreamingMessageResponse item) {
      // 忽略为了让 a2a-sdk 把流式输出的回复记录到历史消息而发送的虚假更新事件
      if (item.getResult() instanceof TaskStatusUpdateEvent event) {
        if (MapUtils.getBooleanValue(event.getMetadata(), "replyAsHistory", false)) {
          return;
        }
      }
      try {
        emitter.send(item, MediaType.APPLICATION_JSON);
      }
      catch (IOException e) {
        logger.error("Failed to send SSE message", e);
      }
    }

    @Override
    public void onError(Throwable throwable) {
      try {
        JSONRPCErrorResponse response = new JSONRPCErrorResponse(request.getId(), new InternalError(ExpUtil.getMsg(throwable)));
        emitter.send(response, MediaType.APPLICATION_JSON);
      }
      catch (Exception e) {
        logger.error("Failed to send SSE message", e);
      }
      finally {
        SseUtil.completeQuietly(emitter);
      }
    }

    @Override
    public void onComplete() {
      SseUtil.completeQuietly(emitter);
    }
  }
}
