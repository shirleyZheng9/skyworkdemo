package com.iwhalecloud.bote.loop.evaluation.domain.service;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchGetEvalTargetBySourceReqParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BotInfoType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTarget;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetCreateResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetInputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExecuteTargetCtx;

import java.util.List;

/**
 * 评估目标服务接口
 * 对应Go: IEvalTargetService
 */
public interface IEvalTargetService {

  /**
   * 创建评估目标
   * 对应Go: CreateEvalTarget
   */
  EvalTargetCreateResult createEvalTarget(Long spaceId, String sourceTargetId, String sourceTargetVersion,
                                          EvalTargetType targetType, String botPublishVersion, BotInfoType botInfoType);

  /**
   * 获取评估目标
   * 对应Go: GetEvalTarget
   */
  EvalTarget getEvalTarget(Long targetId);

  /**
   * 获取评估目标版本
   * 对应Go: GetEvalTargetVersion
   */
  EvalTarget getEvalTargetVersion(Long spaceId, Long versionId, Boolean needSourceInfo);

  /**
   * 按源批量获取评估目标
   * 对应Go: BatchGetEvalTargetBySource
   */
  List<EvalTarget> batchGetEvalTargetBySource(BatchGetEvalTargetBySourceReqParam param);

  /**
   * 批量获取评估目标版本
   * 对应Go: BatchGetEvalTargetVersion
   */
  List<EvalTarget> batchGetEvalTargetVersion(Long spaceId, List<Long> versionIds, Boolean needSourceInfo);

  /**
   * 执行目标
   * 对应Go: ExecuteTarget
   */
  EvalTargetRecord executeTarget(Long spaceId, Long targetId, Long targetVersionId,
                                 ExecuteTargetCtx param, EvalTargetInputData inputData);

  /**
   * 按ID获取记录
   * 对应Go: GetRecordByID
   */
  EvalTargetRecord getRecordById(Long spaceId, Long recordId);

  /**
   * 批量按ID获取记录
   * 对应Go: BatchGetRecordByIDs
   */
  List<EvalTargetRecord> batchGetRecordByIds(Long spaceId, List<Long> recordIds);
}
