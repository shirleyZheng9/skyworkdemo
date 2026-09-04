package com.iwhalecloud.bote.controller.skill;

import com.iwhalecloud.bote.cache.ChatflowContextCache;
import com.iwhalecloud.bote.cache.FlowDslCache;
import com.iwhalecloud.bote.cache.LlmTokenCountCache;
import com.iwhalecloud.bote.cache.WorkflowExecutionStausCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.SceneContextUtil;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.dto.chat.ReplyDTO;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.orchestration.SceneDslDTO;
import com.iwhalecloud.bote.dto.skill.ApiServiceParams;
import com.iwhalecloud.bote.dto.skill.FlowExecutionStateDTO;
import com.iwhalecloud.bote.dto.skill.FlowExecutionStateDTO.FlowExecutionStatus;
import com.iwhalecloud.bote.dto.skill.TokenUsageDTO;
import com.iwhalecloud.bote.llm.client.util.LlmTraceUtil;
import com.iwhalecloud.bote.observability.LangfuseTracingService;
import com.iwhalecloud.bote.service.engine.ApiSkillEngine;
import com.iwhalecloud.bote.service.engine.SqlSkillEngine;
import com.iwhalecloud.bote.service.orchestration.IOrchestrationEngine;
import com.iwhalecloud.bote.service.orchestration.reply.handlers.NonStreamFlowReplyHandler;
import com.iwhalecloud.bote.service.orchestration.reply.handlers.StreamFlowReplyHandler;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 技能运行
 *
 * <p>用于提供给页面组件、外系统调用</p>
 *
 * @author bianjp
 * @since 2024-11-12
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "技能运行")
public class SkillRunController {
  private final IOrchestrationEngine orchestrationEngine;
  private final FlowDslCache flowDslCache;
  private final ChatflowContextCache chatflowContextCache;
  private final ApiSkillEngine apiSkillEngine;
  private final SqlSkillEngine sqlSkillEngine;
  private final WorkflowExecutionStausCache workflowExecutionStausCache;
  private final LlmTokenCountCache tokenCountCache;
  private final LangfuseTracingService langfuseTracingService;

  @RequestMapping(path = "flow/run/{flowId}", method = {RequestMethod.GET, RequestMethod.POST})
  @Operation(summary = "执行工作流", description = "入参可选")
  public ResultVO<Object> runFlow(@PathVariable("flowId") Long flowId,
                                  @RequestBody(required = false) Map<String, Object> params) {
    Long tenantId = TenantIdUtil.getTenantId();
    SceneDslDTO dsl = flowDslCache.getDsl(tenantId, flowId, true);
    Assert.isTrue(Boolean.FALSE.equals(dsl.getChatflow()), "工作流类型必须是任务型");
    return executeTaskFlow(tenantId, flowId, params);
  }

  /**
   * 执行任务型工作流
   */
  private ResultVO<Object> executeTaskFlow(Long tenantId, Long flowId, Map<String, Object> params) {
    OrchestrationEngineRequest request = new OrchestrationEngineRequest();
    request.setTenantId(tenantId);
    request.setFlowId(flowId);
    request.setParameters(params);
    OrchestrationEngineResponse response = orchestrationEngine.run(request);
    if (Boolean.TRUE.equals(response.getSuccess())) {
      return ResultVO.success(response.getOutput());
    }
    return ResultVO.fail(response.getFailMsg());
  }

