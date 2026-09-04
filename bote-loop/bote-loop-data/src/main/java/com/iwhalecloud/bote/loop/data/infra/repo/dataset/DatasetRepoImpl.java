package com.iwhalecloud.bote.loop.data.infra.repo.dataset;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Dataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetOpType;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetOperation;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetVersion;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.IOJob;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Item;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemIdentity;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemSnapshot;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IDatasetAPI;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IDatasetRepo;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IIOJobRepo;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IItemRepo;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IItemSnapshotRepo;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IOperationRepo;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.ISchemaRepo;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IVersionRepo;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.DeltaDatasetIOJob;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListDatasetVersionsParams;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListDatasetsParams;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListIOJobsParams;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListItemSnapshotsParams;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListItemsParams;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.oss.OssItemDAO;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.redis.RedisDatasetDAO;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.redis.RedisVersionDAO;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 数据集Repository实现类
 * 迁移对应关系: Go语言DatasetRepo
 * - 功能: 实现数据集数据访问操作
 * - 方法实现: 各种数据集数据操作方法实现
 * <p>
 * Java实现说明:
 * - 对应Go的DatasetRepo结构体
 * - 使用各个子Repo实现数据访问
 * - 提供数据集数据CRUD操作实现
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
@Component
public class DatasetRepoImpl implements IDatasetAPI {
  @Qualifier("datasetRepoNestImpl")
  @Autowired
  private IDatasetRepo datasetRepo;
  @Qualifier("schemaRepoImpl")
  @Autowired
  private ISchemaRepo schemaRepo;
  @Qualifier("versionRepoImpl")
  @Autowired
  private IVersionRepo versionRepo;
  @Qualifier("operationRepoImpl")
  @Autowired
  private IOperationRepo operationRepo;
  @Qualifier("itemRepoImpl")
  @Autowired
  private IItemRepo itemRepo;
  @Qualifier("itemSnapshotRepoImpl")
  @Autowired
  private IItemSnapshotRepo itemSnapshotRepo;
  @Qualifier("iOJobRepoImpl")
  @Autowired
  private IIOJobRepo ioJobRepo;
  @Autowired
  private RedisDatasetDAO redisDatasetDAO;
  @Autowired
  private RedisVersionDAO versionRedisDAO;
  @Autowired
  private OssItemDAO ossItemDAO;

  // DatasetRepo methods
  @Override
  public Long getItemCount(Long datasetId) {
    return redisDatasetDAO.getItemCount(datasetId);
  }

  @Override
  public Map<Long, Long> mGetItemCount(List<Long> datasetIds) {
    return redisDatasetDAO.mGetItemCount(datasetIds);
  }

  @Override
  public void setItemCount(Long datasetId, Long count) {
    redisDatasetDAO.setItemCount(datasetId, count);
  }

  @Override
  public Long incrItemCount(Long datasetId, Long count) {
    return redisDatasetDAO.incrItemCount(datasetId, count);
  }

  @Override
  public void createDatasetAndSchema(Dataset dataset, List<FieldSchema> fields) {
    datasetRepo.createDatasetAndSchema(dataset, fields);
  }

  @Override
  public Dataset getDataset(Long spaceId, Long id) {
    return datasetRepo.getDataset(spaceId, id);
  }

  @Override
  public List<Dataset> mGetDatasets(Long spaceId, List<Long> ids) {
    return datasetRepo.mGetDatasets(spaceId, ids);
  }

  @Override
  public void patchDataset(Dataset patch, Dataset where) {
    datasetRepo.patchDataset(patch, where);
  }

  @Override
  public void deleteDataset(Long spaceId, Long datasetId) {
    datasetRepo.deleteDataset(spaceId, datasetId);
  }

  @Override
  public PageInfo<Dataset> listDatasets(ListDatasetsParams params) {
    return datasetRepo.listDatasets(params);
  }

  @Override
  public Long countDatasets(ListDatasetsParams params) {
    return datasetRepo.countDatasets(params);
  }

  // SchemaRepo methods
  @Override
  public DatasetSchema getSchema(Long spaceId, Long id) {
    return schemaRepo.getSchema(spaceId, id);
  }

  @Override
  public List<DatasetSchema> mGetSchema(Long spaceId, List<Long> ids) {
    return schemaRepo.mGetSchema(spaceId, ids);
  }

