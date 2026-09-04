package com.iwhalecloud.bote.loop.evaluation.domain.service;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CreateEvaluationSetParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSet;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemSnapshotMappingResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListEvaluationSetsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.UpdateEvaluationSetParam;

import java.util.List;

/**
 * 评估集服务接口
 * 对应Go: IEvaluationSetService
 */
public interface EvaluationSetService {

  /**
   * 创建评估集
   * 对应Go方法: CreateEvaluationSet
   *
   * @param param 创建评估集参数
   * @return 评估集ID
   */
  Long createEvaluationSet(CreateEvaluationSetParam param);

  /**
   * 更新评估集
   * 对应Go方法: UpdateEvaluationSet
   *
   * @param param 更新评估集参数
   */
  void updateEvaluationSet(UpdateEvaluationSetParam param);

  /**
   * 删除评估集
   * 对应Go方法: DeleteEvaluationSet
   *
   * @param spaceId 工作空间ID
   * @param evaluationSetId 评估集ID
   */
  void deleteEvaluationSet(Long spaceId, Long evaluationSetId);

  /**
   * 获取评估集
   * 对应Go方法: GetEvaluationSet
   *
   * @param spaceId 工作空间ID
   * @param evaluationSetId 评估集ID
   * @param deletedAt 是否包含已删除的记录
   * @return 评估集实体
   */
  EvaluationSet getEvaluationSet(Long spaceId, Long evaluationSetId, Boolean deletedAt);

  /**
   * 批量获取评估集
   * 对应Go方法: BatchGetEvaluationSets
   *
   * @param spaceId 工作空间ID
   * @param evaluationSetIds 评估集ID列表
   * @param deletedAt 是否包含已删除的记录
   * @return 评估集实体列表
   */
  List<EvaluationSet> batchGetEvaluationSets(Long spaceId, List<Long> evaluationSetIds, Boolean deletedAt);

  /**
   * 列表查询评估集
   * 对应Go方法: ListEvaluationSets
   *
   * @param param 列表查询参数
   * @return 评估集列表、总数、下一页令牌
   */
  PageInfo<EvaluationSet> listEvaluationSets(ListEvaluationSetsParam param);

  /**
   * 查询数据项快照映射
   * 对应Go方法: QueryItemSnapshotMappings
   *
   * @param spaceId 工作空间ID
   * @param datasetId 数据集ID
   * @param versionId 版本ID
   * @return 字段映射列表、同步检查日期
   */
  ItemSnapshotMappingResult queryItemSnapshotMappings(Long spaceId, Long datasetId, Long versionId);
}
