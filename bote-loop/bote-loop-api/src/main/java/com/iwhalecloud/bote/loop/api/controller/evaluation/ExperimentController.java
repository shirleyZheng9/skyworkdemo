package com.iwhalecloud.bote.loop.api.controller.evaluation;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExperimentDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptStatsInfoDTO;
import com.iwhalecloud.bote.loop.client.evaluation.expt.ExperimentApplicationService;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.BatchDeleteExperimentsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.BatchDeleteExperimentsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.BatchGetExperimentAggrResultRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.BatchGetExperimentAggrResultResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.BatchGetExperimentResultRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.BatchGetExperimentResultResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.BatchGetExperimentsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.BatchGetExperimentsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.CheckExperimentNameRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.CheckExperimentNameResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.CloneExperimentRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.CloneExperimentResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.CreateExperimentRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.CreateExperimentResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.DeleteExperimentRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.DeleteExperimentResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.FinishExperimentRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.FinishExperimentResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.InvokeExperimentRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.InvokeExperimentResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.KillExperimentRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.KillExperimentResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.ListExperimentStatsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.ListExperimentStatsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.ListExperimentsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.ListExperimentsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.RetryExperimentRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.RetryExperimentResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.RunExperimentRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.RunExperimentResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.SubmitExperimentRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.SubmitExperimentResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.UpdateExperimentRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.UpdateExperimentResponse;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.UpsertExptTurnResultFilterRequest;
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.UpsertExptTurnResultFilterResponse;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 实验服务控制器
 * 对应Thrift: ExperimentService
 */
@RestController
@RequestMapping(BaseConsts.API_PREFIX + "api/evaluation/v1/experiments")
@RequiredArgsConstructor
@Tag(name = "实验：基础管理")
public class ExperimentController {
  private static final Logger logger = LoggerFactory.getLogger(ExperimentController.class);
  private final ExperimentApplicationService experimentApplicationService;

  /**
   * 检查实验名称
   * 对应Thrift方法: CheckExperimentName
   */
  @PostMapping("/check_name")
  @Operation(summary = "检查实验名称")
  public ResultVO<CheckExperimentNameResponse> checkExperimentName(@RequestBody CheckExperimentNameRequest request) {
    return ResultVO.success(experimentApplicationService.checkExperimentName(request));
  }

  /**
   * 创建实验（只创建，不提交运行）
   * 对应Thrift方法: CreateExperiment
   */
  @PostMapping
  @Operation(summary = "创建实验")
  public ResultVO<CreateExperimentResponse> createExperiment(@RequestBody CreateExperimentRequest request) {
    Assert.notNull(request.getName(), "实验名称不能为空");
    return ResultVO.success(experimentApplicationService.createExperiment(request));
  }

  /**
   * 提交实验（创建并提交运行）
   * 对应Thrift方法: SubmitExperiment
   */
  @PostMapping("/submit")
  @Operation(summary = "提交实验")
  public ResultVO<SubmitExperimentResponse> submitExperiment(@RequestBody SubmitExperimentRequest request) {
    SubmitExperimentResponse response = experimentApplicationService.submitExperiment(request);
    return ResultVO.success(response);
  }

  /**
   * 批量获取实验
   * 对应Thrift方法: BatchGetExperiments
   */
  @PostMapping("/batch")
  @Operation(summary = "批量获取实验")
  public ResultVO<BatchGetExperimentsResponse> batchGetExperiments(@RequestBody BatchGetExperimentsRequest request) {
    return ResultVO.success(experimentApplicationService.batchGetExperiments(request));
  }

  /**
   * 列表实验
   * 对应Thrift方法: ListExperiments
   */
  @PostMapping("/list")
  @Operation(summary = "列表实验")
  public ResultVO<ListExperimentsResponse> listExperiments(@RequestBody ListExperimentsRequest request) {
    PageInfo<ExperimentDTO> pageInfo = experimentApplicationService.listExperiments(request);
    ListExperimentsResponse response = new ListExperimentsResponse();
    response.setExperiments(pageInfo.getList());
    response.setTotal(pageInfo.getTotal());
    return ResultVO.success(response);
  }

  /**
   * 更新实验
   * 对应Thrift方法: UpdateExperiment
   */
  @PutMapping("/{experimentId}")
  @Operation(summary = "更新实验")
  public ResultVO<UpdateExperimentResponse> updateExperiment(@PathVariable Long experimentId, @RequestBody UpdateExperimentRequest request) {
    request.setExptId(experimentId);
    return ResultVO.success(experimentApplicationService.updateExperiment(request));
  }

  /**
   * 删除实验
   * 对应Thrift方法: DeleteExperiment
   */
  @DeleteMapping("/{experimentId}")
  @Operation(summary = "删除实验")
  public ResultVO<DeleteExperimentResponse> deleteExperiment(@PathVariable Long experimentId) {
    DeleteExperimentRequest request = DeleteExperimentRequest.builder().exptId(experimentId).build();
    return ResultVO.success(experimentApplicationService.deleteExperiment(request));
  }

  /**
   * 批量删除实验
   * 对应Thrift方法: BatchDeleteExperiments
   */
  @PostMapping("/batch_delete")
  @Operation(summary = "批量删除实验")
  public ResultVO<BatchDeleteExperimentsResponse> batchDeleteExperiments(@RequestBody BatchDeleteExperimentsRequest request) {
    return ResultVO.success(experimentApplicationService.batchDeleteExperiments(request));
  }

  /**
   * 克隆实验
   * 对应Thrift方法: CloneExperiment
   */
  @PostMapping("/{experimentId}/clone")
  @Operation(summary = "克隆实验")
  public ResultVO<CloneExperimentResponse> cloneExperiment(@PathVariable Long experimentId, @RequestBody CloneExperimentRequest request) {
    request.setExptId(experimentId);
    return ResultVO.success(experimentApplicationService.cloneExperiment(request));
  }

