package com.iwhalecloud.bote.controller.skill;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.base.SimpleFlowStepDTO;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.dto.skill.SimpleSkillFlowDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowNodeConfigResponseDTO;
import com.iwhalecloud.bote.dto.skill.StandardServiceDiffViewDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.service.orchestration.IOrchestrationEngine;
import com.iwhalecloud.bote.service.orchestration.runner.step.AgentStepRunner;
import com.iwhalecloud.bote.service.skill.ISkillFlowManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 技能：流程 controller
 *
 * @author auto
 * @since 2024-09-18
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/skill/flow", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "技能：流程管理")
public class SkillFlowManageController {

  private final ISkillFlowManageService flowManageService;
  private final IOrchestrationEngine orchestrationEngine;
  private final IRefreshCacheService refreshCacheService;

  @Operation(summary = "查询单个流程")
  @GetMapping("getSkillFlow")
  public ResultVO<SkillFlowDTO> getSkillFlow(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam(name = "flowId") Long flowId) {
    Assert.notNull(flowId, "流程 ID 不能为空");
    SkillFlowDTO flow = flowManageService.findSkillFlow(tenantId, flowId);
    flow.setFlowSteps(JsonUtil.parseJson(flow.getFlowStepJson(), new TypeReference<List<SimpleFlowStepDTO>>() {
    }));
    flow.parseGraph();
    flow.parseParams();
    return ResultVO.success(flow);
  }

  @Operation(summary = "保存流程基本信息")
  @PostMapping("saveSkillFlowBasicInfo")
  public ResultVO<SkillFlowDTO> saveSkillFlowBasicInfo(@RequestBody @Valid SkillFlowDTO flow) {
    ResultVO<SkillFlowDTO> result = flowManageService.saveSkillFlowBasicInfo(flow);
    // 修改工作流时刷新缓存
    if (flow.getFlowId() != null && result.isSuccess()) {
      String key = flow.getTenantId() + CacheConsts.COLON + flow.getFlowId();
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_FLOW_DSL, key);
    }
    return result;
  }

  @Operation(summary = "保存流程")
  @PostMapping("saveSkillFlow")
  public ResultVO<SkillFlowDTO> saveSkillFlow(@RequestBody SkillFlowDTO flow) {
    ResultVO<SkillFlowDTO> result = flowManageService.saveSkillFlow(flow);
    // 修改工作流时刷新缓存
    if (flow.getFlowId() != null && result.isSuccess()) {
      String key = flow.getTenantId() + CacheConsts.COLON + flow.getFlowId();
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_FLOW_DSL, key);
    }
    return result;
  }

  @Operation(summary = "删除流程")
  @GetMapping("deleteSkillFlow")
  public ResultVO<Void> deleteSkillFlow(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam(name = "flowId") Long flowId) {
    Assert.notNull(flowId, "主键 ID 不能为空");
    ResultVO<Void> result = flowManageService.deleteSkillFlow(tenantId, flowId);
    // 删除工作流时刷新缓存
    if (result.isSuccess()) {
      String key = tenantId + CacheConsts.COLON + flowId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_FLOW_DSL, key);
    }
    return result;
  }

  @Operation(summary = "查询流程列表", description = "用于其他模块引用")
  @PostMapping("querySkillFlowList")
  public ResultVO<List<SimpleSkillFlowDTO>> querySkillFlowList(@RequestBody SkillQueryParams queryParams) {
    return ResultVO.success(flowManageService.querySkillFlowList(queryParams));
  }

  @Operation(summary = "分页查询流程")
  @PostMapping("querySkillFlowPage")
  public ResultVO<PageInfo<SkillFlowDTO>> querySkillFlowPage(@RequestBody SkillQueryParams queryParams) {
    return ResultVO.success(flowManageService.querySkillFlowPage(queryParams));
  }

  @Operation(summary = "分页查询流程", description = "用于其他模块引用")
  @PostMapping("querySimpleSkillFlowPage")
  public ResultVO<PageInfo<SimpleSkillFlowDTO>> querySimpleSkillFlowPage(@RequestBody SkillQueryParams queryParams) {
    return ResultVO.success(flowManageService.querySimpleSkillFlowPage(queryParams));
  }

  @Operation(summary = "查询流程参数")
  @GetMapping("getFlowParam")
  public ResultVO<SkillFlowDTO> getFlowParam(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam(name = "flowId") Long flowId) {
    SkillFlowDTO flow = new SkillFlowDTO();
    flow.setParam(flowManageService.findSkillFlowParam(tenantId, flowId));
    flow.parseParams();
    return ResultVO.success(flow);
  }

  @Operation(summary = "获取 Agent 节点默认的工具使用规则")
  @GetMapping("getDefaultAgentToolUseRules")
  public ResultVO<String> getDefaultAgentToolUseRules() throws IOException {
    try (InputStream inputStream = AgentStepRunner.DEFAULT_TOOL_USE_RULES_PROMPT.getInputStream()) {
      String prompt = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
      return ResultVO.success(prompt);
    }
  }

  @PostMapping("test")
  @Operation(summary = "测试任务型流程")
  public ResultVO<OrchestrationEngineResponse> test(@RequestBody SceneChatParamsDTO sceneChatParams) {
    Assert.notNull(sceneChatParams.getFlowId(), "flowId 不能为空");
    OrchestrationEngineRequest request = new OrchestrationEngineRequest();
    request.setDebug(sceneChatParams.getDebug());
    request.setDebugInnerService(sceneChatParams.getDebugInnerService());
    request.setTenantId(sceneChatParams.getTenantId());
    request.setFlowId(sceneChatParams.getFlowId());
    request.setParameters(sceneChatParams.getParams());
    return ResultVO.success(orchestrationEngine.run(request));
  }

  @Operation(summary = "查询流历史版本详情")
  @GetMapping("findSkillFlowVersion")
  public ResultVO<StandardServiceDiffViewDTO> findSkillFlowVersion(@RequestParam("logId") Long logId, @RequestParam("tenantId") Long tenantId) {
    return flowManageService.findSkillFlowVersion(logId, tenantId);
  }

  @Operation(summary = "比较两次修改记录的差异")
  @GetMapping("diffSkillFlowOperLog")
  public ResultVO<StandardServiceDiffViewDTO> diffSkillFlowOperLog(@RequestParam("tenantId") Long tenantId,
                                                            @RequestParam("oldLogId") Long oldLogId,
                                                            @RequestParam("logId") Long logId) {
    return flowManageService.diffSkillFlowOperLog(tenantId, oldLogId, logId);
  }

  @Operation(summary = "根据流程配置，自动生成流程步骤")
  @PostMapping("generateFlowStep")
  public ResultVO<List<SimpleFlowStepDTO>> generateFlowStep(@RequestBody SkillFlowDTO flow) {
    return flowManageService.generateFlowStep(flow);
  }

  @Operation(summary = "获取流程节点配置")
  @GetMapping("getFlowNodeConfigs")
  public ResultVO<List<SkillFlowNodeConfigResponseDTO>> getFlowNodeConfigs() {
    return ResultVO.success(flowManageService.getFlowNodeConfigs());
  }
}
