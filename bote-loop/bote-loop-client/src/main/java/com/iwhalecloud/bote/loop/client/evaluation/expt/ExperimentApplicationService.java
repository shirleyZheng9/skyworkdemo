package com.iwhalecloud.bote.loop.client.evaluation.expt;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExperimentDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptStatsInfoDTO;
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
import com.iwhalecloud.bote.loop.client.evaluation.expt.dto.ListExperimentsRequest;
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

import java.io.OutputStream;

/**
 * 实验服务接口
 * 对应Thrift: ExperimentService
 */
public interface ExperimentApplicationService {

  /**
   * 检查实验名称
   * 对应Thrift方法: CheckExperimentName
   */
  CheckExperimentNameResponse checkExperimentName(CheckExperimentNameRequest request);

  /**
   * 创建实验（只创建，不提交运行）
   * 对应Thrift方法: CreateExperiment
   */
  CreateExperimentResponse createExperiment(CreateExperimentRequest request);

  /**
   * 提交实验（创建并提交运行）
   * 对应Thrift方法: SubmitExperiment
   */
  SubmitExperimentResponse submitExperiment(SubmitExperimentRequest request);

  /**
   * 批量获取实验
   * 对应Thrift方法: BatchGetExperiments
   */
  BatchGetExperimentsResponse batchGetExperiments(BatchGetExperimentsRequest request);

  /**
   * 列表实验
   * 对应Thrift方法: ListExperiments
   */
  PageInfo<ExperimentDTO> listExperiments(ListExperimentsRequest request);

  /**
   * 更新实验
   * 对应Thrift方法: UpdateExperiment
   */
  UpdateExperimentResponse updateExperiment(UpdateExperimentRequest request);

  /**
   * 删除实验
   * 对应Thrift方法: DeleteExperiment
   */
  DeleteExperimentResponse deleteExperiment(DeleteExperimentRequest request);

  /**
   * 批量删除实验
   * 对应Thrift方法: BatchDeleteExperiments
   */
  BatchDeleteExperimentsResponse batchDeleteExperiments(BatchDeleteExperimentsRequest request);

  /**
   * 克隆实验
   * 对应Thrift方法: CloneExperiment
   */
  CloneExperimentResponse cloneExperiment(CloneExperimentRequest request);

  /**
   * 运行已创建的实验
   * 对应Thrift方法: RunExperiment
   */
  RunExperimentResponse runExperiment(RunExperimentRequest request);

  /**
   * 重试实验
   * 对应Thrift方法: RetryExperiment
   */
  RetryExperimentResponse retryExperiment(RetryExperimentRequest request);

  /**
   * 终止实验
   * 对应Thrift方法: KillExperiment
   */
  KillExperimentResponse killExperiment(KillExperimentRequest request);

  /**
   * 批量获取实验结果
   * 对应Thrift方法: BatchGetExperimentResult_
   */
  BatchGetExperimentResultResponse batchGetExperimentResult(BatchGetExperimentResultRequest request);

  /**
   * 批量获取实验聚合结果
   * 对应Thrift方法: BatchGetExperimentAggrResult_
   */
  BatchGetExperimentAggrResultResponse batchGetExperimentAggrResult(BatchGetExperimentAggrResultRequest request);

  /**
   * 调用实验（在线实验）
   * 对应Thrift方法: InvokeExperiment
   */
  InvokeExperimentResponse invokeExperiment(InvokeExperimentRequest request);

  /**
   * 完成实验
   * 对应Thrift方法: FinishExperiment
   */
  FinishExperimentResponse finishExperiment(FinishExperimentRequest request);

  /**
   * 列表实验统计
   * 对应Thrift方法: ListExperimentStats
   */
  PageInfo<ExptStatsInfoDTO> listExperimentStats(ListExperimentStatsRequest request);

  /**
   * 更新实验轮次结果过滤
   * 对应Thrift方法: UpsertExptTurnResultFilter
   */
  UpsertExptTurnResultFilterResponse upsertExptTurnResultFilter(UpsertExptTurnResultFilterRequest request);

  /**
   * 生成实验结果PDF
   * */
  String createPdf(Long exptId, Long tenantId, OutputStream outputStream);
}
