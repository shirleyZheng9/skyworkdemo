package com.iwhalecloud.bote.loop.client.data.dataset;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchCreateDatasetItemsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchCreateDatasetItemsResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchDeleteDatasetItemsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchDeleteDatasetItemsResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetItemsByVersionRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.BatchGetDatasetItemsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ClearDatasetItemRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ClearDatasetItemResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.DeleteDatasetItemRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.DeleteDatasetItemResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetItemRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.GetDatasetItemResponse;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetItemsByVersionRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.ListDatasetItemsRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetItemRequest;
import com.iwhalecloud.bote.loop.client.data.dataset.dto.UpdateDatasetItemResponse;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.DatasetItemDTO;

/**
 * 数据项管理服务接口
 * 对应Go: item_app.go
 */
public interface DatasetItemService {

  /**
   * 批量新增数据
   * 对应Thrift方法: BatchCreateDatasetItems
   */
  BatchCreateDatasetItemsResponse batchCreateDatasetItems(BatchCreateDatasetItemsRequest request);

  /**
   * 更新数据
   * 对应Thrift方法: UpdateDatasetItem
   */
  UpdateDatasetItemResponse updateDatasetItem(UpdateDatasetItemRequest request);

  /**
   * 删除数据
   * 对应Thrift方法: DeleteDatasetItem
   */
  DeleteDatasetItemResponse deleteDatasetItem(DeleteDatasetItemRequest request);

  /**
   * 批量删除数据
   * 对应Thrift方法: BatchDeleteDatasetItems
   */
  BatchDeleteDatasetItemsResponse batchDeleteDatasetItems(BatchDeleteDatasetItemsRequest request);

  /**
   * 分页查询当前数据
   * 对应Thrift方法: ListDatasetItems
   */
  PageInfo<DatasetItemDTO> listDatasetItems(ListDatasetItemsRequest request);

  /**
   * 分页查询指定版本的数据
   * 对应Thrift方法: ListDatasetItemsByVersion
   */
  PageInfo<DatasetItemDTO> listDatasetItemsByVersion(ListDatasetItemsByVersionRequest request);

  /**
   * 获取指定数据
   * 对应Thrift方法: GetDatasetItem
   */
  GetDatasetItemResponse getDatasetItem(GetDatasetItemRequest request);

  /**
   * 批量获取数据
   * 对应Thrift方法: BatchGetDatasetItems
   */
  PageInfo<DatasetItemDTO> batchGetDatasetItems(BatchGetDatasetItemsRequest request);

  /**
   * 批量获取指定版本的数据
   * 对应Thrift方法: BatchGetDatasetItemsByVersion
   */
  PageInfo<DatasetItemDTO> batchGetDatasetItemsByVersion(BatchGetDatasetItemsByVersionRequest request);

  /**
   * 清空数据
   * 对应Thrift方法: ClearDatasetItem
   */
  ClearDatasetItemResponse clearDatasetItem(ClearDatasetItemRequest request);
}
