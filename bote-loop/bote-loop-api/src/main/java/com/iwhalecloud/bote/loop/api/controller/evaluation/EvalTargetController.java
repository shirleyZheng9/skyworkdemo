package com.iwhalecloud.bote.loop.api.controller.evaluation;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.EvalTargetApplicationService;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.BatchGetEvalTargetsBySourceRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.BatchGetEvalTargetsBySourceResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.BatchGetSourceEvalTargetsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.BatchGetSourceEvalTargetsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.CreateEvalTargetRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.CreateEvalTargetResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.ExecuteEvalTargetRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.ExecuteEvalTargetResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.ListSourceEvalTargetVersionsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.ListSourceEvalTargetVersionsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.ListSourceEvalTargetsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.ListSourceEvalTargetsResponse;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评测目标控制器
 * 对应Thrift: EvalTargetService
 */
@RestController
@RequestMapping(BaseConsts.API_PREFIX + "api/evaluation/v1/eval_targets")
@RequiredArgsConstructor
@Tag(name = "评测对象：基础管理")
public class EvalTargetController {
  private final EvalTargetApplicationService evalTargetApplicationService;

  /**
   * 创建评测目标
   * 对应Thrift: POST /api/evaluation/v1/eval_targets
   */
  @PostMapping("createEvalTarget")
  @Operation(summary = "创建评测对象")
  public ResultVO<CreateEvalTargetResponse> createEvalTarget(@RequestBody CreateEvalTargetRequest request) {
    return ResultVO.success(evalTargetApplicationService.createEvalTarget(request));
  }

  /**
   * 根据源目标批量获取评测目标
   * 对应Thrift: POST /api/evaluation/v1/eval_targets/batch_get_by_source
   */
  @PostMapping("/batch_get_by_source")
  @Operation(summary = "根据源目标批量获取评测目标")
  public ResultVO<BatchGetEvalTargetsBySourceResponse> batchGetEvalTargetsBySource(@RequestBody BatchGetEvalTargetsBySourceRequest request) {
    return ResultVO.success(evalTargetApplicationService.batchGetEvalTargetsBySource(request));
  }

  /**
   * 批量获取源评测目标
   * 对应Thrift: POST /api/evaluation/v1/eval_targets/batch_get_source
   */
  @PostMapping("/batch_get_source")
  @Operation(summary = "批量获取源评测目标")
  public ResultVO<BatchGetSourceEvalTargetsResponse> batchGetSourceEvalTargets(@RequestBody BatchGetSourceEvalTargetsRequest request) {
    return ResultVO.success(evalTargetApplicationService.batchGetSourceEvalTargets(request));
  }

  /**
   * 列表源评测目标
   * 对应Thrift: POST /api/evaluation/v1/eval_targets/list_source
   */
  @PostMapping("/list_source")
  @Operation(summary = "列表源评测目标")
  public ResultVO<ListSourceEvalTargetsResponse> listSourceEvalTargets(@RequestBody ListSourceEvalTargetsRequest request) {
    return ResultVO.success(evalTargetApplicationService.listSourceEvalTargets(request));
  }

  /**
   * 列表源评测目标版本
   * 对应Thrift: POST /api/evaluation/v1/eval_targets/list_source_version
   */
  @PostMapping("/list_source_version")
  @Operation(summary = "列表源评测目标版本")
  public ResultVO<ListSourceEvalTargetVersionsResponse> listSourceEvalTargetVersions(@RequestBody ListSourceEvalTargetVersionsRequest request) {
    return ResultVO.success(evalTargetApplicationService.listSourceEvalTargetVersions(request));
  }

  /**
   * 执行评测目标
   * 对应Thrift: POST /api/evaluation/v1/eval_targets/:eval_target_id/versions/:eval_target_version_id/execute
   */
  @PostMapping("/{eval_target_id}/versions/{eval_target_version_id}/execute")
  @Operation(summary = "执行评测目标")
  public ResultVO<ExecuteEvalTargetResponse> executeEvalTarget(@PathVariable("eval_target_id") Long evalTargetId,
                                                               @PathVariable("eval_target_version_id") Long evalTargetVersionId,
                                                               @RequestBody ExecuteEvalTargetRequest request) {
    request.setEvalTargetId(evalTargetId);
    request.setEvalTargetVersionId(evalTargetVersionId);
    return ResultVO.success(evalTargetApplicationService.executeEvalTarget(request));
  }
}
