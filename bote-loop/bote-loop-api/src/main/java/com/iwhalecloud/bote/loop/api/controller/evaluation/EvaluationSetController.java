package com.iwhalecloud.bote.loop.api.controller.evaluation;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetItemDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetVersionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.EvaluationSetApplicationService;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchCreateEvaluationSetItemsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchCreateEvaluationSetItemsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchDeleteEvaluationSetItemsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchDeleteEvaluationSetItemsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchGetEvaluationSetItemsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchGetEvaluationSetItemsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchGetEvaluationSetVersionsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.BatchGetEvaluationSetVersionsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ClearEvaluationSetDraftItemRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ClearEvaluationSetDraftItemResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.CreateEvaluationSetRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.CreateEvaluationSetResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.CreateEvaluationSetVersionRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.CreateEvaluationSetVersionResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.DeleteEvaluationSetRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.DeleteEvaluationSetResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.GetEvaluationSetRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.GetEvaluationSetResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.GetEvaluationSetVersionRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.GetEvaluationSetVersionResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ImportEvaluationSetItemsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ImportEvaluationSetItemsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ListEvaluationSetItemsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ListEvaluationSetItemsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ListEvaluationSetVersionsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ListEvaluationSetVersionsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ListEvaluationSetsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.ListEvaluationSetsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.UpdateEvaluationSetItemRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.UpdateEvaluationSetItemResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.UpdateEvaluationSetRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.UpdateEvaluationSetResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.UpdateEvaluationSetSchemaRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto.UpdateEvaluationSetSchemaResponse;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 评测集控制器 对应Thrift: EvaluationSetService
 */
@RestController
@RequestMapping(BaseConsts.API_PREFIX + "api/evaluation/v1/evaluation_sets")
@RequiredArgsConstructor
@Tag(name = "评测集：基础管理")
public class EvaluationSetController {
  private final EvaluationSetApplicationService evaluationSetService;

  /**
   * 创建评测集 对应Thrift: POST /api/evaluation/v1/evaluation_sets
   */
  @PostMapping
  @Operation(summary = "创建评测集")
  public ResultVO<CreateEvaluationSetResponse> createEvaluationSet(@RequestBody CreateEvaluationSetRequest request) {
    return ResultVO.success(evaluationSetService.createEvaluationSet(request));
  }

  /**
   * 删除评测集 对应Thrift: DELETE /api/evaluation/v1/evaluation_sets/:evaluation_set_id
   */
  @DeleteMapping("/{evaluation_set_id}")
  @Operation(summary = "删除评测集")
  public ResultVO<DeleteEvaluationSetResponse> deleteEvaluationSet(@PathVariable("evaluation_set_id") Long evaluationSetId,
                                                                   @RequestParam("tenantId") Long tenantId) {
    DeleteEvaluationSetRequest request = DeleteEvaluationSetRequest.builder().evaluationSetId(evaluationSetId)
      .workspaceId(tenantId).build();
    return ResultVO.success(evaluationSetService.deleteEvaluationSet(request));
  }

  /**
   * 更新评测集模式 对应Thrift: PUT /api/evaluation/v1/evaluation_sets/:evaluation_set_id/schema
   */
  @PutMapping("/{evaluation_set_id}/schema")
  @Operation(summary = "更新评测集模式")
  public ResultVO<UpdateEvaluationSetSchemaResponse> updateEvaluationSetSchema(@PathVariable("evaluation_set_id") Long evaluationSetId,
                                                                               @RequestBody UpdateEvaluationSetSchemaRequest request) {
    request.setEvaluationSetId(evaluationSetId);
    return ResultVO.success(evaluationSetService.updateEvaluationSetSchema(request));
  }

  /**
   * 创建评测集版本 对应Thrift: POST /api/evaluation/v1/evaluation_sets/:evaluation_set_id/versions
   */
  @PostMapping("/{evaluation_set_id}/versions")
  @Operation(summary = "创建评测集版本")
  public ResultVO<CreateEvaluationSetVersionResponse> createEvaluationSetVersion(@PathVariable("evaluation_set_id") Long evaluationSetId,
                                                                                 @RequestBody CreateEvaluationSetVersionRequest request) {
    request.setEvaluationSetId(evaluationSetId);
    return ResultVO.success(evaluationSetService.createEvaluationSetVersion(request));
  }

  /**
   * 列表评测集版本 对应Thrift: POST /api/evaluation/v1/evaluation_sets/:evaluation_set_id/versions/list
   */
  @PostMapping("/{evaluation_set_id}/versions/list")
  @Operation(summary = "列表评测集版本")
  public ResultVO<ListEvaluationSetVersionsResponse> listEvaluationSetVersions(@PathVariable("evaluation_set_id") Long evaluationSetId,
                                                                               @RequestBody ListEvaluationSetVersionsRequest request) {
    request.setEvaluationSetId(evaluationSetId);
    PageInfo<EvaluationSetVersionDTO> pageInfo = evaluationSetService.listEvaluationSetVersions(request);
    ListEvaluationSetVersionsResponse response = new ListEvaluationSetVersionsResponse();
    response.setVersions(pageInfo.getList());
    response.setTotal(pageInfo.getTotal());
    return ResultVO.success(response);
  }

