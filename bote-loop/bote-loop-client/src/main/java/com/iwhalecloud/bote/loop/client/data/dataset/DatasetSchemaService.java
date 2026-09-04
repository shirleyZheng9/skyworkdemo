package com.iwhalecloud.bote.loop.client.data.dataset;

import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetSchemaRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetSchemaResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetSchemaRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetSchemaResponse;

/**
 * 模式管理服务接口
 * 对应Go: schema_app.go
 */
public interface DatasetSchemaService {

  /**
   * 获取数据集当前的 schema
   * 对应Thrift方法: GetDatasetSchema
   */
  GetDatasetSchemaResponse getDatasetSchema(GetDatasetSchemaRequest request);

  /**
   * 覆盖更新 schema
   * 对应Thrift方法: UpdateDatasetSchema
   */
  UpdateDatasetSchemaResponse updateDatasetSchema(UpdateDatasetSchemaRequest request);
}
