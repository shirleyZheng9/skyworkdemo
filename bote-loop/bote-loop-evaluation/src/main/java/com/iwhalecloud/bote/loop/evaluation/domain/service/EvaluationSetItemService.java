package com.iwhalecloud.bote.loop.evaluation.domain.service;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchCreateEvaluationSetItemsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchCreateEvaluationSetItemsResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchGetEvaluationSetItemsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetItem;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListEvaluationSetItemsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Turn;

import java.util.List;

/**
 * 评估集数据项服务接口
 * 对应Go: EvaluationSetItemService
 */
public interface EvaluationSetItemService {

  /**
   * 批量创建评估集数据项
   * 对应Go方法: BatchCreateEvaluationSetItems
   *
   * @param param 批量创建参数
   * @return 包含ID映射和错误信息的批量创建结果
   */
  BatchCreateEvaluationSetItemsResult batchCreateEvaluationSetItems(BatchCreateEvaluationSetItemsParam param);

  /**
   * 更新评估集数据项
   * 对应Go方法: UpdateEvaluationSetItem
   *
   * @param spaceId 工作空间ID
   * @param evaluationSetId 评估集ID
   * @param itemId 数据项ID
   * @param turns 对话轮次列表
   */
  void updateEvaluationSetItem(Long spaceId, Long evaluationSetId, Long itemId, List<Turn> turns);

  /**
   * 批量删除评估集数据项
   * 对应Go方法: BatchDeleteEvaluationSetItems
   *
   * @param spaceId 工作空间ID
   * @param evaluationSetId 评估集ID
   * @param itemIds 数据项ID列表
   */
  void batchDeleteEvaluationSetItems(Long spaceId, Long evaluationSetId, List<Long> itemIds);

  /**
   * 查询评估集数据项列表
   * 对应Go方法: ListEvaluationSetItems
   *
   * @param param 查询参数
   * @return 包含数据项列表、总数和分页信息的查询结果
   */
  PageInfo<EvaluationSetItem> listEvaluationSetItems(ListEvaluationSetItemsParam param);

  /**
   * 批量获取评估集数据项
   * 对应Go方法: BatchGetEvaluationSetItems
   *
   * @param param 批量获取参数
   * @return 数据项列表
   */
  PageInfo<EvaluationSetItem> batchGetEvaluationSetItems(BatchGetEvaluationSetItemsParam param);

  /**
   * 清空评估集草稿数据项
   * 对应Go方法: ClearEvaluationSetDraftItem
   *
   * @param spaceId 工作空间ID
   * @param evaluationSetId 评估集ID
   */
  void clearEvaluationSetDraftItem(Long spaceId, Long evaluationSetId);
}
