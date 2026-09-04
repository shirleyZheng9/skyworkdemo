package com.iwhalecloud.bote.loop.data.application;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.client.data.dataset.DatasetApplicationService;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchCreateDatasetItemsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchCreateDatasetItemsResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchDeleteDatasetItemsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchDeleteDatasetItemsResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetItemsByVersionRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetItemsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetVersionsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetVersionsResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetsResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ClearDatasetItemRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ClearDatasetItemResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.CreateDatasetRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.CreateDatasetResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.CreateDatasetVersionRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.CreateDatasetVersionResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.DeleteDatasetItemRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.DeleteDatasetItemResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.DeleteDatasetRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.DeleteDatasetResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetIOJobRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetIOJobResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetItemRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetItemResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetSchemaRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetSchemaResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetVersionRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetVersionResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ImportDatasetRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ImportDatasetResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetIOJobsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetIOJobsResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetItemsByVersionRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetItemsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetVersionsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetItemRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetItemResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetSchemaRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetSchemaResponse;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetItemDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetVersionDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DatasetApplicationServiceImpl implements DatasetApplicationService {

  private final DatasetItemServiceImpl datasetItemService;
  private final DatasetJobServiceImpl datasetJobService;
  private final DatasetManagementServiceImpl datasetManagementService;
  private final DatasetSchemaServiceImpl datasetSchemaService;
  private final DatasetVersionServiceImpl datasetVersionService;

  @Override
  public BatchCreateDatasetItemsResponse batchCreateDatasetItems(BatchCreateDatasetItemsRequest request) {
    return datasetItemService.batchCreateDatasetItems(request);
  }

  @Override
  public UpdateDatasetItemResponse updateDatasetItem(UpdateDatasetItemRequest request) {
    return datasetItemService.updateDatasetItem(request);
  }

  @Override
  public DeleteDatasetItemResponse deleteDatasetItem(DeleteDatasetItemRequest request) {
    return datasetItemService.deleteDatasetItem(request);
  }

  @Override
  public BatchDeleteDatasetItemsResponse batchDeleteDatasetItems(BatchDeleteDatasetItemsRequest request) {
    return datasetItemService.batchDeleteDatasetItems(request);
  }

  @Override
  public PageInfo<DatasetItemDTO> listDatasetItems(ListDatasetItemsRequest request) {
    return datasetItemService.listDatasetItems(request);
  }

  @Override
  public PageInfo<DatasetItemDTO> listDatasetItemsByVersion(ListDatasetItemsByVersionRequest request) {
    return datasetItemService.listDatasetItemsByVersion(request);
  }

  @Override
  public GetDatasetItemResponse getDatasetItem(GetDatasetItemRequest request) {
    return datasetItemService.getDatasetItem(request);
  }

  @Override
  public PageInfo<DatasetItemDTO> batchGetDatasetItems(BatchGetDatasetItemsRequest request) {
    return datasetItemService.batchGetDatasetItems(request);
  }

  @Override
  public PageInfo<DatasetItemDTO> batchGetDatasetItemsByVersion(BatchGetDatasetItemsByVersionRequest request) {
    return datasetItemService.batchGetDatasetItemsByVersion(request);
  }

  @Override
  public ClearDatasetItemResponse clearDatasetItem(ClearDatasetItemRequest request) {
    return datasetItemService.clearDatasetItem(request);
  }

  @Override
  public ImportDatasetResponse importDataset(ImportDatasetRequest request) {
    return datasetJobService.importDataset(request);
  }

  @Override
  public GetDatasetIOJobResponse getDatasetIOJob(GetDatasetIOJobRequest request) {
    return datasetJobService.getDatasetIOJob(request);
  }

  @Override
  public ListDatasetIOJobsResponse listDatasetIOJobs(ListDatasetIOJobsRequest request) {
    return datasetJobService.listDatasetIOJobs(request);
  }

  @Override
  public CreateDatasetResponse createDataset(CreateDatasetRequest request) {
    return datasetManagementService.createDataset(request);
  }

  @Override
  public UpdateDatasetResponse updateDataset(UpdateDatasetRequest request) {
    return datasetManagementService.updateDataset(request);
  }

  @Override
  public DeleteDatasetResponse deleteDataset(DeleteDatasetRequest request) {
    return datasetManagementService.deleteDataset(request);
  }

  @Override
  public PageInfo<DatasetDTO> listDatasets(ListDatasetsRequest request) {
    return datasetManagementService.listDatasets(request);
  }

  @Override
  public GetDatasetResponse getDataset(GetDatasetRequest request) {
    return datasetManagementService.getDataset(request);
  }

  @Override
  public BatchGetDatasetsResponse batchGetDatasets(BatchGetDatasetsRequest request) {
    return datasetManagementService.batchGetDatasets(request);
  }

  @Override
  public GetDatasetSchemaResponse getDatasetSchema(GetDatasetSchemaRequest request) {
    return datasetSchemaService.getDatasetSchema(request);
  }

  @Override
  public UpdateDatasetSchemaResponse updateDatasetSchema(UpdateDatasetSchemaRequest request) {
    return datasetSchemaService.updateDatasetSchema(request);
  }

  @Override
  public CreateDatasetVersionResponse createDatasetVersion(CreateDatasetVersionRequest request) {
    return datasetVersionService.createDatasetVersion(request);
  }

  @Override
  public PageInfo<DatasetVersionDTO> listDatasetVersions(ListDatasetVersionsRequest request) {
    return datasetVersionService.listDatasetVersions(request);
  }

  @Override
  public GetDatasetVersionResponse getDatasetVersion(GetDatasetVersionRequest request) {
    return datasetVersionService.getDatasetVersion(request);
  }

  @Override
  public BatchGetDatasetVersionsResponse batchGetDatasetVersions(BatchGetDatasetVersionsRequest request) {
    return datasetVersionService.batchGetDatasetVersions(request);
  }
}