  @Override
  public void createSchema(DatasetSchema schema) {
    schemaRepo.createSchema(schema);
  }

  @Override
  public void updateSchema(Long updateVersion, DatasetSchema schema) {
    schemaRepo.updateSchema(updateVersion, schema);
  }

  // VersionRepo methods
  @Override
  public void createVersion(DatasetVersion version) {
    versionRepo.createVersion(version);
  }

  @Override
  public DatasetVersion getVersion(Long spaceId, Long versionId) {
    return versionRepo.getVersion(spaceId, versionId);
  }

  @Override
  public List<DatasetVersion> mGetVersions(Long spaceId, List<Long> ids) {
    return versionRepo.mGetVersions(spaceId, ids);
  }

  @Override
  public Long getItemCountOfVersion(Long versionId) {
    return versionRedisDAO.getItemCountOfVersion(versionId);
  }

  @Override
  public void setItemCountOfVersion(Long datasetId, Long n) {
    versionRedisDAO.setItemCountOfVersion(datasetId, n);
  }

  @Override
  public PageInfo<DatasetVersion> listVersions(ListDatasetVersionsParams params) {
    return versionRepo.listVersions(params);
  }

  @Override
  public Long countVersions(ListDatasetVersionsParams params) {
    return versionRepo.countVersions(params);
  }

  @Override
  public void patchVersion(DatasetVersion patch, DatasetVersion where) {
    versionRepo.patchVersion(patch, where);
  }

  // OperationRepo methods
  @Override
  public void addDatasetOperation(Long datasetId, DatasetOperation op) {
    operationRepo.addDatasetOperation(datasetId, op);
  }

  @Override
  public void delDatasetOperation(Long datasetId, DatasetOpType opType, String id) {
    operationRepo.delDatasetOperation(datasetId, opType, id);
  }

  @Override
  public Map<DatasetOpType, List<DatasetOperation>> mGetDatasetOperations(Long datasetId, List<DatasetOpType> opTypes) {
    return operationRepo.mGetDatasetOperations(datasetId, opTypes);
  }

  // ItemRepo methods
  @Override
  public Long countItems(ListItemsParams params) {
    return itemRepo.countItems(params);
  }

  @Override
  public Integer mSetItemData(List<Item> items) {
    return ossItemDAO.mSetItemData(items);
  }

  @Override
  public void mGetItemData(List<Item> items) {
    ossItemDAO.mGetItemData(items);
  }

  @Override
  public Long mCreateItems(List<Item> items) {
    return itemRepo.mCreateItems(items);
  }

  @Override
  public PageInfo<Item> listItems(ListItemsParams params) {
    return itemRepo.listItems(params);
  }

  @Override
  public void updateItem(Item item) {
    itemRepo.updateItem(item);
  }

  @Override
  public void deleteItems(Long spaceId, List<Long> ids) {
    itemRepo.deleteItems(spaceId, ids);
  }

  @Override
  public void archiveItems(Long spaceId, Long delVn, List<Long> ids) {
    itemRepo.archiveItems(spaceId, delVn, ids);
  }

  @Override
  public List<ItemIdentity> clearDataset(Long spaceId, Long datasetId, Long delVn) {
    return itemRepo.clearDataset(spaceId, datasetId, delVn);
  }

  // ItemSnapshotRepo methods
  @Override
  public Long batchUpsertItemSnapshots(List<ItemSnapshot> snapshots) {
    return itemSnapshotRepo.batchUpsertItemSnapshots(snapshots);
  }

  @Override
  public PageInfo<ItemSnapshot> listItemSnapshots(ListItemSnapshotsParams params) {
    return itemSnapshotRepo.listItemSnapshots(params);
  }

  @Override
  public Long countItemSnapshots(ListItemSnapshotsParams params) {
    return itemSnapshotRepo.countItemSnapshots(params);
  }

  // IOJobRepo methods
  @Override
  public void createIOJob(IOJob job) {
    ioJobRepo.createIOJob(job);
  }

  @Override
  public IOJob getIOJob(Long id) {
    return ioJobRepo.getIOJob(id);
  }

  @Override
  public void updateIOJob(Long id, DeltaDatasetIOJob delta) {
    ioJobRepo.updateIOJob(id, delta);
  }

  @Override
  public List<IOJob> listIOJobs(ListIOJobsParams params) {
    return ioJobRepo.listIOJobs(params);
  }
}
