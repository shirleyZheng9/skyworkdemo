package com.iwhalecloud.bote.controller.scene;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.SceneContextUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.test.ConvertParamsRequest;
import com.iwhalecloud.bote.dto.orchestration.test.DebuggableNodeTypesDTO;
import com.iwhalecloud.bote.dto.orchestration.test.ExtractNodeParamsRequest;
import com.iwhalecloud.bote.dto.orchestration.test.ExtractNodeParamsResultDTO;
import com.iwhalecloud.bote.dto.orchestration.test.FillParamsRequest;
import com.iwhalecloud.bote.dto.orchestration.test.TestNodeRequest;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.SceneStepRegistry;
import com.iwhalecloud.bote.service.orchestration.helper.NodeDebugHelper;
import com.iwhalecloud.bote.service.orchestration.helper.NodeParamConvertHelper;
import com.iwhalecloud.bote.service.orchestration.reply.handlers.SceneTestReplyHandler;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import com.iwhalecloud.bote.service.scene.ISceneChatService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 场景测试
 *
 * @author bianjp
 * @since 2024-09-12
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "sceneTest/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "机器人：场景测试")
@SuppressWarnings("PMD.GuardLogStatement")
public class SceneTestController {
  private static final Logger logger = LoggerFactory.getLogger(SceneTestController.class);

  private final ISceneChatService sceneChatService;

