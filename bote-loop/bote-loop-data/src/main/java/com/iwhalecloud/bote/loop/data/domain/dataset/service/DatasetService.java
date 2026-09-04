package com.iwhalecloud.bote.loop.data.domain.dataset.service;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Dataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.DatasetWithSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.SearchDatasetsParam;
import com.iwhalecloud.bote.loop.data.domain.dataset.service.dto.UpdateDatasetParam;
import java.util.List;

/**
 * 数据集核心服务接口
 * 迁移对应关系: Go语言service.IDatasetService
 * - 功能: 提供数据集核心业务逻辑服务
 * - 方法定义: 各种数据集核心操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的service.IDatasetService接口
 * - 使用Java接口定义，包含数据集核心服务方法
 * - 提供数据集核心CRUD和业务逻辑操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface DatasetService {

  /**
   * 创建数据集
   * 迁移对应关系: Go语言service.IDatasetService.CreateDataset
   * - 功能: 创建数据集
   * - 参数: dataset - 数据集, fields - 字段列表
   * - 用途: 创建新数据集
   */
  void createDataset(Dataset dataset, List<FieldSchema> fields);

  /**
   * 更新数据集
   * 迁移对应关系: Go语言service.IDatasetService.UpdateDataset
   * - 功能: 更新数据集
   * - 参数: param - 更新参数
   * - 用途: 更新数据集
   */
  void updateDataset(UpdateDatasetParam param);

  /**
   * 删除数据集
   * 迁移对应关系: Go语言service.IDatasetService.DeleteDataset
   * - 功能: 删除数据集
   * - 参数: spaceId - 空间ID, id - 数据集ID
   * - 用途: 删除数据集
   */
  void deleteDataset(Long spaceId, Long id);

  /**
   * 获取数据集
   * 迁移对应关系: Go语言service.IDatasetService.GetDataset
   * - 功能: 获取数据集
   * - 参数: spaceId - 空间ID, id - 数据集ID
   * - 返回: 数据集和模式
   * - 用途: 获取数据集信息
   */
  DatasetWithSchema getDataset(Long spaceId, Long id);

  /**
   * 批量获取数据集
   * 迁移对应关系: Go语言service.IDatasetService.BatchGetDataset
   * - 功能: 批量获取数据集
   * - 参数: spaceId - 空间ID, ids - 数据集ID列表
   * - 返回: 数据集和模式列表
   * - 用途: 批量获取数据集信息
   */
  List<DatasetWithSchema> batchGetDataset(Long spaceId, List<Long> ids);

  /**
   * 获取数据集（带选项）
   * 迁移对应关系: Go语言service.IDatasetService.GetDatasetWithOpt
   * - 功能: 获取数据集（带选项）
   * - 参数: spaceId - 空间ID, id - 数据集ID, opt - 获取选项
   * - 返回: 数据集和模式
   * - 用途: 获取数据集信息（带选项）
   */
  DatasetWithSchema getDatasetWithOpt(Long spaceId, Long id, Boolean withDeleted);

  /**
   * 批量获取数据集（带选项）
   * 迁移对应关系: Go语言service.IDatasetService.BatchGetDatasetWithOpt
   * - 功能: 批量获取数据集（带选项）
   * - 参数: spaceId - 空间ID, ids - 数据集ID列表, opt - 获取选项
   * - 返回: 数据集和模式列表
   * - 用途: 批量获取数据集信息（带选项）
   */
  List<DatasetWithSchema> batchGetDatasetWithOpt(Long spaceId, List<Long> ids, Boolean withDeleted);

  /**
   * 搜索数据集
   * 迁移对应关系: Go语言service.IDatasetService.SearchDataset
   * - 功能: 搜索数据集
   * - 参数: req - 搜索参数
   * - 返回: 搜索结果
   * - 用途: 搜索数据集
   */
  PageInfo<DatasetWithSchema> searchDataset(SearchDatasetsParam req);
}
