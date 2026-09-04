package com.iwhalecloud.bote.loop.client.evaluation.eval_target;

import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.BatchGetEvalTargetRecordsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.BatchGetEvalTargetRecordsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.BatchGetEvalTargetVersionsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.BatchGetEvalTargetVersionsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.BatchGetEvalTargetsBySourceRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.BatchGetEvalTargetsBySourceResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.BatchGetSourceEvalTargetsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.BatchGetSourceEvalTargetsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.CreateEvalTargetRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.CreateEvalTargetResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.ExecuteEvalTargetRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.ExecuteEvalTargetResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.GetEvalTargetRecordRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.GetEvalTargetRecordResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.GetEvalTargetVersionRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.GetEvalTargetVersionResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.ListSourceEvalTargetVersionsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.ListSourceEvalTargetVersionsResponse;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.ListSourceEvalTargetsRequest;
import com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto.ListSourceEvalTargetsResponse;

/**
 * 评测目标服务接口
 * 对应Thrift: EvalTargetService
 */
public interface EvalTargetApplicationService {

  /**
   * 创建评测对象
   * 对应Thrift方法: CreateEvalTarget
   */
  CreateEvalTargetResponse createEvalTarget(CreateEvalTargetRequest request);

  /**
   * 根据source target获取评测对象信息
   * 对应Thrift方法: BatchGetEvalTargetsBySource
   */
  BatchGetEvalTargetsBySourceResponse batchGetEvalTargetsBySource(BatchGetEvalTargetsBySourceRequest request);

  /**
   * 获取评测对象+版本
   * 对应Thrift方法: GetEvalTargetVersion
   */
  GetEvalTargetVersionResponse getEvalTargetVersion(GetEvalTargetVersionRequest request);

  /**
   * 批量获取+版本
   * 对应Thrift方法: BatchGetEvalTargetVersions
   */
  BatchGetEvalTargetVersionsResponse batchGetEvalTargetVersions(BatchGetEvalTargetVersionsRequest request);

  /**
   * Source评测对象列表
   * 对应Thrift方法: ListSourceEvalTargets
   */
  ListSourceEvalTargetsResponse listSourceEvalTargets(ListSourceEvalTargetsRequest request);

  /**
   * Source评测对象版本列表
   * 对应Thrift方法: ListSourceEvalTargetVersions
   */
  ListSourceEvalTargetVersionsResponse listSourceEvalTargetVersions(ListSourceEvalTargetVersionsRequest request);

  /**
   * 批量获取源评测目标
   * 对应Thrift方法: BatchGetSourceEvalTargets
   */
  BatchGetSourceEvalTargetsResponse batchGetSourceEvalTargets(BatchGetSourceEvalTargetsRequest request);

  /**
   * 执行
   * 对应Thrift方法: ExecuteEvalTarget
   */
  ExecuteEvalTargetResponse executeEvalTarget(ExecuteEvalTargetRequest request);

  /**
   * 获取评测目标记录
   * 对应Thrift方法: GetEvalTargetRecord
   */
  GetEvalTargetRecordResponse getEvalTargetRecord(GetEvalTargetRecordRequest request);

  /**
   * 批量获取评测目标记录
   * 对应Thrift方法: BatchGetEvalTargetRecords
   */
  BatchGetEvalTargetRecordsResponse batchGetEvalTargetRecords(BatchGetEvalTargetRecordsRequest request);
}