  /**
   * 运行已创建的实验
   * 对应Thrift方法: RunExperiment
   */
  @PostMapping("/{experimentId}/run")
  @Operation(summary = "运行实验")
  public ResultVO<RunExperimentResponse> runExperiment(@PathVariable Long experimentId, @RequestBody RunExperimentRequest request) {
    request.setExptId(experimentId);
    return ResultVO.success(experimentApplicationService.runExperiment(request));
  }

  /**
   * 重试实验
   * 对应Thrift方法: RetryExperiment
   */
  @PostMapping("/retry")
  @Operation(summary = "重试实验")
  public ResultVO<RetryExperimentResponse> retryExperiment(@RequestBody RetryExperimentRequest request) {
    Assert.isTrue(request.getExptIds() == null || request.getExptId() == null, () -> "重跑数据不能为空");
    return ResultVO.success(experimentApplicationService.retryExperiment(request));
  }

  /**
   * 终止实验
   * 对应Thrift方法: KillExperiment
   */
  @PostMapping("/{experimentId}/kill")
  @Operation(summary = "终止实验")
  public ResultVO<KillExperimentResponse> killExperiment(@PathVariable Long experimentId, @RequestBody KillExperimentRequest request) {
    request.setExptId(experimentId);
    return ResultVO.success(experimentApplicationService.killExperiment(request));
  }

  /**
   * 批量获取实验结果
   * 对应Thrift方法: BatchGetExperimentResult_
   */
  @PostMapping("/results/batch")
  @Operation(summary = "批量获取实验结果")
  public ResultVO<BatchGetExperimentResultResponse> batchGetExperimentResult(@RequestBody BatchGetExperimentResultRequest request) {
    return ResultVO.success(experimentApplicationService.batchGetExperimentResult(request));
  }

  /**
   * 批量获取实验聚合结果
   * 对应Thrift方法: BatchGetExperimentAggrResult_
   */
  @PostMapping("/results/aggregate/batch")
  @Operation(summary = "批量获取实验聚合结果")
  public ResultVO<BatchGetExperimentAggrResultResponse> batchGetExperimentAggrResult(@RequestBody BatchGetExperimentAggrResultRequest request) {
    return ResultVO.success(experimentApplicationService.batchGetExperimentAggrResult(request));
  }

  /**
   * 调用实验（在线实验）
   * 对应Thrift方法: InvokeExperiment
   */
  @PostMapping("/{experimentId}/invoke")
  @Operation(summary = "调用实验")
  public ResultVO<InvokeExperimentResponse> invokeExperiment(@PathVariable Long experimentId, @RequestBody InvokeExperimentRequest request) {
    request.setExperimentId(experimentId);
    return ResultVO.success(experimentApplicationService.invokeExperiment(request));
  }

  /**
   * 完成实验
   * 对应Thrift方法: FinishExperiment
   */
  @PostMapping("/{experimentId}/finish")
  @Operation(summary = "完成实验")
  public ResultVO<FinishExperimentResponse> finishExperiment(@PathVariable Long experimentId, @RequestBody FinishExperimentRequest request) {
    request.setExperimentId(experimentId);
    return ResultVO.success(experimentApplicationService.finishExperiment(request));
  }

  /**
   * 列表实验统计
   * 对应Thrift方法: ListExperimentStats
   */
  @PostMapping("/stats/list")
  @Operation(summary = "列表实验统计")
  public ResultVO<ListExperimentStatsResponse> listExperimentStats(@RequestBody ListExperimentStatsRequest request) {
    PageInfo<ExptStatsInfoDTO> pageInfo = experimentApplicationService.listExperimentStats(request);
    ListExperimentStatsResponse response = new ListExperimentStatsResponse();
    response.setExptStatsInfos(pageInfo.getList());
    response.setTotal(pageInfo.getTotal());
    return ResultVO.success(response);
  }

  /**
   * 更新实验轮次结果过滤
   * 对应Thrift方法: UpsertExptTurnResultFilter
   */
  @PostMapping("/{experimentId}/turn_result_filter")
  @Operation(summary = "更新实验轮次结果过滤")
  public ResultVO<UpsertExptTurnResultFilterResponse> upsertExptTurnResultFilter(@PathVariable Long experimentId,
                                                                                 @RequestBody UpsertExptTurnResultFilterRequest request) {
    request.setExperimentId(experimentId);
    return ResultVO.success(experimentApplicationService.upsertExptTurnResultFilter(request));
  }


  @GetMapping("/results/download/{experimentId}")
  @Operation(summary = "下载实验结果PDF")
  public ResponseEntity<?> downloadResultPdf(@PathVariable("experimentId") Long experimentId, @RequestParam(name = "tenantId") Long tenantId) {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();) {
      String name = experimentApplicationService.createPdf(experimentId, tenantId, outputStream);
      // 2. 设置下载响应头
      HttpHeaders headers = new HttpHeaders();
      // 指定MIME类型为PDF
      headers.setContentType(MediaType.APPLICATION_PDF);
      // 设置文件名（解决中文文件名乱码）
      String fileNameTemplate = "【%s】实验评测报告PDF.pdf";
      // 设置内容长度
      headers.setContentDisposition(ContentDisposition.attachment().filename(String.format(fileNameTemplate, name), StandardCharsets.UTF_8).build());

      // 3. 返回响应实体（字节数组+响应头+状态码）
      return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
    }
    catch (Exception e) {
      logger.error(e.getMessage(), e);
      return new ResponseEntity<>(ResultVO.fail("实验评测报告预览失败:" + e.getMessage()), HttpStatus.OK);
    }
  }
}
