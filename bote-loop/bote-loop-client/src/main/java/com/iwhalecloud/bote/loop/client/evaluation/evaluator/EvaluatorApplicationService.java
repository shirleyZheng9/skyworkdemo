package com.iwhalecloud.bote.loop.client.evaluation.evaluator;

import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.BatchGetEvaluatorRecordsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.BatchGetEvaluatorRecordsResponse;
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
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.GetEvaluatorRecordRequest;
import com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto.GetEvaluatorRecordResponse;
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

/**
 * 评测器服务接口
 * 对应Thrift: EvaluatorService
 */
public interface EvaluatorApplicationService {

  /**
   * 列表评测器
   * 对应Thrift方法: ListEvaluators
   */
  ListEvaluatorsResponse listEvaluators(ListEvaluatorsRequest request);

  /**
   * 批量获取评测器
   * 对应Thrift方法: BatchGetEvaluators
   */
  BatchGetEvaluatorsResponse batchGetEvaluators(BatchGetEvaluatorsRequest request);

  /**
   * 获取评测器
   * 对应Thrift方法: GetEvaluator
   */
  GetEvaluatorResponse getEvaluator(GetEvaluatorRequest request);

  /**
   * 创建评测器
   * 对应Thrift方法: CreateEvaluator
   */
  CreateEvaluatorResponse createEvaluator(CreateEvaluatorRequest request);

  /**
   * 更新评测器
   * 对应Thrift方法: UpdateEvaluator
   */
  UpdateEvaluatorResponse updateEvaluator(UpdateEvaluatorRequest request);

  /**
   * 更新评测器草稿
   * 对应Thrift方法: UpdateEvaluatorDraft
   */
  UpdateEvaluatorDraftResponse updateEvaluatorDraft(UpdateEvaluatorDraftRequest request);

  /**
   * 删除评测器
   * 对应Thrift方法: DeleteEvaluator
   */
  DeleteEvaluatorResponse deleteEvaluator(DeleteEvaluatorRequest request);

  /**
   * 检查评测器名称
   * 对应Thrift方法: CheckEvaluatorName
   */
  CheckEvaluatorNameResponse checkEvaluatorName(CheckEvaluatorNameRequest request);

  /**
   * 列表评测器版本
   * 对应Thrift方法: ListEvaluatorVersions
   */
  ListEvaluatorVersionsResponse listEvaluatorVersions(ListEvaluatorVersionsRequest request);

  /**
   * 获取评测器版本
   * 对应Thrift方法: GetEvaluatorVersion
   */
  GetEvaluatorVersionResponse getEvaluatorVersion(GetEvaluatorVersionRequest request);

  /**
   * 批量获取评测器版本
   * 对应Thrift方法: BatchGetEvaluatorVersions
   */
  BatchGetEvaluatorVersionsResponse batchGetEvaluatorVersions(BatchGetEvaluatorVersionsRequest request);

  /**
   * 提交评测器版本
   * 对应Thrift方法: SubmitEvaluatorVersion
   */
  SubmitEvaluatorVersionResponse submitEvaluatorVersion(SubmitEvaluatorVersionRequest request);

  /**
   * 列表模板
   * 对应Thrift方法: ListTemplates
   */
  ListTemplatesResponse listTemplates(ListTemplatesRequest request);

  /**
   * 获取模板信息
   * 对应Thrift方法: GetTemplateInfo
   */
  GetTemplateInfoResponse getTemplateInfo(GetTemplateInfoRequest request);

  /**
   * 获取默认提示词评测器工具
   * 对应Thrift方法: GetDefaultPromptEvaluatorTools
   */
  GetDefaultPromptEvaluatorToolsResponse getDefaultPromptEvaluatorTools(GetDefaultPromptEvaluatorToolsRequest request);

  /**
   * 运行评测器
   * 对应Thrift方法: RunEvaluator
   */
  RunEvaluatorResponse runEvaluator(RunEvaluatorRequest request);

  /**
   * 调试评测器
   * 对应Thrift方法: DebugEvaluator
   */
  DebugEvaluatorResponse debugEvaluator(DebugEvaluatorRequest request);

  /**
   * 更新评测器记录
   * 对应Thrift方法: UpdateEvaluatorRecord
   */
  UpdateEvaluatorRecordResponse updateEvaluatorRecord(UpdateEvaluatorRecordRequest request);

  /**
   * 获取评测器记录
   * 对应Thrift方法: GetEvaluatorRecord
   */
  GetEvaluatorRecordResponse getEvaluatorRecord(GetEvaluatorRecordRequest request);

  /**
   * 批量获取评测器记录
   * 对应Thrift方法: BatchGetEvaluatorRecords
   */
  BatchGetEvaluatorRecordsResponse batchGetEvaluatorRecords(BatchGetEvaluatorRecordsRequest request);
}
