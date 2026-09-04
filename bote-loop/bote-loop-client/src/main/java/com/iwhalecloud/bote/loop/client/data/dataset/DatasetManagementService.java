package com.iwhalecloud.bote.loop.client.data.dataset;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetsResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.CreateDatasetRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.CreateDatasetResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.DeleteDatasetRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.DeleteDatasetResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetResponse;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetDTO;

/**
 * 数据集管理服务接口
 * 对应Go: dataset_app.go
 */
public interface DatasetManagementService {

  /**
   * 新增数据集
   * 对应Thrift方法: CreateDataset
   */
  CreateDatasetResponse createDataset(CreateDatasetRequest request);

  /**
   * 修改数据集
   * 对应Thrift方法: UpdateDataset
   */
  UpdateDatasetResponse updateDataset(UpdateDatasetRequest request);

  /**
   * 删除数据集
   * 对应Thrift方法: DeleteDataset
   */
  DeleteDatasetResponse deleteDataset(DeleteDatasetRequest request);

  /**
   * 获取数据集列表
   * 对应Thrift方法: ListDatasets
   */
  PageInfo<DatasetDTO> listDatasets(ListDatasetsRequest request);

  /**
   * 数据集当前信息（不包括数据）
   * 对应Thrift方法: GetDataset
   */
  GetDatasetResponse getDataset(GetDatasetRequest request);

  /**
   * 批量获取数据集
   * 对应Thrift方法: BatchGetDatasets
   */
  BatchGetDatasetsResponse batchGetDatasets(BatchGetDatasetsRequest request);
}
