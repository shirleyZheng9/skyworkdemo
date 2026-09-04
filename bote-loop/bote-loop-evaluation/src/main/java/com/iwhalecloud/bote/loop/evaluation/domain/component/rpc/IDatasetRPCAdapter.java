package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.BatchCreateDatasetItemsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.BatchGetDatasetItemsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.BatchGetVersionedDatasetsResult;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.CreateDatasetParam;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ItemSnapshotMappingsResult;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ListDatasetItemsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ListDatasetsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchCreateEvaluationSetItemsResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSet;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetItem;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetVersion;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetVersionResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldSchema;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Turn;

import java.util.List;

/**
 * 数据集RPC适配器接口
 */
public interface IDatasetRPCAdapter {

  /**
   * 创建数据集
   */
  Long createDataset(CreateDatasetParam param);

  /**
   * 更新数据集
   */
  void updateDataset(Long spaceId, Long evaluationSetId, String name, String desc, Long catalogItemId);

  /**
   * 删除数据集
   */
  void deleteDataset(Long spaceId, Long evaluationSetId);

  /**
   * 获取数据集
   */
  EvaluationSet getDataset(Long spaceId, Long evaluationSetId, Boolean deletedAt);

  /**
   * 批量获取数据集
   */
  List<EvaluationSet> batchGetDatasets(Long spaceId, List<Long> evaluationSetIds, Boolean deletedAt);

  /**
   * 分页查询数据集列表
   */
  PageInfo<EvaluationSet> listDatasets(ListDatasetsParam param);

  /**
   * 创建数据集版本
   */
  Long createDatasetVersion(Long spaceId, Long evaluationSetId, String version, String desc);

  /**
   * 获取数据集版本
   */
  EvaluationSetVersionResult getDatasetVersion(Long spaceId, Long versionId, Boolean deletedAt);

  /**
   * 批量获取版本化数据集
   */
  List<BatchGetVersionedDatasetsResult> batchGetVersionedDatasets(Long spaceId, List<Long> versionIds, Boolean deletedAt);

  /**
   * 分页查询数据集版本列表
   */
  PageInfo<EvaluationSetVersion> listDatasetVersions(Long spaceId, Long evaluationSetId, String pageToken,
                                                 Integer pageNumber, Integer pageSize, String versionLike);

  /**
   * 更新数据集模式
   */
  void updateDatasetSchema(Long spaceId, Long evaluationSetId, List<FieldSchema> schemas);

  /**
   * 批量创建数据集项目
   */
  BatchCreateEvaluationSetItemsResult batchCreateDatasetItems(BatchCreateDatasetItemsParam param);

  /**
   * 更新数据集项目
   */
  void updateDatasetItem(Long spaceId, Long evaluationSetId, Long itemId, List<Turn> turns);

  /**
   * 批量删除数据集项目
   */
  void batchDeleteDatasetItems(Long spaceId, Long evaluationSetId, List<Long> itemIds);

  /**
   * 分页查询数据集项目列表
   */
  PageInfo<EvaluationSetItem> listDatasetItems(ListDatasetItemsParam param);

  /**
   * 根据版本分页查询数据集项目列表
   */
  PageInfo<EvaluationSetItem> listDatasetItemsByVersion(ListDatasetItemsParam param);

  /**
   * 批量获取数据集项目
   */
  PageInfo<EvaluationSetItem> batchGetDatasetItems(BatchGetDatasetItemsParam param);

  /**
   * 根据版本批量获取数据集项目
   */
  PageInfo<EvaluationSetItem> batchGetDatasetItemsByVersion(BatchGetDatasetItemsParam param);

  /**
   * 清空评估集草稿项目
   */
  void clearEvaluationSetDraftItem(Long spaceId, Long evaluationSetId);

  /**
   * 查询项目快照映射
   */
  ItemSnapshotMappingsResult queryItemSnapshotMappings(Long spaceId, Long datasetId, Long versionId);
}
