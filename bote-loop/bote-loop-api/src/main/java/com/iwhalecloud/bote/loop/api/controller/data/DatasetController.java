package com.iwhalecloud.bote.loop.api.controller.data;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
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
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetVersionsResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetsResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetItemRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetItemResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetSchemaRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetSchemaResponse;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetItemDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetVersionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据集控制器
 * 对应Thrift: DatasetService
 */
@RestController
@RequestMapping("/api/data/v1")
@RequiredArgsConstructor
@IgnoreSession
@IgnoreSign
public class DatasetController {
  private final DatasetApplicationService datasetApplicationService;

  /**
   * 创建数据集
   */
  @PostMapping("/datasets")
  public CreateDatasetResponse createDataset(@RequestBody CreateDatasetRequest request) {
    return datasetApplicationService.createDataset(request);
  }

  /**
   * 批量获取数据集
   */
  @PostMapping("/datasets/batch_get")
  public BatchGetDatasetsResponse batchGetDatasets(@RequestBody BatchGetDatasetsRequest request) {
    return datasetApplicationService.batchGetDatasets(request);
  }

  /**
   * 删除数据集
   */
  @DeleteMapping("/datasets/{dataset_id}")
  public DeleteDatasetResponse deleteDataset(
    @PathVariable("dataset_id") Long datasetId,
    @RequestBody DeleteDatasetRequest request) {
    request.setDatasetId(datasetId);
    return datasetApplicationService.deleteDataset(request);
  }

  /**
   * 导入数据集
   */
  @PostMapping("/datasets/{dataset_id}/import")
  public ImportDatasetResponse importDataset(
    @PathVariable("dataset_id") Long datasetId,
    @RequestBody ImportDatasetRequest request) {
    request.setDatasetId(datasetId);
    return datasetApplicationService.importDataset(request);
  }

  /**
   * 列表数据集IO任务
   */
  @PostMapping("/datasets/{dataset_id}/io_jobs")
  public ListDatasetIOJobsResponse listDatasetIOJobs(
    @PathVariable("dataset_id") Long datasetId,
    @RequestBody ListDatasetIOJobsRequest request) {
    request.setDatasetId(datasetId);
    return datasetApplicationService.listDatasetIOJobs(request);
  }

  /**
   * 获取数据集字段
   */
  @GetMapping("/datasets/{dataset_id}/schema")
  public GetDatasetSchemaResponse getDatasetSchema(
    @PathVariable("dataset_id") Long datasetId,
    GetDatasetSchemaRequest request) {
    request.setDatasetId(datasetId);
    return datasetApplicationService.getDatasetSchema(request);
  }

  /**
   * 更新数据集字段
   */
  @PutMapping("/datasets/{dataset_id}/schema")
  public UpdateDatasetSchemaResponse updateDatasetSchema(
    @PathVariable("dataset_id") Long datasetId,
    @RequestBody UpdateDatasetSchemaRequest request) {
    request.setDatasetId(datasetId);
    return datasetApplicationService.updateDatasetSchema(request);
  }

  /**
   * 创建数据集版本
   */
  @PostMapping("/datasets/{dataset_id}/versions")
  public CreateDatasetVersionResponse createDatasetVersion(
    @PathVariable("dataset_id") Long datasetId,
    @RequestBody CreateDatasetVersionRequest request) {
    request.setDatasetId(datasetId);
    return datasetApplicationService.createDatasetVersion(request);
  }

  /**
   * 列表数据集版本
   */
  @PostMapping("/datasets/{dataset_id}/versions/list")
  public ListDatasetVersionsResponse listDatasetVersions(
    @PathVariable("dataset_id") Long datasetId,
    @RequestBody ListDatasetVersionsRequest request) {
    request.setDatasetId(datasetId);
    PageInfo<DatasetVersionDTO> pageInfo = datasetApplicationService.listDatasetVersions(request);
    ListDatasetVersionsResponse response = new ListDatasetVersionsResponse();
    response.setVersions(pageInfo.getList());
    response.setTotal(pageInfo.getTotal());
    return response;
  }

  /**
   * 批量获取数据集版本数据
   */
  @PostMapping("/datasets/{dataset_id}/versions/{version_id}/items/batch_get")
  public PageInfo<DatasetItemDTO> batchGetDatasetItemsByVersion(
    @PathVariable("dataset_id") Long datasetId,
    @PathVariable("version_id") Long versionId,
    @RequestBody BatchGetDatasetItemsByVersionRequest request) {
    request.setDatasetId(datasetId);
    request.setVersionId(versionId);
    return datasetApplicationService.batchGetDatasetItemsByVersion(request);
  }

  /**
   * 列表数据集版本数据
   */
  @PostMapping("/datasets/{dataset_id}/versions/{version_id}/items/list")
  public PageInfo<DatasetItemDTO> listDatasetItemsByVersion(
    @PathVariable("dataset_id") Long datasetId,
    @PathVariable("version_id") Long versionId,
    @RequestBody ListDatasetItemsByVersionRequest request) {
    request.setDatasetId(datasetId);
    request.setVersionId(versionId);
    return datasetApplicationService.listDatasetItemsByVersion(request);
  }

  /**
   * 批量创建数据集数据
   */
  @PostMapping("/datasets/{dataset_id}/items/batch_create")
  public BatchCreateDatasetItemsResponse batchCreateDatasetItems(
    @PathVariable("dataset_id") Long datasetId,
    @RequestBody BatchCreateDatasetItemsRequest request) {
    request.setDatasetId(datasetId);
    return datasetApplicationService.batchCreateDatasetItems(request);
  }