  @PostMapping(path = "flow/execute", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
  @Operation(summary = "执行工作流", description = "只有对话型工作流支持流式，任务型只能使用非流式")
  public Object executeFlow(@RequestBody FlowExecutionRequest request) {
    try {
      String traceId = langfuseTracingService.beginAgentTurn(request.getTraceId(), "flow", null, null);
      request.setTraceId(traceId);
      return doExecuteFlow(request);
    }
    catch (Exception e) {
      // 流式输出时需要以流式格式返回报错
      if (Boolean.TRUE.equals(request.getStream())) {
        SseEmitter emitter = new SseEmitter();
        SseUtil.sendJson(emitter, ChatMessageType.ERROR, ExpUtil.getMsg(e));
        closeEmitter(emitter);
        return emitter;
      }
      // 捕获异常，以 200 状态码返回。否则 ExceptionHandlers 会以 500 状态码返回，不便于调用方获取错误信息
      return ResultVO.fail(e);
    }
    finally {
      langfuseTracingService.endAgentTurn(null);
      LlmTraceUtil.clearTraceId();
    }
  }

  @PostMapping(path = "flow/execute/async", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "异步执行工作流")
  @SuppressWarnings("PMD.AvoidCatchingThrowable")
  public ResultVO<String> executeFlowAsync(@RequestBody FlowExecutionRequest request) {
    Assert.notNull(request.getFlowId(), "工作流 ID 不能为空");
    Long tenantId = getTenantId(request);
    Assert.notNull(tenantId, "租户 ID 不能为空");
    // 异步执行时只能使用非流式输出
    request.setStream(false);
    String requestId = UUID.randomUUID().toString();
    FlowExecutionStateDTO state = new FlowExecutionStateDTO();
    state.setTenantId(tenantId);
    state.setFlowId(request.getFlowId());
    state.setStatus(FlowExecutionStatus.RUNNING);
    workflowExecutionStausCache.put(requestId, state);
    ThreadPools.getOrchestration().submit(() -> {
      try {
        String traceId = langfuseTracingService.beginAgentTurn(request.getTraceId(), "flow-async", null, null);
        request.setTraceId(traceId);
        // 只检查是否成功，不关心出参/回复
        ResultVO<?> result = (ResultVO<?>) doExecuteFlow(request);
        if (result.isSuccess()) {
          state.setStatus(FlowExecutionStatus.SUCCESS);
        }
        else {
          state.setStatus(FlowExecutionStatus.FAILED);
          state.setError(result.getResultMsg());
        }
        workflowExecutionStausCache.put(requestId, state);
      }
      catch (Throwable e) {
        state.setStatus(FlowExecutionStatus.FAILED);
        state.setError(ExpUtil.getMsg(e));
        workflowExecutionStausCache.put(requestId, state);
      }
      finally {
        langfuseTracingService.endAgentTurn(null);
        LlmTraceUtil.clearTraceId();
      }
    });
    return ResultVO.success(requestId);
  }

  @GetMapping("flow/executionStatus")
  @Operation(summary = "查询工作流执行状态", description = "查不到时返回 null")
  public ResultVO<FlowExecutionStateDTO> queryFlowExecutionStatus(@RequestParam("requestId") String requestId) {
    Assert.hasLength(requestId, "requestId 不能为空");
    return ResultVO.success(workflowExecutionStausCache.get(requestId));
  }

  /**
   * 执行工作流
   */
  private Object doExecuteFlow(@RequestBody FlowExecutionRequest request) {
    Assert.notNull(request.getFlowId(), "工作流 ID 不能为空");
    Long tenantId = getTenantId(request);
    Assert.notNull(tenantId, "租户 ID 不能为空");

    SceneDslDTO dsl = flowDslCache.getDsl(tenantId, request.getFlowId(), true);
    // 任务型
    if (Boolean.FALSE.equals(dsl.getChatflow())) {
      return executeTaskFlow(tenantId, request.getFlowId(), request.getParams());
    }

    // 对话型
    String contextId = StringUtils.isNotEmpty(request.getContextId()) ? request.getContextId() : SceneContextUtil.newContextId();
    OrchestrationEngineRequest engineRequest = new OrchestrationEngineRequest();
    engineRequest.setTenantId(tenantId);
    engineRequest.setFlowId(request.getFlowId());
    engineRequest.setContextId(contextId);
    engineRequest.setMessageContent(request.getMessage());
    engineRequest.setParameters(request.getParams());

    // 非流式输出
    if (!Boolean.TRUE.equals(request.getStream())) {
      NonStreamFlowReplyHandler replyHandler = new NonStreamFlowReplyHandler(request.getClientId());
      engineRequest.setReplyHandler(replyHandler);
      OrchestrationEngineResponse response = orchestrationEngine.run(engineRequest);
      // 执行失败
      if (!Boolean.TRUE.equals(response.getSuccess())) {
        return ResultVO.fail(response.getFailMsg());
      }
      ChatflowExecutionResponse finalResponse = new ChatflowExecutionResponse();
      finalResponse.setContextId(contextId);
      finalResponse.setReplies(replyHandler.getReplies());
      return ResultVO.success(finalResponse);
    }

    // 流式输出
    return SseUtil.createSseEmitter(request.getClientId(), true, false, emitter -> {
      try {
        engineRequest.setReplyHandler(new StreamFlowReplyHandler(emitter, request.getClientId()));
        // 发送上下文 ID
        SseUtil.sendText(emitter, ChatMessageType.CONTEXT_ID, contextId);
        // 调用工作流
        OrchestrationEngineResponse response = orchestrationEngine.run(engineRequest);
        // 执行失败
        if (!Boolean.TRUE.equals(response.getSuccess())) {
          SseUtil.sendJson(emitter, ChatMessageType.ERROR, response.getFailMsg());
        }
        closeEmitter(emitter);
      }
      catch (Exception e) {
        SseUtil.sendJson(emitter, ChatMessageType.ERROR, e.getMessage());
        closeEmitter(emitter);
      }
    });
  }

  /**
   * 获取租户 ID
   */
  @Nullable
  private Long getTenantId(FlowExecutionRequest request) {
    Long tenantId;
    if (request.getTenantId() != null) {
      tenantId = request.getTenantId();
      TenantIdUtil.setTenantId(tenantId);
    }
    else {
      tenantId = TenantIdUtil.getTenantIdOptional();
      request.setTenantId(tenantId);
    }
    return tenantId;
  }

  /**
   * 关闭 SSE 触发器
   */
  private static void closeEmitter(SseEmitter emitter) {
    // 参考 OpenAI 协议发送一个结束标记
    SseUtil.sendText(emitter, ChatMessageType.DONE, ChatConsts.COMPLETIONS_DONE);
    SseUtil.completeQuietly(emitter);
  }

  @Operation(summary = "清空工作流全局变量")
  @GetMapping("flow/clearGlobalVariable")
  public ResultVO<Void> clearGlobalVariable(@RequestParam("flowId") Long flowId, @RequestParam("contextId") String contextId) {
    Assert.notNull(flowId, "工作流 ID 不能为空");
    Assert.hasText(contextId, "上下文 ID 不能为空");
    chatflowContextCache.put(false, null, flowId, contextId, new HashMap<>());
    return ResultVO.success();
  }

  @Operation(summary = "根据链路追踪标识获取大模型 token 使用量")
  @GetMapping("model/getTokenUsageByTraceId")
  public ResultVO<TokenUsageDTO> getTokenUsageByTraceId(@RequestParam("traceId") String traceId) {
    Assert.hasLength(traceId, "链路追踪标识不能为空");
    return ResultVO.success(tokenCountCache.get(traceId));
  }

  @PostMapping("api/execute")
  @Operation(summary = "执行 API 技能")
  public ResultVO<Object> executeApi(@RequestBody ApiExecutionRequest request) {
    Assert.notNull(request.getServiceId(), "服务 ID 不能为空");
    // 优先取请求体中的租户 ID
    Long tenantId;
    if (request.getTenantId() != null) {
      tenantId = request.getTenantId();
      TenantIdUtil.setTenantId(tenantId);
    }
    else {
      tenantId = TenantIdUtil.getTenantId();
    }
    ApiServiceParams params = request.getParams() != null ? request.getParams() : new ApiServiceParams();
    Object result = apiSkillEngine.execute(tenantId, request.getServiceId(), params);
    return ResultVO.success(result);
  }

  @PostMapping(value = "api/execute", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "执行 API 技能")
  public ResultVO<Object> executeApi(@RequestParam("SERVICE.serviceId") Long serviceId, @RequestParam Map<String, Object> parameters,
    HttpServletRequest httpServletRequest) {
    ApiExecutionRequest request = fillMultipartParams(parameters, httpServletRequest);
    request.setServiceId(serviceId);
    return executeApi(request);
  }

  /**
   * 提取参数
   *
   * @param parameters 参数
   * @param httpServletRequest 请求
   * @return API 技能执行请求参数
   */
  private ApiExecutionRequest fillMultipartParams(Map<String, Object> parameters, HttpServletRequest httpServletRequest) {
    ApiExecutionRequest request = new ApiExecutionRequest();
    parameters.keySet().removeIf(k -> k.startsWith("SERVICE."));
    Map<String, Object> header = filterAndParseRequestParams(parameters, "PARAMS.HEADER.");
    Map<String, Object> query = filterAndParseRequestParams(parameters, "PARAMS.QUERY.");
    Map<String, Object> path = filterAndParseRequestParams(parameters, "PARAMS.PATH.");
    Map<String, Object> body = filterAndParseRequestParams(parameters, "PARAMS.BODY.");
    if (httpServletRequest instanceof MultipartHttpServletRequest) {
      body.putAll(((MultipartHttpServletRequest) httpServletRequest).getFileMap());
    }
    ApiServiceParams params = new ApiServiceParams();
    params.setPath(path);
    params.setHeader(header);
    params.setQuery(query);
    params.setBody(body);
    request.setParams(params);
    return request;
  }

  private Map<String, Object> filterAndParseRequestParams(Map<String, Object> parameters, String keyPrefix) {
    return MapUtils.emptyIfNull(parameters).entrySet().stream().filter(item -> item.getKey().startsWith(keyPrefix))
      .collect(Collectors.toMap(each -> Strings.CS.removeStart(each.getKey(), keyPrefix), Map.Entry::getValue));
  }

  @PostMapping("sql/execute")
  @Operation(summary = "执行 SQL 技能")
  public ResultVO<Object> executeSql(@RequestBody SqlExecutionRequest request) {
    Assert.notNull(request.getServiceId(), "服务 ID 不能为空");
    // 优先取请求体中的租户 ID
    Long tenantId;
    if (request.getTenantId() != null) {
      tenantId = request.getTenantId();
      TenantIdUtil.setTenantId(tenantId);
    }
    else {
      tenantId = TenantIdUtil.getTenantId();
    }
    //noinspection JvmTaintAnalysis
    Object result = sqlSkillEngine.execute(tenantId, request.getServiceId(), request.getParams());
    return ResultVO.success(result);
  }

  /**
   * 工作流执行请求
   */
  @Getter
  @Setter
  @ToString
  @Schema(description = "工作流执行请求")
  public static class FlowExecutionRequest {
    @Schema(description = "租户 ID(可选，也可通过请求头或 URL 参数传递)")
    private Long tenantId;
    @Schema(description = "工作流 ID")
    private Long flowId;
    @Schema(description = "工作流入参")
    private Map<String, Object> params;
    @Schema(description = "上下文 ID(仅用于对话型工作流，多轮会话使用)")
    private String contextId;
    @Schema(description = "用户消息(仅用于对话型工作流)")
    private String message;
    @Schema(description = "是否流式输出(仅用于对话型工作流)，默认否")
    private Boolean stream;
    @Schema(description = "客户端 ID(流式输出时中断请求使用)")
    private String clientId;
    @Schema(description = "链路追踪标识，用于统计大模型 token 用量。客户端生成，应全局唯一")
    private String traceId;
  }

  /**
   * 对话流执行响应
   */
  @Getter
  @Setter
  @ToString
  @Schema(description = "对话流执行响应")
  public static class ChatflowExecutionResponse {
    @Schema(description = "上下文 ID")
    private String contextId;
    @Schema(description = "回复列表")
    private List<ReplyDTO> replies;
  }

  /**
   * API 技能执行请求
   */
  @Getter
  @Setter
  @ToString
  @Schema(description = "API 技能执行请求")
  public static class ApiExecutionRequest {
    @Schema(description = "租户 ID")
    private Long tenantId;
    @Schema(description = "API 技能 ID")
    private Long serviceId;
    @Schema(description = "参数")
    private ApiServiceParams params;
  }

  /**
   * SQL 技能执行请求
   */
  @Getter
  @Setter
  @ToString
  @Schema(description = "SQL 技能执行请求")
  public static class SqlExecutionRequest {
    @Schema(description = "租户 ID")
    private Long tenantId;
    @Schema(description = "SQL 技能 ID")
    private Long serviceId;
    @Schema(description = "参数")
    private Map<String, Object> params;
  }

}
