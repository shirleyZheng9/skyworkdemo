package com.iwhalecloud.bote.loop.client.data.dataset;

import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetIOJobRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetIOJobResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ImportDatasetRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ImportDatasetResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetIOJobsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetIOJobsResponse;

/**
 * 任务管理服务接口
 * 对应Go: job_app.go
 */
public interface DatasetJobService {

  /**
   * 导入数据
   * 对应Thrift方法: ImportDataset
   */
  ImportDatasetResponse importDataset(ImportDatasetRequest request);

  /**
   * 任务(导入、导出、转换)详情
   * 对应Thrift方法: GetDatasetIOJob
   */
  GetDatasetIOJobResponse getDatasetIOJob(GetDatasetIOJobRequest request);

  /**
   * 数据集任务列表
   * 对应Thrift方法: ListDatasetIOJobs
   */
  ListDatasetIOJobsResponse listDatasetIOJobs(ListDatasetIOJobsRequest request);
}
