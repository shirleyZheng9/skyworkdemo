package com.iwhalecloud.bote.controller.model;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.doc.module.knowledge.service.IDocumentContentManageService;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.query.RecordQueryParams;
import com.iwhalecloud.bote.dto.model.EvalPublishRecordDTO;
import com.iwhalecloud.bote.dto.model.FinetunePublishRecordDTO;
import com.iwhalecloud.bote.dto.model.ModelEvalDTO;
import com.iwhalecloud.bote.dto.model.ModelFinetuneDTO;
import com.iwhalecloud.bote.dto.model.query.ModelFinetuneQueryParams;
import com.iwhalecloud.bote.dto.model.response.PredictResponse.PredictInfo;
import com.iwhalecloud.bote.dto.model.response.UpdatePublishStatusResponse;
import com.iwhalecloud.bote.intent.IIntentQuestionManageService;
import com.iwhalecloud.bote.service.model.IModelFinetuneManageService;
import com.iwhalecloud.bote.service.publish.IPublishService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模型微调管理 controller
 *
 * @author auto
 * @since 2025-02-19
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/finetune", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "基础：模型微调管理")
public class ModelFinetuneManageController {

  private final IModelFinetuneManageService service;
  private final IPublishService publishService;
  private final IIntentQuestionManageService intentQuestionManageService;
  private final IDocumentContentManageService documentContentManageService;
  private final IRefreshCacheService refreshCacheService;

  @Operation(summary = "查询单个模型微调")
  @GetMapping("getFinetune")
  public ResultVO<ModelFinetuneDTO> getFinetune(@RequestParam(name = "id") Long id) {
    Assert.notNull(id, "主键 ID 不能为空");
    return ResultVO.success(service.getFinetune(id));
  }

  @Operation(summary = "保存模型微调")
  @PostMapping("saveFinetune")
  public ResultVO<ModelFinetuneDTO> saveFinetune(@RequestBody ModelFinetuneDTO finetune) {
    return service.saveFinetune(finetune);
  }

  @Operation(summary = "删除模型微调")
  @GetMapping("deleteFinetune")
  public ResultVO<Void> deleteFinetune(@RequestParam(name = "id") Long id) {
    Assert.notNull(id, "主键 ID 不能为空");
    return service.deleteFinetune(id);
  }