  @PostMapping(path = "test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Operation(summary = "测试场景/对话型工作流")
  public SseEmitter test(@RequestBody SceneChatParamsDTO sceneChatParams) {
    sceneChatParams.setConversationId(SceneConsts.TEST_CONVERSATION_ID);
    return SseUtil.createSseEmitter(sceneChatParams.getClientId(), sseEmitter -> {
      OrchestrationEngineResponse response;
      try {
        Assert.isTrue(sceneChatParams.getSceneId() != null || sceneChatParams.getFlowId() != null, "sceneId 或 flowId 不能为空");
        Assert.isTrue(sceneChatParams.getSceneId() == null || sceneChatParams.getFlowId() == null, "sceneId 或 flowId 只能有一个不为空");
        Assert.hasLength(sceneChatParams.getContextId(), "contextId 不能为空");
        sceneChatParams.setReplyHandler(new SceneTestReplyHandler(sseEmitter, sceneChatParams.getClientId()));
        sceneChatParams.setHistoryMessagesLoader(() -> ListUtils.emptyIfNull(sceneChatParams.getHistoryMessages()));
        response = sceneChatService.run(sceneChatParams);
        // 发送响应对象
        SseUtil.sendJson(sseEmitter, ChatMessageType.RESPONSE, response);
      }
      catch (Exception e) {
        logger.error("Failed to run scene test", e);
        SseUtil.sendJson(sseEmitter, ChatMessageType.ERROR, ExpUtil.getMsg(e));
      }
      finally {
        SseUtil.completeQuietly(sseEmitter);
      }
    });
  }

  @Operation(summary = "获取支持单节点调试的节点类型信息")
  @GetMapping("getDebuggableNodeTypes")
  public ResultVO<DebuggableNodeTypesDTO> getDebuggableNodeTypes() {
    DebuggableNodeTypesDTO dto = new DebuggableNodeTypesDTO();
    dto.setNodeTypes(NodeDebugHelper.SUPPORTED_STEP_TYPES);
    dto.setStreamNodeTypes(NodeDebugHelper.STREAM_STEP_TYPES);
    return ResultVO.success(dto);
  }

  @Operation(summary = "提取节点参数", description = "用于单节点调试")
  @PostMapping("extractNodeParams")
  public ResultVO<ExtractNodeParamsResultDTO> extractNodeParams(@RequestBody ExtractNodeParamsRequest request) {
    Assert.notNull(request.getTenantId(), "租户 ID 不能为空");
    Assert.hasLength(request.getNodeCode(), "节点编码不能为空");
    Assert.notNull(request.getGraph(), "流程图不能为空");
    List<ParameterSpec> params = NodeDebugHelper.extractInputParams(request);
    ExtractNodeParamsResultDTO result = new ExtractNodeParamsResultDTO();
    result.setSpecs(params);
    if (SceneConsts.DEBUG_PARAMS_JSON.equals(request.getFormat())) {
      result.setParams(NodeParamConvertHelper.convertParamsToJson(params, true));
    }
    else {
      // 根据参数类型生成默认值，方便用户使用
      NodeParamConvertHelper.generateDefaultValue(params);
    }
    return ResultVO.success(result);
  }

  @Operation(summary = "参数转为表格格式")
  @PostMapping("convertParamsToTable")
  public ResultVO<List<ParameterSpec>> convertParamsToTable(@RequestBody ConvertParamsRequest request) {
    Assert.notEmpty(request.getSpecs(), "参数规格不能为空");
    return ResultVO.success(NodeParamConvertHelper.convertParamsToTable(request.getParams(), request.getSpecs()));
  }

  @Operation(summary = "参数转为 JSON 格式")
  @PostMapping("convertParamsToJson")
  public ResultVO<Map<String, Object>> convertParamsToJson(@RequestBody List<ParameterSpec> specs) {
    Assert.notEmpty(specs, "参数规格不能为空");
    return ResultVO.success(NodeParamConvertHelper.convertParamsToJson(specs, false));
  }

  @Operation(summary = "AI 填充参数(JSON)")
  @PostMapping("aiFillParamsJson")
  public ResultVO<Map<String, Object>> aiFillParamsJson(@RequestBody FillParamsRequest request) {
    Assert.notNull(request.getTenantId(), "租户 ID 不能为空");
    Assert.notEmpty(request.getSpecs(), "参数规格不能为空");
    Map<String, Object> params = Boolean.TRUE.equals(request.getIgnoreParams()) ? null : request.getParams();
    return ResultVO.success(NodeParamConvertHelper.aiFillParams(request.getTenantId(), params, request.getSpecs()));
  }

  @Operation(summary = "AI 填充参数(表格)")
  @PostMapping("aiFillParamsTable")
  public ResultVO<List<ParameterSpec>> aiFillParamsTable(@RequestBody FillParamsRequest request) {
    Assert.notNull(request.getTenantId(), "租户 ID 不能为空");
    Assert.notEmpty(request.getSpecs(), "参数规格不能为空");
    Map<String, Object> params = Boolean.TRUE.equals(request.getIgnoreParams()) ? null : NodeParamConvertHelper.convertParamsToJson(request.getSpecs(), false);
    Map<String, Object> filledParams = NodeParamConvertHelper.aiFillParams(request.getTenantId(), params, request.getSpecs());
    List<ParameterSpec> filledSpecs = NodeParamConvertHelper.convertParamsToTable(filledParams, request.getSpecs());
    return ResultVO.success(filledSpecs);
  }

  @Operation(summary = "测试节点")
  @PostMapping(path = "testNode", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
  public Object testNode(@RequestBody TestNodeRequest request) {
    // 非流式
    if (!Boolean.TRUE.equals(request.getStream())) {
      try {
        OrchestrationStepRunLog log = doTestNode(request, null);
        return ResultVO.success(log);
      }
      catch (Exception e) {
        return ResultVO.fail(e);
      }
    }

    // 流式
    return SseUtil.createSseEmitter(request.getClientId(), emitter -> {
      try {
        OrchestrationStepRunLog log = doTestNode(request, emitter);
        SseUtil.sendJson(emitter, ChatMessageType.STEP_LOG, log);
      }
      catch (Exception e) {
        SseUtil.sendJson(emitter, ChatMessageType.ERROR, ExpUtil.getMsg(e));
      }
      SseUtil.completeQuietly(emitter);
    });
  }

  /**
   * 执行单节点测试
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  private OrchestrationStepRunLog doTestNode(TestNodeRequest request, @Nullable SseEmitter emitter) {
    Assert.notNull(request.getTenantId(), "租户 ID 不能为空");
    Assert.hasLength(request.getNodeCode(), "节点编码不能为空");
    Assert.notNull(request.getGraph(), "流程图不能为空");
    SceneGraphNodeDTO node = IterableUtils.find(request.getGraph().getNodes(), n -> request.getNodeCode().equals(n.getNodeCode()));
    Assert.notNull(node, () -> "节点不存在: " + request.getNodeCode());
    Assert.isTrue(NodeDebugHelper.SUPPORTED_STEP_TYPES.contains(node.getNodeType()), () -> "节点【" + node.getNodeName() + "】不支持单节点调试");

    // 构造上下文
    SceneOrchestrationContext context = NodeDebugHelper.mockContext(request);
    if (emitter != null) {
      context.setReplyHandler(new SceneTestReplyHandler(emitter, request.getClientId()));
    }

    // 执行
    AbstractStep step = context.findStep(request.getNodeCode());
    AbstractStepRunner runner = SceneStepRegistry.getRunner(step.getType());
    context.startStepLog(step);
    OrchestrationStepRunLog log = context.getLastStepRunLogOptional().orElseThrow(() -> new IllegalStateException("节点日志不能为空"));
    try {
      SceneContextUtil.setContext(context);
      runner.debugStep(context, step, log);
      log.succeed();
    }
    catch (Exception e) {
      logger.error("Failed to debug node: nodeCode={}", request.getNodeCode(), e);
      log.fail(e);
    }
    finally {
      SceneContextUtil.removeContext();
    }
    return log;
  }

}