  /**
   * 获取评测集版本 对应Thrift: GET /api/evaluation/v1/evaluation_sets/:evaluation_set_id/versions/:version_id
   */
  @GetMapping("/{evaluation_set_id}/versions/{version_id}")
  @Operation(summary = "获取评测集版本")
  public ResultVO<GetEvaluationSetVersionResponse> getEvaluationSetVersion(@PathVariable("evaluation_set_id") Long evaluationSetId,
                                                                           @PathVariable("version_id") Long versionId,
                                                                           @RequestParam(name = "tenantId", required = false) Long tenantId) {
    GetEvaluationSetVersionRequest request = GetEvaluationSetVersionRequest.builder()
      .evaluationSetId(evaluationSetId).versionId(versionId).workspaceId(tenantId).build();
    return ResultVO.success(evaluationSetService.getEvaluationSetVersion(request));
  }

  /**
   * 批量创建评测集项目 对应Thrift: POST /api/evaluation/v1/evaluation_sets/:evaluation_set_id/items/batch_create
   */
  @PostMapping("/{evaluation_set_id}/items/batch_create")
  @Operation(summary = "批量创建评测集项目")
  public ResultVO<BatchCreateEvaluationSetItemsResponse> batchCreateEvaluationSetItems(@PathVariable("evaluation_set_id") Long evaluationSetId,
                                                                                       @RequestBody BatchCreateEvaluationSetItemsRequest request) {
    request.setEvaluationSetId(evaluationSetId);
    return ResultVO.success(evaluationSetService.batchCreateEvaluationSetItems(request));
  }

  /**
   * 批量删除评测集项目 对应Thrift: POST /api/evaluation/v1/evaluation_sets/:evaluation_set_id/items/batch_delete
   */
  @PostMapping("/{evaluation_set_id}/items/batch_delete")
  @Operation(summary = "批量删除评测集项目")
  public ResultVO<BatchDeleteEvaluationSetItemsResponse> batchDeleteEvaluationSetItems(@PathVariable("evaluation_set_id") Long evaluationSetId,
                                                                                       @RequestBody BatchDeleteEvaluationSetItemsRequest request) {
    request.setEvaluationSetId(evaluationSetId);
    return ResultVO.success(evaluationSetService.batchDeleteEvaluationSetItems(request));
  }

  /**
   * 批量获取评测集项目 对应Thrift: POST /api/evaluation/v1/evaluation_sets/:evaluation_set_id/items/batch_get
   */
  @PostMapping("/{evaluation_set_id}/items/batch_get")
  @Operation(summary = "批量获取评测集项目")
  public ResultVO<BatchGetEvaluationSetItemsResponse> batchGetEvaluationSetItems(@PathVariable("evaluation_set_id") Long evaluationSetId,
                                                                                 @RequestBody BatchGetEvaluationSetItemsRequest request) {
    request.setEvaluationSetId(evaluationSetId);
    return ResultVO.success(evaluationSetService.batchGetEvaluationSetItems(request));
  }

  /**
   * 清除评测集草稿项目 对应Thrift: POST /api/evaluation/v1/evaluation_sets/:evaluation_set_id/items/clear
   */
  @PostMapping("/{evaluation_set_id}/items/clear")
  @Operation(summary = "清除评测集草稿项目")
  public ResultVO<ClearEvaluationSetDraftItemResponse> clearEvaluationSetDraftItem(@PathVariable("evaluation_set_id") Long evaluationSetId,
                                                                                   @RequestBody ClearEvaluationSetDraftItemRequest request) {
    request.setEvaluationSetId(evaluationSetId);
    return ResultVO.success(evaluationSetService.clearEvaluationSetDraftItem(request));
  }

  /**
   * 更新评测集项目 对应Thrift: PUT /api/evaluation/v1/evaluation_sets/:evaluation_set_id/items/:item_id
   */
  @PutMapping("/{evaluation_set_id}/items/{item_id}")
  @Operation(summary = "更新评测集项目")
  public ResultVO<UpdateEvaluationSetItemResponse> updateEvaluationSetItem(@PathVariable("evaluation_set_id") Long evaluationSetId,
                                                                           @PathVariable("item_id") Long itemId,
                                                                           @RequestBody UpdateEvaluationSetItemRequest request) {
    request.setEvaluationSetId(evaluationSetId);
    request.setItemId(itemId);
    return ResultVO.success(evaluationSetService.updateEvaluationSetItem(request));
  }

