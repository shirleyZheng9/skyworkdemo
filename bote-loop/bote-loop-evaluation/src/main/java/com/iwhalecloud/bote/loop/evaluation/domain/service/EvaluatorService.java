package com.iwhalecloud.bote.loop.evaluation.domain.service;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.Evaluator;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorInputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorListResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorOutputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorRecord;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluatorVersionListResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListEvaluatorRequest;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListEvaluatorVersionRequest;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.RunEvaluatorParam;

import java.util.List;

/**
 * 评估器服务接口
 * 对应Go: EvaluatorService
 */
public interface EvaluatorService {

  /**
   * 按查询条件查询 evaluator_version
   * 对应Go: ListEvaluator
   */
  EvaluatorListResult listEvaluator(ListEvaluatorRequest request);

  /**
   * 按 id 批量查询 evaluator_version
   * 对应Go: BatchGetEvaluator
   */
  List<Evaluator> batchGetEvaluator(Long spaceId, List<Long> evaluatorIds, Boolean includeDeleted);

  /**
   * 按 id 单个查询 evaluator_version
   * 对应Go: GetEvaluator
   */
  Evaluator getEvaluator(Long spaceId, Long evaluatorId, Boolean includeDeleted);

  /**
   * 创建 evaluator_version
   * 对应Go: CreateEvaluator
   */
  Long createEvaluator(Evaluator evaluator, String cid);

  /**
   * 修改 evaluator_version
   * 对应Go: UpdateEvaluatorMeta
   */
  void updateEvaluatorMeta(Long id, Long spaceId, String name, String description, String userId, Long catalogItemId);

  /**
   * 修改 evaluator_version draft
   * 对应Go: UpdateEvaluatorDraft
   */
  void updateEvaluatorDraft(Evaluator versionDO);

  /**
   * 删除 evaluator_version
   * 对应Go: DeleteEvaluator
   */
  void deleteEvaluator(List<Long> evaluatorIds, String userId);

  /**
   * evaluator_version 运行
   * 对应Go: RunEvaluator
   */
  EvaluatorRecord runEvaluator(RunEvaluatorParam request);

  /**
   * 调试 evaluator_version
   * 对应Go: DebugEvaluator
   */
  EvaluatorOutputData debugEvaluator(Long workspaceId, Evaluator evaluatorDO, EvaluatorInputData inputData);

  /**
   * 按 version id 单个查询 evaluator_version version
   * 对应Go: GetEvaluatorVersion
   */
  Evaluator getEvaluatorVersion(Long evaluatorVersionId, Boolean includeDeleted);

  /**
   * 按 version id 批量查询 evaluator_version version
   * 对应Go: BatchGetEvaluatorVersion
   */
  List<Evaluator> batchGetEvaluatorVersion(Long spaceId, List<Long> evaluatorVersionIds, Boolean includeDeleted);

  /**
   * 按条件查询 evaluator_version version
   * 对应Go: ListEvaluatorVersion
   */
  EvaluatorVersionListResult listEvaluatorVersion(ListEvaluatorVersionRequest request);

  /**
   * 提交 evaluator_version 版本
   * 对应Go: SubmitEvaluatorVersion
   */
  Evaluator submitEvaluatorVersion(Evaluator evaluatorVersionDO, String version, String description, String cid);

  /**
   * 检查名称是否存在
   * 对应Go: CheckNameExist
   */
  Boolean checkNameExist(Long spaceId, Long evaluatorId, String name);
}
