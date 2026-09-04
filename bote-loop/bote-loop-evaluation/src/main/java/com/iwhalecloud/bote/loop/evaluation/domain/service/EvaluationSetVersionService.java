package com.iwhalecloud.bote.loop.evaluation.domain.service;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchGetEvaluationSetVersionsResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CreateEvaluationSetVersionParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetVersion;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetVersionResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListEvaluationSetVersionsParam;

import java.util.List;

/**
 * 评估集版本服务接口
 * 对应Go: EvaluationSetVersionService
 */
public interface EvaluationSetVersionService {

  /**
   * 创建评估集版本
   * 对应Go方法: CreateEvaluationSetVersion
   *
   * @param param 创建评估集版本参数
   * @return 版本ID
   */
  Long createEvaluationSetVersion(CreateEvaluationSetVersionParam param);

  /**
   * 获取评估集版本
   * 对应Go方法: GetEvaluationSetVersion
   *
   * @param spaceId 工作空间ID
   * @param versionId 版本ID
   * @param deletedAt 是否包含已删除的记录
   * @return 包含版本和评估集信息的结果
   */
  EvaluationSetVersionResult getEvaluationSetVersion(Long spaceId, Long versionId, Boolean deletedAt);

  /**
   * 查询评估集版本列表
   * 对应Go方法: ListEvaluationSetVersions
   *
   * @param param 查询参数
   * @return 包含版本列表、总数和分页信息的查询结果
   */
  PageInfo<EvaluationSetVersion> listEvaluationSetVersions(ListEvaluationSetVersionsParam param);

  /**
   * 批量获取评估集版本
   * 对应Go方法: BatchGetEvaluationSetVersions
   *
   * @param spaceId 工作空间ID
   * @param evaluationSetIds 评估集ID列表
   * @param deletedAt 是否包含已删除的记录
   * @return 版本结果列表
   */
  List<BatchGetEvaluationSetVersionsResult> batchGetEvaluationSetVersions(Long spaceId, List<Long> evaluationSetIds, Boolean deletedAt);
}