  /**
   * 列表评测集项目 对应Thrift: POST /api/evaluation/v1/evaluation_sets/:evaluation_set_id/items/list
   */
  @PostMapping("/{evaluation_set_id}/items/list")
  @Operation(summary = "列表评测集项目")
  public ResultVO<ListEvaluationSetItemsResponse> listEvaluationSetItems(@PathVariable("evaluation_set_id") Long evaluationSetId,
                                                                         @RequestBody ListEvaluationSetItemsRequest request) {
    request.setEvaluationSetId(evaluationSetId);
    PageInfo<EvaluationSetItemDTO> pageInfo = evaluationSetService.listEvaluationSetItems(request);
    ListEvaluationSetItemsResponse response = new ListEvaluationSetItemsResponse();
    response.setItems(pageInfo.getList());
    response.setTotal(pageInfo.getTotal());
    return ResultVO.success(response);
  }

  /**
   * 获取评测集 对应Thrift: GET /api/evaluation/v1/evaluation_sets/:evaluation_set_id
   */
  @GetMapping("/{evaluation_set_id}")
  @Operation(summary = "获取评测集")
  public ResultVO<GetEvaluationSetResponse> getEvaluationSet(@PathVariable("evaluation_set_id") Long evaluationSetId,
                                                             @RequestParam(name = "tenantId") Long tenantId) {
    GetEvaluationSetRequest request = GetEvaluationSetRequest.builder().evaluationSetId(evaluationSetId)
      .workspaceId(tenantId).build();
    return ResultVO.success(evaluationSetService.getEvaluationSet(request));
  }

  /**
   * 更新评测集 对应Thrift: PATCH /api/evaluation/v1/evaluation_sets/:evaluation_set_id
   */
  @PatchMapping("/{evaluation_set_id}")
  @Operation(summary = "更新评测集")
  public ResultVO<UpdateEvaluationSetResponse> updateEvaluationSet(@PathVariable("evaluation_set_id") Long evaluationSetId,
                                                                   @RequestParam(name = "tenantId") Long tenantId,
                                                                   @RequestBody UpdateEvaluationSetRequest request) {
    request.setEvaluationSetId(evaluationSetId);
    // 如果请求体中没有 workspaceId，则使用 tenantId
    if (request.getWorkspaceId() == null) {
      request.setWorkspaceId(tenantId);
    }
    return ResultVO.success(evaluationSetService.updateEvaluationSet(request));
  }

  /**
   * 列表评测集 对应Thrift: POST /api/evaluation/v1/evaluation_sets/list
   */
  @PostMapping("/list")
  @Operation(summary = "列表评测集")
  public ResultVO<ListEvaluationSetsResponse> listEvaluationSets(@RequestParam(name = "tenantId", required = false) Long tenantId,
                                                                 @RequestBody ListEvaluationSetsRequest request) {
    // 如果请求体中没有 workspaceId，则使用 tenantId
    if (request.getWorkspaceId() == null && tenantId != null) {
      request.setWorkspaceId(tenantId);
    }
    PageInfo<EvaluationSetDTO> pageInfo = evaluationSetService.listEvaluationSets(request);
    ListEvaluationSetsResponse response = new ListEvaluationSetsResponse();
    response.setEvaluationSets(pageInfo.getList());
    response.setTotal(pageInfo.getTotal());
    return ResultVO.success(response);
  }

  /**
   * 批量获取评测集版本 对应Thrift: POST /api/evaluation/v1/evaluation_set_versions/batch_get
   */
  @PostMapping("/evaluation_set_versions/batch_get")
  @Operation(summary = "批量获取评测集版本")
  public ResultVO<BatchGetEvaluationSetVersionsResponse> batchGetEvaluationSetVersions(@RequestParam(name = "tenantId", required = false) Long tenantId,
                                                                                       @RequestBody BatchGetEvaluationSetVersionsRequest request) {
    // If workspaceId is not set in the request body, use tenantId from query parameter
    if (request.getWorkspaceId() == null && tenantId != null) {
      request.setWorkspaceId(tenantId);
    }
    return ResultVO.success(evaluationSetService.batchGetEvaluationSetVersions(request));
  }

  @PostMapping("/{evaluation_set_id}/import")
  @Operation(summary = "批量导入评测集数据")
  public ResultVO<ImportEvaluationSetItemsResponse> batchImportEvaluationSetItems(@PathVariable("evaluation_set_id") Long evaluationSetId,
                                                                                  @RequestPart("file") MultipartFile file,
                                                                                  @RequestPart("request") ImportEvaluationSetItemsRequest request) {
    request.setEvaluationSetId(evaluationSetId);
    return ResultVO.success(evaluationSetService.batchImportEvaluationSetItems(file, request));
  }

}
