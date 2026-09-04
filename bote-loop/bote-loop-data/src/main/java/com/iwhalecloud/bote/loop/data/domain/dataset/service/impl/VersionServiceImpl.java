package com.iwhalecloud.bote.loop.data.domain.dataset.service.impl;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Dataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetOpType;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetVersion;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.SnapshotStatus;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IDatasetAPI;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListItemsParams;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.VersionService;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.DatasetWithSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.VersionWithDatasetResult;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.util.JobUtils;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.util.VersionUtils;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 数据集版本服务实现类
 * 迁移对应关系: Go语言DatasetServiceImpl版本相关方法
 * - 功能: 实现数据集版本相关的业务逻辑
 * - 方法实现: 各种数据集版本操作方法实现
 */
@Service
@RequiredArgsConstructor
public class VersionServiceImpl implements VersionService {
  private static final Logger logger = LoggerFactory.getLogger(VersionServiceImpl.class);

  private final IDatasetAPI repo;

  /**
   * 批量获取版本化数据集（带选项）
   */
  public List<VersionWithDatasetResult> batchGetVersionedDatasetsWithOpt(Long spaceId, List<Long> versionIds, Boolean withDeleted) {
    if (versionIds.isEmpty()) {
      return List.of();
    }

    List<DatasetVersion> versions = repo.mGetVersions(spaceId, versionIds);
    List<Long> datasetIds = versions.stream()
      .map(DatasetVersion::getDatasetId)
      .collect(Collectors.toList());

    List<Dataset> datasets = repo.mGetDatasets(spaceId, datasetIds);
    List<Long> schemaIds = versions.stream()
      .map(DatasetVersion::getSchemaId)
      .collect(Collectors.toList());
    List<DatasetSchema> schemas = repo.mGetSchema(spaceId, schemaIds);

    Map<Long, Dataset> datasetMap = datasets.stream()
      .collect(Collectors.toMap(Dataset::getId, dataset -> dataset));
    Map<Long, DatasetSchema> schemaMap = schemas.stream()
      .collect(Collectors.toMap(DatasetSchema::getId, schema -> schema));

    return versions.stream()
      .filter(version -> {
        Dataset dataset = datasetMap.get(version.getDatasetId());
        DatasetSchema schema = schemaMap.get(version.getSchemaId());
        return dataset != null && schema != null;
      })
      .map(version -> VersionWithDatasetResult.builder()
        .version(version)
        .dataset(DatasetWithSchema.builder()
          .dataset(datasetMap.get(version.getDatasetId()))
          .schema(schemaMap.get(version.getSchemaId()))
          .build())
        .build())
      .collect(Collectors.toList());
  }

  /**
   * 获取或设置版本项目数量
   */
  @Transactional
  @SuppressWarnings("PMD.GuardLogStatement")
  public Long getOrSetItemCountOfVersion(DatasetVersion version) {
    if (version.getSnapshotStatus() == SnapshotStatus.COMPLETED) {
      return version.getItemCount();
    }

    Long count = repo.getItemCountOfVersion(version.getId());
    if (count != null) {
      return count;
    }

    logger.info("counting item count of version, dataset_id={}, version_id={}, version={}",
      version.getDatasetId(), version.getId(), version.getVersion());

    ListItemsParams query = newListItemsParamsFromVersion(version);
    Long n = repo.countItems(query);
    logger.info("{} item count found, version={}", n, version.getId());

    repo.setItemCountOfVersion(version.getId(), n);
    return n;
  }

  /**
   * 获取版本（带选项）
   */
  public VersionWithDatasetResult getVersionWithOpt(Long spaceId, Long versionId, Boolean withDeleted) {
    DatasetVersion version = repo.getVersion(spaceId, versionId);
    if (version == null) {
      throw new BssException("version " + versionId + " is not found");
    }

    if (version.getSnapshotStatus() != SnapshotStatus.COMPLETED) {
      Long count = getOrSetItemCountOfVersion(version);
      version.setItemCount(count);
    }

    Dataset dataset = repo.getDataset(spaceId, version.getDatasetId());
    if (dataset == null) {
      return new VersionWithDatasetResult();
    }

    DatasetSchema schema = repo.getSchema(spaceId, version.getSchemaId());
    if (schema == null) {
      throw new BssException("schema not found");
    }

    return VersionWithDatasetResult.builder()
      .version(version)
      .dataset(DatasetWithSchema.builder().dataset(dataset).schema(schema).build())
      .build();
  }

  /**
   * 创建版本
   */
  @Transactional
  @SuppressWarnings("PMD.GuardLogStatement")
  public void createVersion(DatasetWithSchema ds, DatasetVersion version) {
    VersionUtils.validateVersion(ds.getDataset().getLatestVersion(), version.getVersion());
    VersionUtils.patchVersionWithDataset(ds.getDataset(), version);
    createVersionInternal(ds, version);
    logger.info("send create_snapshot msg, version_id={}", version.getId());
    JobUtils.runSnapshotItemJob(ds, version);
  }

  /**
   * 创建版本内部实现
   */
  @Transactional
  public void createVersionInternal(DatasetWithSchema ds, DatasetVersion version) {
    // 插入 version
    repo.createVersion(version);

    // 更新 dataset 关联的版本信息
    Dataset dPatch = Dataset.builder()
      .latestVersion(version.getVersion())
      .nextVersionNum(ds.getDataset().getNextVersionNum() + 1)
      .lastOperation(DatasetOpType.CREATE_VERSION)
      .updatedBy(ds.getDataset().getUpdatedBy())
      .build();

    repo.patchDataset(dPatch, Dataset.builder()
      .spaceId(ds.getDataset().getSpaceId())
      .id(ds.getDataset().getId())
      .build());

    // 锁定 schema 不可变更
    if (ds.getSchema().getImmutable()) {
      return;
    }

    DatasetSchema sPatch = DatasetSchema.builder()
      .id(ds.getSchema().getId())
      .spaceId(ds.getDataset().getSpaceId())
      .updateVersion(ds.getSchema().getUpdateVersion() + 1)
      .immutable(true)
      .build();

    repo.updateSchema(ds.getSchema().getUpdateVersion(), sPatch);
  }

  /**
   * 从版本创建列表项目参数
   */
  private ListItemsParams newListItemsParamsFromVersion(DatasetVersion version) {
    return ListItemsParams.builder()
      .spaceId(version.getSpaceId())
      .datasetId(version.getDatasetId())
      .delVnGt(version.getVersionNum())
      .addVnLte(version.getVersionNum())
      .build();
  }
}
