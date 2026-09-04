package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.IDatasetRPCAdapter;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.CreateDatasetParam;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ItemSnapshotMappingsResult;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ListDatasetsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CreateEvaluationSetParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSet;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemSnapshotMappingResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListEvaluationSetsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.UpdateEvaluationSetParam;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluationSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 评估集服务实现类
 */
@Service
@RequiredArgsConstructor
public class EvaluationSetServiceImpl implements EvaluationSetService {
  private final IDatasetRPCAdapter datasetRPCAdapter;

  @Override
  public Long createEvaluationSet(CreateEvaluationSetParam param) {
    Assert.notNull(param, "参数不能为空");
    // 依赖数据集服务
    CreateDatasetParam createParam = CreateDatasetParam.builder()
      .spaceId(param.getSpaceId())
      .name(param.getName())
      .desc(param.getDescription())
      .evaluationSetItems(param.getEvaluationSetSchema())
      .bizCategory(param.getBizCategory())
      .session(param.getSession())
      .catalogItemId(param.getCatalogItemId())
      .build();

    return datasetRPCAdapter.createDataset(createParam);
  }

  @Override
  public void updateEvaluationSet(UpdateEvaluationSetParam param) {
    Assert.notNull(param, "参数不能为空");
    // 依赖数据集服务
    datasetRPCAdapter.updateDataset(param.getSpaceId(), param.getEvaluationSetId(),
      param.getName(), param.getDescription(), param.getCatalogItemId());
  }

  @Override
  public void deleteEvaluationSet(Long spaceId, Long evaluationSetId) {
    // 依赖数据集服务
    datasetRPCAdapter.deleteDataset(spaceId, evaluationSetId);
  }

  @Override
  public EvaluationSet getEvaluationSet(Long spaceId, Long evaluationSetId, Boolean deletedAt) {
    // 依赖数据集服务
    return datasetRPCAdapter.getDataset(spaceId, evaluationSetId, deletedAt);
  }

  @Override
  public List<EvaluationSet> batchGetEvaluationSets(Long spaceId, List<Long> evaluationSetIds, Boolean deletedAt) {
    // 依赖数据集服务
    return datasetRPCAdapter.batchGetDatasets(spaceId, evaluationSetIds, deletedAt);
  }

  @Override
  public PageInfo<EvaluationSet> listEvaluationSets(ListEvaluationSetsParam param) {
    Assert.notNull(param, "参数不能为空");
    // 依赖数据集服务
    ListDatasetsParam listParam = ListDatasetsParam.builder()
      .spaceId(param.getSpaceId())
      .evaluationSetIds(param.getEvaluationSetIds())
      .catalogItemId(param.getCatalogItemId())
      .name(param.getName())
      .creators(param.getCreators())
      .pageNumber(param.getPageNumber())
      .pageSize(param.getPageSize())
      .pageToken(param.getPageToken())
      .orderBys(param.getOrderBys())
      .build();
    return datasetRPCAdapter.listDatasets(listParam);
  }

  @Override
  public ItemSnapshotMappingResult queryItemSnapshotMappings(Long spaceId, Long datasetId, Long versionId) {
    ItemSnapshotMappingsResult itemSnapshotMappingsResult = datasetRPCAdapter.queryItemSnapshotMappings(spaceId, datasetId, versionId);
    return ItemSnapshotMappingResult.builder()
      .fieldMappings(itemSnapshotMappingsResult.getFieldMappings())
      .syncCkDate(itemSnapshotMappingsResult.getSyncCkDate())
      .build();
  }
}
