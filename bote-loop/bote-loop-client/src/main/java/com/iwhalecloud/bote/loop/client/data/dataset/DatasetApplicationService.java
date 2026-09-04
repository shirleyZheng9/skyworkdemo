package com.iwhalecloud.bote.loop.client.data.dataset;

/**
 * 统一数据集服务接口
 * 组合所有数据集相关的服务接口
 * 对应Go: DatasetApplicationImpl 的完整功能
 */
public interface DatasetApplicationService extends DatasetManagementService,
  DatasetItemService,
  DatasetVersionService,
  DatasetSchemaService,
  DatasetJobService {

}
