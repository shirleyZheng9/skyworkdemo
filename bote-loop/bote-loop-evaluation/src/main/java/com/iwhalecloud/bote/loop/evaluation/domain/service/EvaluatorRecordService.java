package com.iwhalecloud.bote.loop.evaluation.domain.service;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.Correction;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRecord;

import java.util.List;

/**
 * 评估器记录服务接口
 * 对应Go: EvaluatorRecordService
 */
public interface EvaluatorRecordService {

  /**
   * 创建 evaluator_version 运行结果
   * 对应Go: CorrectEvaluatorRecord
   */
  void correctEvaluatorRecord(EvaluatorRecord evaluatorRecordDO, Correction correctionDO);

  /**
   * 按 id 查询单个 evaluator_version 运行结果
   * 对应Go: GetEvaluatorRecord
   */
  EvaluatorRecord getEvaluatorRecord(Long evaluatorRecordId, Boolean includeDeleted);

  /**
   * 按 id 批量查询 evaluator_version 运行结果
   * 对应Go: BatchGetEvaluatorRecord
   */
  List<EvaluatorRecord> batchGetEvaluatorRecord(List<Long> evaluatorRecordIds, Boolean includeDeleted);
}
