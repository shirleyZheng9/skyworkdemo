package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.client.data.dataset.DatasetApplicationService;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchCreateDatasetItemsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchCreateDatasetItemsResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchDeleteDatasetItemsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetItemsByVersionRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetItemsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetVersionsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetVersionsResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetsResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ClearDatasetItemRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.CreateDatasetRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.CreateDatasetResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.CreateDatasetVersionRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.CreateDatasetVersionResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.DeleteDatasetRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetVersionRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetVersionResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetItemsByVersionRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetItemsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetVersionsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetItemRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetSchemaRequest;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetCategoryDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetFeaturesDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetItemDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetSpecDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetVersionDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetVisibilityDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldDataDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldSchemaDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.VersionedDatasetDTO;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.IDatasetRPCAdapter;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.convertor.DatasetRpcConvertor;
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
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetSchema;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetVersion;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetVersionResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldSchema;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Turn;
import lombok.AllArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class DatasetRPCAdapterImpl implements IDatasetRPCAdapter {

  private final DatasetApplicationService datasetApplicationService;

  @Override
  public Long createDataset(CreateDatasetParam param) {
    List<FieldSchemaDTO> fields = Lists.newArrayList();
    EvaluationSetSchema evaluationSetItems = param.getEvaluationSetItems();
    if (evaluationSetItems != null) {
      List<FieldSchema> schemas = evaluationSetItems.getFieldSchemas();
      fields = DatasetRpcConvertor.convert2DatasetFieldSchemas(schemas);
    }

    CreateDatasetRequest req = new CreateDatasetRequest();
    req.setWorkspaceId(param.getSpaceId());
    req.setName(param.getName());
    req.setAppId(param.getEvaluationSetItems().getAppId());
    req.setDescription(param.getDesc());
    req.setCategory(DatasetCategoryDTO.EVALUATION);
    req.setBizCategory(param.getBizCategory().toString());
    req.setVisibility(DatasetVisibilityDTO.SPACE);
    req.setFields(fields);
    req.setCatalogItemId(param.getCatalogItemId());

    DatasetSpecDTO specDTO = new DatasetSpecDTO();
    specDTO.setMaxFieldCount(50);
    specDTO.setMaxItemSize(204800L);
    specDTO.setMaxItemCount(5000L);
    req.setSpec(specDTO);

    DatasetFeaturesDTO featuresDTO = new DatasetFeaturesDTO();
    featuresDTO.setEditSchema(true);
    req.setFeatures(featuresDTO);
    req.setCatalogItemId(param.getCatalogItemId());

    CreateDatasetResponse resp = datasetApplicationService.createDataset(req);

    return resp.getDatasetId();
  }

  @Override
  public void updateDataset(Long spaceId, Long evaluationSetId, String name, String desc, Long catalogItemId) {
    UpdateDatasetRequest req = new UpdateDatasetRequest();
    req.setWorkspaceId(spaceId);
    req.setDatasetId(evaluationSetId);
    req.setName(name);
    req.setDescription(desc);
    req.setCatalogItemId(catalogItemId);

    datasetApplicationService.updateDataset(req);
  }

  @Override
  public void deleteDataset(Long spaceId, Long evaluationSetId) {
    DeleteDatasetRequest req = new DeleteDatasetRequest();
    req.setWorkspaceId(spaceId);
    req.setDatasetId(evaluationSetId);

    datasetApplicationService.deleteDataset(req);
  }

  @Override
  public EvaluationSet getDataset(Long spaceId, Long evaluationSetId, Boolean deletedAt) {
    GetDatasetRequest req = new GetDatasetRequest();
    req.setWorkspaceId(spaceId);
    req.setDatasetId(evaluationSetId);
    req.setWithDeleted(deletedAt != null ? deletedAt : false);

    GetDatasetResponse resp = datasetApplicationService.getDataset(req);
    if (resp == null || resp.getDataset() == null) {
      return null;
    }
    return DatasetRpcConvertor.convert2EvaluationSet(resp.getDataset());
  }

  @Override
  public List<EvaluationSet> batchGetDatasets(Long spaceId, List<Long> evaluationSetIds, Boolean deletedAt) {
    BatchGetDatasetsRequest req = new BatchGetDatasetsRequest();
    req.setWorkspaceId(spaceId);
    req.setDatasetIds(evaluationSetIds);
    req.setWithDeleted(deletedAt);

    BatchGetDatasetsResponse resp = datasetApplicationService.batchGetDatasets(req);
    return DatasetRpcConvertor.convert2EvaluationSets(resp.getDatasets());
  }

  @Override
  public PageInfo<EvaluationSet> listDatasets(ListDatasetsParam param) {
    ListDatasetsRequest req = new ListDatasetsRequest();
    req.setWorkspaceId(param.getSpaceId());
    req.setDatasetIds(param.getEvaluationSetIds());
    req.setName(param.getName());
    req.setCreatedBys(param.getCreators());
    req.setPageNumber(param.getPageNumber());
    req.setPageSize(param.getPageSize());
    req.setPageToken(param.getPageToken());
    req.setOrderBys(DatasetRpcConvertor.convert2DatasetOrderBys(param.getOrderBys()));
    req.setCategory(DatasetCategoryDTO.EVALUATION);
    req.setCatalogItemId(param.getCatalogItemId());

    PageInfo<DatasetDTO> pageInfo = datasetApplicationService.listDatasets(req);
    return pageInfo.convert(DatasetRpcConvertor::convert2EvaluationSet);
  }

  @Override
  public Long createDatasetVersion(Long spaceId, Long evaluationSetId, String version, String desc) {
    CreateDatasetVersionRequest req = new CreateDatasetVersionRequest();
    req.setWorkspaceId(spaceId);
    req.setDatasetId(evaluationSetId);
    req.setVersion(version);
    req.setDesc(desc);

    CreateDatasetVersionResponse resp = datasetApplicationService.createDatasetVersion(req);
    return resp.getId();
  }

  @Override
  public EvaluationSetVersionResult getDatasetVersion(Long spaceId, Long versionId, Boolean deletedAt) {
    GetDatasetVersionRequest req = new GetDatasetVersionRequest();
    req.setWorkspaceId(spaceId);
    req.setVersionId(versionId);
    req.setWithDeleted(deletedAt);

    GetDatasetVersionResponse resp = datasetApplicationService.getDatasetVersion(req);

    EvaluationSetVersion version = DatasetRpcConvertor.convert2EvaluationSetVersion(resp.getVersion(), resp.getDataset());
    EvaluationSet set = DatasetRpcConvertor.convert2EvaluationSet(resp.getDataset());

    // 数据集返回的dataset结构体中version的值是草稿版本的值，这里需要替换一下
    if (set != null) {
      set.setEvaluationSetVersion(version);
    }

    EvaluationSetVersionResult result = new EvaluationSetVersionResult();
    result.setVersion(version);
    result.setEvaluationSet(set);
    return result;
  }

  @Override
  public List<BatchGetVersionedDatasetsResult> batchGetVersionedDatasets(Long spaceId, List<Long> versionIds, Boolean deletedAt) {
    BatchGetDatasetVersionsRequest req = new BatchGetDatasetVersionsRequest();
    req.setWorkspaceId(spaceId);
    req.setVersionIds(versionIds);
    req.setWithDeleted(deletedAt);

    BatchGetDatasetVersionsResponse resp = datasetApplicationService.batchGetDatasetVersions(req);

    List<BatchGetVersionedDatasetsResult> results = Lists.newArrayList();
    for (VersionedDatasetDTO v : resp.getVersionedDataset()) {
      EvaluationSetVersion version = DatasetRpcConvertor.convert2EvaluationSetVersion(v.getVersion(), v.getDataset());
      EvaluationSet set = DatasetRpcConvertor.convert2EvaluationSet(v.getDataset());

      // 数据集返回的dataset结构体中version的值是草稿版本的值，这里需要替换一下
      if (set != null) {
        set.setEvaluationSetVersion(version);
      }

      BatchGetVersionedDatasetsResult result = new BatchGetVersionedDatasetsResult();
      result.setEvaluationSet(set);
      result.setVersion(version);
      results.add(result);
    }

    return results;
  }

  @Override
  public PageInfo<EvaluationSetVersion> listDatasetVersions(Long spaceId, Long evaluationSetId, String pageToken, Integer pageNumber, Integer pageSize, String versionLike) {
    ListDatasetVersionsRequest req = new ListDatasetVersionsRequest();
    req.setWorkspaceId(spaceId);
    req.setDatasetId(evaluationSetId);
    req.setPageToken(pageToken);
    req.setPageSize(pageSize);
    req.setPageNumber(pageNumber);
    req.setVersionLike(versionLike);

    PageInfo<DatasetVersionDTO> pageInfo = datasetApplicationService.listDatasetVersions(req);
    return pageInfo.convert(DatasetRpcConvertor::convert2EvaluationSetVersion);
  }

  @Override
  public void updateDatasetSchema(Long spaceId, Long evaluationSetId, List<FieldSchema> schemas) {
    List<FieldSchemaDTO> fieldSchemas = DatasetRpcConvertor.convert2DatasetFieldSchemas(schemas);

    UpdateDatasetSchemaRequest req = new UpdateDatasetSchemaRequest();
    req.setWorkspaceId(spaceId);
    req.setDatasetId(evaluationSetId);
    req.setFields(fieldSchemas);

    datasetApplicationService.updateDatasetSchema(req);
  }

  @Override
  public BatchCreateEvaluationSetItemsResult batchCreateDatasetItems(BatchCreateDatasetItemsParam param) {
    List<DatasetItemDTO> datasetItems = DatasetRpcConvertor.convert2DatasetItems(param.getItems());

    BatchCreateDatasetItemsRequest req = new BatchCreateDatasetItemsRequest();
    req.setWorkspaceId(param.getSpaceId());
    req.setDatasetId(param.getEvaluationSetId());
    req.setItems(datasetItems);
    req.setSkipInvalidItems(param.getSkipInvalidItems());
    req.setAllowPartialAdd(param.getAllowPartialAdd());

    BatchCreateDatasetItemsResponse resp = datasetApplicationService.batchCreateDatasetItems(req);

    BatchCreateEvaluationSetItemsResult result = new BatchCreateEvaluationSetItemsResult();
    result.setIdMap(resp.getAddedItems());
    result.setErrors(DatasetRpcConvertor.convert2EvaluationSetErrorGroups(resp.getErrors()));
    return result;
  }

  @Override
  public void updateDatasetItem(Long spaceId, Long evaluationSetId, Long itemId, List<Turn> turns) {
    List<FieldDataDTO> data = DatasetRpcConvertor.convert2DatasetData(turns);

    UpdateDatasetItemRequest req = new UpdateDatasetItemRequest();
    req.setWorkspaceId(spaceId);
    req.setDatasetId(evaluationSetId);
    req.setItemId(itemId);
    req.setData(data);

    datasetApplicationService.updateDatasetItem(req);
  }

  @Override
  public void batchDeleteDatasetItems(Long spaceId, Long evaluationSetId, List<Long> itemIds) {
    BatchDeleteDatasetItemsRequest req = new BatchDeleteDatasetItemsRequest();
    req.setWorkspaceId(spaceId);
    req.setDatasetId(evaluationSetId);
    req.setItemIds(itemIds);

    datasetApplicationService.batchDeleteDatasetItems(req);
  }

  @Override
  public PageInfo<EvaluationSetItem> listDatasetItems(ListDatasetItemsParam param) {
    ListDatasetItemsRequest req = new ListDatasetItemsRequest();
    req.setWorkspaceId(param.getSpaceId());
    req.setDatasetId(param.getEvaluationSetId());
    req.setPageNumber(param.getPageNumber());
    req.setPageSize(param.getPageSize());
    req.setOrderBys(DatasetRpcConvertor.convert2DatasetOrderBys(param.getOrderBys()));
    // todo
    // req.setItemIdsNotIn(param.getItemIdsNotIn());

    PageInfo<DatasetItemDTO> resp = datasetApplicationService.listDatasetItems(req);
    return resp.convert(DatasetRpcConvertor::convert2EvaluationSetItem);
  }

  @Override
  public PageInfo<EvaluationSetItem> listDatasetItemsByVersion(ListDatasetItemsParam param) {
    ListDatasetItemsByVersionRequest req = new ListDatasetItemsByVersionRequest();
    req.setWorkspaceId(param.getSpaceId());
    req.setDatasetId(param.getEvaluationSetId());
    req.setVersionId(param.getVersionId());
    req.setPageNumber(param.getPageNumber());
    req.setPageSize(param.getPageSize());
    req.setOrderBys(DatasetRpcConvertor.convert2DatasetOrderBys(param.getOrderBys()));

    PageInfo<DatasetItemDTO> resp = datasetApplicationService.listDatasetItemsByVersion(req);
    return resp.convert(DatasetRpcConvertor::convert2EvaluationSetItem);
  }

  @Override
  public PageInfo<EvaluationSetItem> batchGetDatasetItems(BatchGetDatasetItemsParam param) {
    BatchGetDatasetItemsRequest req = new BatchGetDatasetItemsRequest();
    req.setWorkspaceId(param.getSpaceId());
    req.setDatasetId(param.getEvaluationSetId());
    req.setItemIds(param.getItemIds());

    PageInfo<DatasetItemDTO> resp = datasetApplicationService.batchGetDatasetItems(req);
    return resp.convert(DatasetRpcConvertor::convert2EvaluationSetItem);
  }

  @Override
  public PageInfo<EvaluationSetItem> batchGetDatasetItemsByVersion(BatchGetDatasetItemsParam param) {
    BatchGetDatasetItemsByVersionRequest req = new BatchGetDatasetItemsByVersionRequest();
    req.setWorkspaceId(param.getSpaceId());
    req.setDatasetId(param.getEvaluationSetId());
    req.setItemIds(param.getItemIds());
    req.setVersionId(param.getVersionId());
    PageInfo<DatasetItemDTO> resp = datasetApplicationService.batchGetDatasetItemsByVersion(req);
    return resp.convert(DatasetRpcConvertor::convert2EvaluationSetItem);
  }

  @Override
  public void clearEvaluationSetDraftItem(Long spaceId, Long evaluationSetId) {
    ClearDatasetItemRequest req = new ClearDatasetItemRequest();
    req.setWorkspaceId(spaceId);
    req.setDatasetId(evaluationSetId);

    datasetApplicationService.clearDatasetItem(req);
  }

  @Override
  public ItemSnapshotMappingsResult queryItemSnapshotMappings(Long spaceId, Long datasetId, Long versionId) {
    // 根据Go代码，这个方法直接返回null
    return null;
  }
}
