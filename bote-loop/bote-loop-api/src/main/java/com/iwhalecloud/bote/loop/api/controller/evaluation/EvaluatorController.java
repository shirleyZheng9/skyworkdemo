package com.iwhalecloud.bote.loop.api.controller.evaluation;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.EvaluatorApplicationService;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.BatchGetEvaluatorVersionsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.BatchGetEvaluatorVersionsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.BatchGetEvaluatorsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.BatchGetEvaluatorsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.CheckEvaluatorNameRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.CheckEvaluatorNameResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.CreateEvaluatorRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.CreateEvaluatorResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.DebugEvaluatorRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.DebugEvaluatorResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.DeleteEvaluatorRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.DeleteEvaluatorResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.GetDefaultPromptEvaluatorToolsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.GetDefaultPromptEvaluatorToolsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.GetEvaluatorRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.GetEvaluatorResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.GetEvaluatorVersionRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.GetEvaluatorVersionResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.GetTemplateInfoRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.GetTemplateInfoResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.ListEvaluatorVersionsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.ListEvaluatorVersionsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.ListEvaluatorsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.ListEvaluatorsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.ListTemplatesRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.ListTemplatesResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.RunEvaluatorRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.RunEvaluatorResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.SubmitEvaluatorVersionRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.SubmitEvaluatorVersionResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.UpdateEvaluatorDraftRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.UpdateEvaluatorDraftResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.UpdateEvaluatorRecordRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.UpdateEvaluatorRecordResponse;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.UpdateEvaluatorRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.UpdateEvaluatorResponse;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评测器控制器
 * 对应Thrift: EvaluatorService
 */
@RestController
@RequestMapping(BaseConsts.API_PREFIX + "api/evaluation/v1/evaluators")
@RequiredArgsConstructor
@Tag(name = "评测器：基础管理")
public class EvaluatorController {
  private final EvaluatorApplicationService evaluatorApplicationService;

  /**
   * 创建评测器
   * 对应Thrift: POST /api/evaluation/v1/evaluators
   */
  @PostMapping("createEvaluator")
  @Operation(summary = "创建评测器")
  public ResultVO<CreateEvaluatorResponse> createEvaluator(@RequestBody CreateEvaluatorRequest request) {
    return ResultVO.success(evaluatorApplicationService.createEvaluator(request));
  }

  /**
   * 检查评测器名称
   * 对应Thrift: POST /api/evaluation/v1/evaluators/check_name
   */
  @PostMapping("/check_name")
  @Operation(summary = "检查评测器名称")
  public ResultVO<CheckEvaluatorNameResponse> checkEvaluatorName(@RequestBody CheckEvaluatorNameRequest request) {
    return ResultVO.success(evaluatorApplicationService.checkEvaluatorName(request));
  }

  /**
   * 调试评测器
   * 对应Thrift: POST /api/evaluation/v1/evaluators/debug
   */
  @PostMapping("/debug")
  @Operation(summary = "调试评测器")
  public ResultVO<DebugEvaluatorResponse> debugEvaluator(@RequestBody DebugEvaluatorRequest request) {
    return ResultVO.success(evaluatorApplicationService.debugEvaluator(request));
  }

  /**
   * 获取默认提示词评测器工具
   * 对应Thrift: POST /api/evaluation/v1/evaluators/default_prompt_evaluator_tools
   */
  @PostMapping("/default_prompt_evaluator_tools")
  @Operation(summary = "获取默认提示词评测器工具")
  public ResultVO<GetDefaultPromptEvaluatorToolsResponse> getDefaultPromptEvaluatorTools(@RequestBody GetDefaultPromptEvaluatorToolsRequest request) {
    return ResultVO.success(evaluatorApplicationService.getDefaultPromptEvaluatorTools(request));
  }

  /**
   * 删除评测器
   * 对应Thrift: DELETE /api/evaluation/v1/evaluators/:evaluator_id
   */
  @DeleteMapping("/{evaluatorId}")
  @Operation(summary = "删除评测器")
  public ResultVO<DeleteEvaluatorResponse> deleteEvaluator(@PathVariable Long evaluatorId) {
    DeleteEvaluatorRequest request = DeleteEvaluatorRequest.builder().evaluatorId(evaluatorId).build();
    return ResultVO.success(evaluatorApplicationService.deleteEvaluator(request));
  }

  /**
   * 提交评测器版本
   * 对应Thrift: POST /api/evaluation/v1/evaluators/:evaluator_id/submit_version
   */
  @PostMapping("/{evaluatorId}/submit_version")
  @Operation(summary = "提交评测器版本")
  public ResultVO<SubmitEvaluatorVersionResponse> submitEvaluatorVersion(@PathVariable Long evaluatorId,
                                                                         @RequestBody SubmitEvaluatorVersionRequest request) {
    request.setEvaluatorId(evaluatorId);
    return ResultVO.success(evaluatorApplicationService.submitEvaluatorVersion(request));
  }

  /**
   * 列表评测器版本
   * 对应Thrift: POST /api/evaluation/v1/evaluators/:evaluator_id/versions/list
   */
  @PostMapping("/{evaluatorId}/versions/list")
  @Operation(summary = "列表评测器版本")
  public ResultVO<ListEvaluatorVersionsResponse> listEvaluatorVersions(@PathVariable Long evaluatorId,
                                                                       @RequestBody ListEvaluatorVersionsRequest request) {
    request.setEvaluatorId(evaluatorId);
    return ResultVO.success(evaluatorApplicationService.listEvaluatorVersions(request));
  }