  @Operation(summary = "上下架微调")
  @GetMapping("publishFinetune")
  public ResultVO<Void> publishFinetune(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam(name = "id") Long id, @RequestParam(name = "status") String status) {
    Assert.notNull(id, "主键 ID 不能为空");
    Assert.hasText(status, "状态编码不能为空");
    ResultVO<Void> result = service.publishFinetune(tenantId, id, status);
    if (result.isSuccess()) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_TENANT_SETTING, tenantId.toString());
    }
    return result;
  }

  @Operation(summary = "分页查询微调模型")
  @PostMapping("queryFinetunePage")
  public ResultVO<PageInfo<ModelFinetuneDTO>> queryFinetunePage(@RequestBody ModelFinetuneQueryParams queryParams) {
    return ResultVO.success(service.queryFinetunePage(queryParams));
  }

  @Operation(summary = "查询微调模型")
  @PostMapping("queryFinetuneList")
  public ResultVO<List<ModelFinetuneDTO>> queryFinetuneList(@RequestBody ModelFinetuneQueryParams queryParams) {
    return ResultVO.success(service.queryFinetuneList(queryParams));
  }

  @Operation(summary = "更新微调评测训练状态, 用于外系统调用")
  @PostMapping("updatePublishStatus")
  @IgnoreSign
  @IgnoreSession
  public ResultVO<Void> updatePublishStatus(@RequestBody UpdatePublishStatusResponse response) {
    Assert.notNull(response, "微调评测参数不能为空");
    if (response.isSuccess()) {
      Assert.notNull(response, "微调评测参数不能为空");
      Assert.notNull(response.getResultObject().getPublishId(), "发布记录 ID 不能为空");
    }
    return service.updatePublishStatus(response);
  }

  @Operation(summary = "通知部署所有启用的微调模型, 用于外系统调用")
  @GetMapping("loadAllFinetune")
  @IgnoreSign
  @IgnoreSession
  public ResultVO<Void> loadAllFinetune() {
    return service.loadAllFinetun();
  }

  @Operation(summary = "查询微调任务（分页）")
  @PostMapping("queryFinetuneRecordPage")
  public ResultVO<PageInfo<FinetunePublishRecordDTO>> queryFinetuneRecordPage(@RequestBody RecordQueryParams params) {
    return ResultVO.success(service.queryFinetuneRecordPage(params));
  }

  @Operation(summary = "查看发布流程进度")
  @GetMapping("queryStepLog")
  public ResultVO<PublishRecordDTO> queryStepLog(@RequestParam("publishId") Long publishId) {
    Assert.notNull(publishId, "日志 ID 不能为空");
    return ResultVO.success(publishService.getRecord(publishId));
  }

  @Operation(summary = "中断发布流程")
  @GetMapping("cancel")
  public ResultVO<Void> cancel(@RequestParam("publishId") Long publishId, @RequestParam("type") String type) {
    Assert.notNull(publishId, "日志 ID 不能为空");
    Assert.hasText(type, "类型不能为空");
    return service.cancel(publishId, type);
  }

  @Operation(summary = "重试发布流程")
  @GetMapping("retry")
  public ResultVO<Void> retry(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam("publishId") Long publishId,
    @RequestParam("type") String type) {
    Assert.notNull(publishId, "日志 ID 不能为空");
    Assert.hasText(type, "类型不能为空");
    return service.retry(tenantId, publishId, type);
  }

  @Operation(summary = "保存模型评测")
  @PostMapping("saveEval")
  public ResultVO<ModelEvalDTO> saveEval(@RequestBody ModelEvalDTO eval) {
    Assert.notNull(eval.getModelId(), "评测模型不能为空");
    Assert.notNull(eval.getRequestFileId(), "评测数据不能为空");
    return service.saveEval(eval);
  }

  @Operation(summary = "删除模型评测")
  @GetMapping("deleteEval")
  public ResultVO<Void> deleteEval(@RequestParam(name = "id") Long id) {
    Assert.notNull(id, "主键 ID 不能为空");
    return service.deleteEval(id);
  }

  @Operation(summary = "查询评测任务（分页）")
  @PostMapping("queryEvalRecordPage")
  public ResultVO<PageInfo<EvalPublishRecordDTO>> queryEvalRecordPage(@RequestBody RecordQueryParams params) {
    return ResultVO.success(service.queryEvalRecordPage(params));
  }

  @Operation(summary = "导出评测模板")
  @GetMapping("exportTemplate")
  public ResponseEntity<?> exportTemplate(@RequestParam(name = "tenantId") Long tenantId, @RequestParam(name = "finetuneId") Long finetuneId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(finetuneId, "评测模型不能为空");
    ModelFinetuneDTO finetune = service.getFinetune(finetuneId);
    String fileName;
    File file;
    if (BaseConsts.FINETUNE_USE_TYPE_INTENT.equals(finetune.getUseType())) {
      fileName = "IntentQuestion-" + System.currentTimeMillis() + ".xlsx";
      file = intentQuestionManageService.createIntentQuestionFile(tenantId);
    }
    else {
      fileName = "CorpusQuestion-" + System.currentTimeMillis() + ".xlsx";
      file = documentContentManageService.createCorpusQuestionFile(finetune.getCorpusInfo(), finetune.getTenantId()).getLeft();
    }
    FileSystemResource resource = new FileSystemResource(file);
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString());
    // @formatter:off
    return ResponseEntity.ok()
      .headers(headers)
      .contentLength(file.length())
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .body(resource);
    // @formatter:on
  }

  @Operation(summary = "微调模型推理测试")
  @PostMapping("predict")
  public ResultVO<PredictInfo> predict(@RequestBody ModelFinetuneDTO finetune) {
    Assert.notNull(finetune.getId(), "微调模型 ID 不能为空");
    return service.predict(finetune);
  }
}
