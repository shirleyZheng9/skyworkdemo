package com.iwhalecloud.bote.loop.data.domain.dataset.service.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.data.domain.component.conf.IConfig;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Dataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetFeatures;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetSpec;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetStatus;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.DatasetVisibility;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.SecurityLevel;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.IDatasetAPI;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListDatasetsParams;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.DatasetService;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.DatasetWithSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.SearchDatasetsParam;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.UpdateDatasetParam;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.util.SchemaUtils;
import com.iwhalecloud.bote.loop.data.pkg.pagination.Paginator;
import com.iwhalecloud.bote.loop.data.pkg.pagination.PaginatorFactory;
import com.iwhalecloud.bss.litchi.base.exception.BssException;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class DatasetServiceImpl implements DatasetService {

  private final IDatasetAPI repo;
  private final IConfig config;

  @Override
  @Transactional
  public void createDataset(Dataset dataset, List<FieldSchema> fields) {
    buildNewDataset(dataset);
    SchemaUtils.genFieldKeys(fields);
    SchemaUtils.validateSchema(dataset, fields);
    repo.createDatasetAndSchema(dataset, fields);
  }

  @Override
  @Transactional
  public void updateDataset(UpdateDatasetParam param) {
    final Dataset ds = repo.getDataset(param.getSpaceId(), param.getDatasetId());
    if (ds == null) {
      throw new BssException(String.format("dataset %s not found", param.getDatasetId()));
    }
    Dataset patch = new Dataset();
    String name = param.getName();
    String description = param.getDescription();
    String updatedBy = param.getUpdatedBy();
    patch.setName(name);
    patch.setDescription(description);
    patch.setLastOperation(param.getLastOperation());
    patch.setUpdatedBy(updatedBy);
    patch.setUpdatedAt(new Date());
    patch.setCatalogItemId(param.getCatalogItemId());

    Dataset where = new Dataset();
    where.setSpaceId(ds.getSpaceId());
    where.setId(ds.getId());
    repo.patchDataset(patch, where);
  }

  @Override
  @Transactional
  public void deleteDataset(Long spaceId, Long id) {
    final Dataset ds = repo.getDataset(spaceId, id);
    if (ds == null) {
      throw new BssException(String.format("dataset %s not found", id));
    }
    repo.deleteDataset(spaceId, id);
  }

  @Override
  public DatasetWithSchema getDataset(Long spaceId, Long id) {

    Dataset dataset = repo.getDataset(spaceId, id);
    if (dataset == null) {
      return null;
    }

    // 使用 dataset.getSchemaId() 查询 schema，而不是使用 datasetId
    DatasetSchema schema = null;
    if (dataset.getSchemaId() != null) {
      schema = repo.getSchema(spaceId, dataset.getSchemaId());
    }

    DatasetWithSchema result = new DatasetWithSchema();

    result.setDataset(dataset);
    result.setSchema(schema);

    return result;
  }

  @Override
  public List<DatasetWithSchema> batchGetDataset(Long spaceId, List<Long> ids) {

    List<Dataset> datasets = repo.mGetDatasets(spaceId, ids);

    List<Long> schemaIDs = datasets.stream().map(Dataset::getSchemaId).toList();

    List<DatasetSchema> schemas = repo.mGetSchema(spaceId, schemaIDs);

    Map<Long, DatasetSchema> schemaM = schemas.stream().collect(Collectors.toMap(DatasetSchema::getId, o -> o));

    return datasets.stream().map(o -> {
      DatasetWithSchema datasetWithSchema = new DatasetWithSchema();
      datasetWithSchema.setDataset(o);
      datasetWithSchema.setSchema(schemaM.get(o.getSchemaId()));
      return datasetWithSchema;
    }).toList();
  }

  @Override
  public DatasetWithSchema getDatasetWithOpt(Long spaceId, Long id, Boolean withDeleted) {
    DatasetWithSchema result = getDataset(spaceId, id);
    if (result == null && (withDeleted == null || !withDeleted)) {
      return null;
    }
    // TODO: 如果 withDeleted 为 true，需要查询已删除的数据集
    return result;
  }

  @Override
  public List<DatasetWithSchema> batchGetDatasetWithOpt(Long spaceId, List<Long> ids, Boolean withDeleted) {
    return batchGetDataset(spaceId, ids);
  }

  @Override
  public PageInfo<DatasetWithSchema> searchDataset(SearchDatasetsParam req) {
    Paginator pg = PaginatorFactory.newPaginator(req.getOrderBy(), req.getPageSize(), req.getPageNumber());
    ListDatasetsParams datasetsParam = new ListDatasetsParams();
    datasetsParam.setIds(req.getDatasetIds());
    datasetsParam.setSpaceId(req.getSpaceId());
    datasetsParam.setCategory(req.getCategory());
    datasetsParam.setCreatedBys(req.getCreatedBys());
    datasetsParam.setNameLike(req.getName());
    datasetsParam.setPaginator(pg);
    datasetsParam.setBizCategorys(req.getBizCategorys());
    datasetsParam.setCatalogItemId(req.getCatalogItemId() == null ? null : Long.parseLong(req.getCatalogItemId()));
    PageInfo<Dataset> datasetPageInfo = repo.listDatasets(datasetsParam);
    List<Dataset> datasets = datasetPageInfo.getList();
    List<Long> schemaIDs = datasets.stream().map(Dataset::getSchemaId).toList();
    List<DatasetSchema> schemas = repo.mGetSchema(req.getSpaceId(), schemaIDs);
    Map<Long, DatasetSchema> schemaM = schemas.stream().collect(Collectors.toMap(DatasetSchema::getId, o -> o));
    return datasetPageInfo.convert(o -> {
      DatasetWithSchema datasetWithSchema = new DatasetWithSchema();
      datasetWithSchema.setDataset(o);
      datasetWithSchema.setSchema(schemaM.get(o.getSchemaId()));
      return datasetWithSchema;
    });
  }

  private void buildNewDataset(Dataset dataset) {
    DatasetStatus status = dataset.getStatus();
    if (DatasetStatus.UNKNOWN == status) {
      dataset.setStatus(DatasetStatus.AVAILABLE);
    }
    DatasetVisibility visibility = dataset.getVisibility();
    if (DatasetVisibility.UNKNOWN == visibility) {
      dataset.setVisibility(DatasetVisibility.SPACE);
    }
    SecurityLevel securityLevel = dataset.getSecurityLevel();
    if (SecurityLevel.UNKNOWN == securityLevel) {
      dataset.setSecurityLevel(SecurityLevel.L2);
    }
    DatasetFeatures features = dataset.getFeatures();
    if (features == null) {
      DatasetFeatures datasetFeatures = config.getDatasetFeature().getFeatureByCategory().get(dataset.getCategory());
      dataset.setFeatures(datasetFeatures);
    }
    DatasetSpec spec = dataset.getSpec();
    if (spec == null) {
      DatasetSpec datasetSpec = config.getDatasetSpec().getSpecsByCategory().get(dataset.getCategory());
      dataset.setSpec(datasetSpec);
    }
  }
}