  /**
   * 更新评测器
   * 对应Thrift: PATCH /api/evaluation/v1/evaluators/:evaluator_id
   */
  @PatchMapping("/{evaluatorId}")
  @Operation(summary = "更新评测器")
  public ResultVO<UpdateEvaluatorResponse> updateEvaluator(@PathVariable Long evaluatorId,
                                                           @RequestBody UpdateEvaluatorRequest request) {
    request.setEvaluatorId(evaluatorId);
    return ResultVO.success(evaluatorApplicationService.updateEvaluator(request));
  }

  /**
   * 更新评测器草稿
   * 对应Thrift: PATCH /api/evaluation/v1/evaluators/:evaluator_id/update_draft
   */
  @PatchMapping("/{evaluatorId}/update_draft")
  @Operation(summary = "更新评测器草稿")
  public ResultVO<UpdateEvaluatorDraftResponse> updateEvaluatorDraft(@PathVariable Long evaluatorId,
                                                                     @RequestBody UpdateEvaluatorDraftRequest request) {
    request.setEvaluatorId(evaluatorId);
    return ResultVO.success(evaluatorApplicationService.updateEvaluatorDraft(request));
  }

  /**
   * 获取模板信息
   * 对应Thrift: POST /api/evaluation/v1/evaluators/get_template_info
   */
  @PostMapping("/get_template_info")
  @Operation(summary = "获取模板信息")
  public ResultVO<GetTemplateInfoResponse> getTemplateInfo(@RequestBody GetTemplateInfoRequest request) {
    return ResultVO.success(evaluatorApplicationService.getTemplateInfo(request));
  }

  /**
   * 列表模板
   * 对应Thrift: POST /api/evaluation/v1/evaluators/list_template
   */
  @PostMapping("/list_template")
  @Operation(summary = "列表模板")
  public ResultVO<ListTemplatesResponse> listTemplates(@RequestBody ListTemplatesRequest request) {
    return ResultVO.success(evaluatorApplicationService.listTemplates(request));
  }

  /**
   * 批量获取评测器
   * 对应Thrift: POST /api/evaluation/v1/evaluators/batch_get
   */
  @PostMapping("/batch_get")
  @Operation(summary = "批量获取评测器")
  public ResultVO<BatchGetEvaluatorsResponse> batchGetEvaluators(@RequestBody BatchGetEvaluatorsRequest request) {
    return ResultVO.success(evaluatorApplicationService.batchGetEvaluators(request));
  }

  /**
   * 获取评测器
   * 对应Thrift: GET /api/evaluation/v1/evaluators/:evaluator_id
   */
  @GetMapping("/{evaluatorId}")
  @Operation(summary = "获取评测器")
  public ResultVO<GetEvaluatorResponse> getEvaluator(@PathVariable Long evaluatorId, @RequestParam("tenantId") Long tenantId) {
    GetEvaluatorRequest request = GetEvaluatorRequest.builder().evaluatorId(evaluatorId).workspaceId(tenantId).build();
    return ResultVO.success(evaluatorApplicationService.getEvaluator(request));
  }

  /**
   * 列表评测器
   * 对应Thrift: POST /api/evaluation/v1/evaluators/list
   */
  @PostMapping("/list")
  @Operation(summary = "列表评测器")
  public ResultVO<ListEvaluatorsResponse> listEvaluators(@RequestBody ListEvaluatorsRequest request) {
    return ResultVO.success(evaluatorApplicationService.listEvaluators(request));
  }

  /**
   * 批量获取评测器版本
   * 对应Thrift: POST /api/evaluation/v1/evaluators_versions/batch_get
   */
  @PostMapping("/versions/batch_get")
  @Operation(summary = "批量获取评测器版本")
  public ResultVO<BatchGetEvaluatorVersionsResponse> batchGetEvaluatorVersions(@RequestBody BatchGetEvaluatorVersionsRequest request) {
    return ResultVO.success(evaluatorApplicationService.batchGetEvaluatorVersions(request));
  }

  /**
   * 获取评测器版本
   * 对应Thrift: GET /api/evaluation/v1/evaluators_versions/:evaluator_version_id
   */
  @GetMapping("/versions/{evaluatorVersionId}")
  @Operation(summary = "获取评测器版本")
  public ResultVO<GetEvaluatorVersionResponse> getEvaluatorVersion(@PathVariable Long evaluatorVersionId) {
    GetEvaluatorVersionRequest request = GetEvaluatorVersionRequest.builder().evaluatorVersionId(evaluatorVersionId).build();
    return ResultVO.success(evaluatorApplicationService.getEvaluatorVersion(request));
  }

  /**
   * 运行评测器
   * 对应Thrift: POST /api/evaluation/v1/evaluators_versions/:evaluator_version_id/run
   */
  @PostMapping("/versions/{evaluatorVersionId}/run")
  @Operation(summary = "运行评测器")
  public ResultVO<RunEvaluatorResponse> runEvaluator(@PathVariable Long evaluatorVersionId,
                                                     @RequestBody RunEvaluatorRequest request) {
    request.setEvaluatorVersionId(evaluatorVersionId);
    return ResultVO.success(evaluatorApplicationService.runEvaluator(request));
  }

  /**
   * 更新评测器记录
   * 对应Thrift: PATCH /api/evaluation/v1/evaluator_records/:evaluator_record_id
   */
  @PatchMapping("/records/{evaluatorRecordId}")
  @Operation(summary = "更新评测器记录")
  public ResultVO<UpdateEvaluatorRecordResponse> updateEvaluatorRecord(@PathVariable Long evaluatorRecordId,
                                                                       @RequestBody UpdateEvaluatorRecordRequest request) {
    request.setEvaluatorRecordId(evaluatorRecordId);
    return ResultVO.success(evaluatorApplicationService.updateEvaluatorRecord(request));
  }
}