  /**
   * 批量删除数据集数据
   */
  @PostMapping("/datasets/{dataset_id}/items/batch_delete")
  public BatchDeleteDatasetItemsResponse batchDeleteDatasetItems(
    @PathVariable("dataset_id") Long datasetId,
    @RequestBody BatchDeleteDatasetItemsRequest request) {
    request.setDatasetId(datasetId);
    return datasetApplicationService.batchDeleteDatasetItems(request);
  }

  /**
   * 批量获取数据集数据
   */
  @PostMapping("/datasets/{dataset_id}/items/batch_get")
  public PageInfo<DatasetItemDTO> batchGetDatasetItems(
    @PathVariable("dataset_id") Long datasetId,
    @RequestBody BatchGetDatasetItemsRequest request) {
    request.setDatasetId(datasetId);
    return datasetApplicationService.batchGetDatasetItems(request);
  }

  /**
   * 清空数据集数据
   */
  @PostMapping("/datasets/{dataset_id}/items/clear")
  public ClearDatasetItemResponse clearDatasetItem(
    @PathVariable("dataset_id") Long datasetId,
    @RequestBody ClearDatasetItemRequest request) {
    request.setDatasetId(datasetId);
    return datasetApplicationService.clearDatasetItem(request);
  }

  /**
   * 删除数据集数据
   */
  @DeleteMapping("/datasets/{dataset_id}/items/{item_id}")
  public DeleteDatasetItemResponse deleteDatasetItem(
    @PathVariable("dataset_id") Long datasetId,
    @PathVariable("item_id") Long itemId,
    @RequestBody DeleteDatasetItemRequest request) {
    request.setDatasetId(datasetId);
    request.setItemId(itemId);
    return datasetApplicationService.deleteDatasetItem(request);
  }

  /**
   * 获取数据集数据
   */
  @GetMapping("/datasets/{dataset_id}/items/{item_id}")
  public GetDatasetItemResponse getDatasetItem(
    @PathVariable("dataset_id") Long datasetId,
    @PathVariable("item_id") Long itemId,
    GetDatasetItemRequest request) {
    request.setDatasetId(datasetId);
    request.setItemId(itemId);
    return datasetApplicationService.getDatasetItem(request);
  }

  /**
   * 更新数据集数据
   */
  @PutMapping("/datasets/{dataset_id}/items/{item_id}")
  public UpdateDatasetItemResponse updateDatasetItem(
    @PathVariable("dataset_id") Long datasetId,
    @PathVariable("item_id") Long itemId,
    @RequestBody UpdateDatasetItemRequest request) {
    request.setDatasetId(datasetId);
    request.setItemId(itemId);
    return datasetApplicationService.updateDatasetItem(request);
  }

  /**
   * 列表数据集数据
   */
  @PostMapping("/datasets/{dataset_id}/items/list")
  public PageInfo<DatasetItemDTO> listDatasetItems(
    @PathVariable("dataset_id") Long datasetId,
    @RequestBody ListDatasetItemsRequest request) {
    request.setDatasetId(datasetId);
    return datasetApplicationService.listDatasetItems(request);
  }

  /**
   * 获取数据集
   */
  @GetMapping("/datasets/{dataset_id}")
  public GetDatasetResponse getDataset(
    @PathVariable("dataset_id") Long datasetId,
    GetDatasetRequest request) {
    request.setDatasetId(datasetId);
    return datasetApplicationService.getDataset(request);
  }

  /**
   * 更新数据集
   */
  @PatchMapping("/datasets/{dataset_id}")
  public UpdateDatasetResponse updateDataset(
    @PathVariable("dataset_id") Long datasetId,
    @RequestBody UpdateDatasetRequest request) {
    request.setDatasetId(datasetId);
    return datasetApplicationService.updateDataset(request);
  }

  /**
   * 列表数据集
   */
  @PostMapping("/datasets/list")
  public ListDatasetsResponse listDatasets(@RequestBody ListDatasetsRequest request) {
    PageInfo<DatasetDTO> pageInfo = datasetApplicationService.listDatasets(request);
    ListDatasetsResponse response = new ListDatasetsResponse();
    response.setDatasets(pageInfo.getList());
    response.setTotal(pageInfo.getTotal());
    return response;
  }

  /**
   * 获取数据集IO任务
   */
  @GetMapping("/dataset_io_jobs/{job_id}")
  public GetDatasetIOJobResponse getDatasetIOJob(
    @PathVariable("job_id") Long jobId,
    GetDatasetIOJobRequest request) {
    request.setJobId(jobId);
    return datasetApplicationService.getDatasetIOJob(request);
  }

  /**
   * 批量获取数据集版本
   */
  @PostMapping("/dataset_versions/batch_get")
  public BatchGetDatasetVersionsResponse batchGetDatasetVersions(
    @RequestBody BatchGetDatasetVersionsRequest request) {
    return datasetApplicationService.batchGetDatasetVersions(request);
  }

  /**
   * 获取数据集版本
   */
  @GetMapping("/dataset_versions/{version_id}")
  public GetDatasetVersionResponse getDatasetVersion(
    @PathVariable("version_id") Long versionId,
    GetDatasetVersionRequest request) {
    request.setVersionId(versionId);
    return datasetApplicationService.getDatasetVersion(request);
  }
}
