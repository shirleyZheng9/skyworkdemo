package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.IDatasetRPCAdapter;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.BatchGetVersionedDatasetsResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchGetEvaluationSetVersionsResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.CreateEvaluationSetVersionParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetVersion;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetVersionResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListEvaluationSetVersionsParam;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluationSetVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评估集版本服务实现类
 */
@Service
@RequiredArgsConstructor
public class EvaluationSetVersionServiceImpl implements EvaluationSetVersionService {
  private final IDatasetRPCAdapter datasetRPCAdapter;

  @Override
  public Long createEvaluationSetVersion(CreateEvaluationSetVersionParam param) {
    Assert.notNull(param, "参数不能为空");
    // 依赖数据集服务
    return datasetRPCAdapter.createDatasetVersion(param.getSpaceId(), param.getEvaluationSetId(),
      param.getVersion(), param.getDescription());
  }

  @Override
  public EvaluationSetVersionResult getEvaluationSetVersion(Long spaceId, Long versionId, Boolean deletedAt) {
    // 依赖数据集服务
    return datasetRPCAdapter.getDatasetVersion(spaceId, versionId, deletedAt);
  }

  @Override
  public PageInfo<EvaluationSetVersion> listEvaluationSetVersions(ListEvaluationSetVersionsParam param) {
    Assert.notNull(param, "参数不能为空");
    // 依赖数据集服务
    return datasetRPCAdapter.listDatasetVersions(param.getSpaceId(), param.getEvaluationSetId(),
      param.getPageToken(), param.getPageNumber(), param.getPageSize(), param.getVersionLike());
  }

  @Override
  public List<BatchGetEvaluationSetVersionsResult> batchGetEvaluationSetVersions(Long spaceId,
                                                                                 List<Long> versionIds, Boolean deletedAt) {
    // 依赖数据集服务
    List<BatchGetVersionedDatasetsResult> datasets = datasetRPCAdapter.batchGetVersionedDatasets(
      spaceId, versionIds, deletedAt);

    // 转换结果
    return datasets.stream()
      .map(dataset -> BatchGetEvaluationSetVersionsResult.builder()
        .version(dataset.getVersion())
        .evaluationSet(dataset.getEvaluationSet())
        .build())
      .collect(Collectors.toList());
  }
}
