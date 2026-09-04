package com.iwhalecloud.bote.loop.client.data.dataset;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetVersionsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetVersionsResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.CreateDatasetVersionRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.CreateDatasetVersionResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetVersionRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetVersionResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetVersionsRequest;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetVersionDTO;

/**
 * 版本管理服务接口
 * 对应Go: version_app.go
 */
public interface DatasetVersionService {

  /**
   * 生成一个新版本
   * 对应Thrift方法: CreateDatasetVersion
   */
  CreateDatasetVersionResponse createDatasetVersion(CreateDatasetVersionRequest request);

  /**
   * 版本列表
   * 对应Thrift方法: ListDatasetVersions
   */
  PageInfo<DatasetVersionDTO> listDatasetVersions(ListDatasetVersionsRequest request);

  /**
   * 获取指定版本的数据集详情
   * 对应Thrift方法: GetDatasetVersion
   */
  GetDatasetVersionResponse getDatasetVersion(GetDatasetVersionRequest request);

  /**
   * 批量获取指定版本的数据集详情
   * 对应Thrift方法: BatchGetDatasetVersions
   */
  BatchGetDatasetVersionsResponse batchGetDatasetVersions(BatchGetDatasetVersionsRequest request);
}
