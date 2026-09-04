package com.iwhalecloud.bote.loop.data.domain.dataset.service.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Dataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetVersion;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.IOJob;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Item;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.JobRunMessage;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.DatasetDomainService;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.DatasetWithSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.IndexedItem;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.MAddItemOpt;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.SearchDatasetsParam;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.UpdateDatasetParam;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.VersionWithDatasetResult;
import com.iwhalecloud.bote.loop.data.domain.entity.Provider;
import java.io.File;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 数据集服务实现类
 * 迁移对应关系: Go语言service.DatasetServiceImpl
 * - 功能: 实现数据集相关的业务逻辑服务
 * - 方法实现: 各种数据集操作方法实现
 */
@Service
@RequiredArgsConstructor
public class DatasetDomainServiceImpl implements DatasetDomainService {
  private final DatasetServiceImpl datasetService;
  private final SchemaServiceImpl schemaService;
  private final VersionServiceImpl versionService;
  private final ItemServiceImpl itemService;
  private final ItemSnapshotServiceImpl itemSnapshotService;
  private final IOJobServiceImpl ioJobService;
  private final FileStoreServiceImpl fileStoreService;

  // ==================== DatasetServiceCore 接口实现 ====================

  @Override
  public void createDataset(Dataset dataset, List<FieldSchema> fields) {
    datasetService.createDataset(dataset, fields);
  }

  @Override
  public void updateDataset(UpdateDatasetParam param) {
    datasetService.updateDataset(param);
  }

  @Override
  public void deleteDataset(Long spaceID, Long id) {
    datasetService.deleteDataset(spaceID, id);
  }

  @Override
  public DatasetWithSchema getDataset(Long spaceID, Long id) {
    return datasetService.getDataset(spaceID, id);
  }

  @Override
  public List<DatasetWithSchema> batchGetDataset(Long spaceID, List<Long> ids) {
    return datasetService.batchGetDataset(spaceID, ids);
  }

  @Override
  public DatasetWithSchema getDatasetWithOpt(Long spaceID, Long id, Boolean withDeleted) {
    return datasetService.getDatasetWithOpt(spaceID, id, withDeleted);
  }

  @Override
  public List<DatasetWithSchema> batchGetDatasetWithOpt(Long spaceID, List<Long> ids, Boolean withDeleted) {
    return datasetService.batchGetDatasetWithOpt(spaceID, ids, withDeleted);
  }

  @Override
  public PageInfo<DatasetWithSchema> searchDataset(SearchDatasetsParam req) {
    return datasetService.searchDataset(req);
  }

  // ==================== SchemaService 接口实现 ====================

  @Override
  public void updateSchema(Dataset ds, List<FieldSchema> fields, String updatedBy) {
    schemaService.updateSchema(ds, fields, updatedBy);
  }

  // ==================== VersionService 接口实现 ====================

  @Override
  public void createVersion(DatasetWithSchema ds, DatasetVersion version) {
    versionService.createVersion(ds, version);
  }

  @Override
  public VersionWithDatasetResult getVersionWithOpt(Long spaceID, Long versionID, Boolean withDeleted) {
    return versionService.getVersionWithOpt(spaceID, versionID, withDeleted);
  }

  @Override
  public Long getOrSetItemCountOfVersion(DatasetVersion version) {
    return versionService.getOrSetItemCountOfVersion(version);
  }

  @Override
  public List<VersionWithDatasetResult> batchGetVersionedDatasetsWithOpt(Long spaceID, List<Long> versionIDs, Boolean withDeleted) {
    return versionService.batchGetVersionedDatasetsWithOpt(spaceID, versionIDs, withDeleted);
  }

  // ==================== ItemService 接口实现 ====================

  @Override
  public List<IndexedItem> batchCreateItems(DatasetWithSchema ds, List<IndexedItem> items, MAddItemOpt opt) {
    return itemService.batchCreateItems(ds, items, opt);
  }

  @Override
  public PageInfo<Item> batchGetItems(Long spaceID, Long datasetID, List<Long> itemIDs) {
    return itemService.batchGetItems(spaceID, datasetID, itemIDs);
  }

  @Override
  public Item getItem(Long spaceID, Long datasetID, Long itemID) {
    return itemService.getItem(spaceID, datasetID, itemID);
  }

  @Override
  public void loadItemData(Item... items) {
    itemService.loadItemData(items);
  }

  @Override
  public void archiveAndCreateItem(DatasetWithSchema ds, Long oldID, Item item) {
    itemService.archiveAndCreateItem(ds, oldID, item);
  }

  @Override
  public void updateItem(DatasetWithSchema ds, Item item) {
    itemService.updateItem(ds, item);
  }

  @Override
  public void batchDeleteItems(DatasetWithSchema ds, Item... items) {
    itemService.batchDeleteItems(ds, items);
  }

  @Override
  public void clearDataset(DatasetWithSchema ds) {
    itemService.clearDataset(ds);
  }

  // ==================== ItemSnapshotService 接口实现 ====================

  @Override
  public void runSnapshotItemJob(JobRunMessage msg) {
    itemSnapshotService.runSnapshotItemJob(msg);
  }

  // ==================== IOJobService 接口实现 ====================

  @Override
  public IOJob getIOJob(Long jobID) {
    return ioJobService.getIOJob(jobID);
  }

  @Override
  public void createIOJob(IOJob job) {
    ioJobService.createIOJob(job);
  }

  @Override
  public void runIOJob(JobRunMessage msg) {
    ioJobService.runIOJob(msg);
  }

  @Override
  public File statFile(Provider provider, String path) {
    return fileStoreService.statFile(provider, path);
  }
}
