package com.iwhalecloud.bote.loop.data.domain.dataset.repo;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Dataset;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.FieldSchema;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListDatasetsParams;
import java.util.List;
import java.util.Map;

/**
 * 数据集仓库接口
 * 迁移对应关系: Go语言repo.IDatasetRepo
 * - 功能: 提供数据集核心数据访问接口
 * - 方法定义: 各种数据集核心数据操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的repo.IDatasetRepo接口
 * - 使用Java接口定义，包含数据集核心数据访问方法
 * - 提供数据集核心数据CRUD操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface IDatasetRepo {

  /**
   * 获取项目数量
   * 迁移对应关系: Go语言repo.IDatasetRepo.GetItemCount
   * - 功能: 获取项目数量
   * - 参数: datasetID - 数据集ID
   * - 返回: 项目数量
   * - 用途: 获取数据集项目数量
   */
  Long getItemCount(Long datasetID);

  /**
   * 批量获取项目数量
   * 迁移对应关系: Go语言repo.IDatasetRepo.MGetItemCount
   * - 功能: 批量获取项目数量
   * - 参数: datasetIDs - 数据集ID列表
   * - 返回: 数据集ID到项目数量的映射
   * - 用途: 批量获取数据集项目数量
   */
  Map<Long, Long> mGetItemCount(List<Long> datasetIDs);

  /**
   * 设置项目数量
   * 迁移对应关系: Go语言repo.IDatasetRepo.SetItemCount
   * - 功能: 设置项目数量
   * - 参数: datasetID - 数据集ID, n - 数量
   * - 用途: 设置数据集项目数量
   */
  void setItemCount(Long datasetID, Long n);

  /**
   * 增加项目数量
   * 迁移对应关系: Go语言repo.IDatasetRepo.IncrItemCount
   * - 功能: 增加项目数量
   * - 参数: datasetID - 数据集ID, n - 增加数量
   * - 返回: 新数量
   * - 用途: 增加数据集项目数量
   */
  Long incrItemCount(Long datasetID, Long n);

  /**
   * 创建数据集和模式
   * 迁移对应关系: Go语言repo.IDatasetRepo.CreateDatasetAndSchema
   * - 功能: 创建数据集和模式
   * - 参数: dataset - 数据集, fields - 字段列表
   * - 用途: 创建数据集和模式
   */
  void createDatasetAndSchema(Dataset dataset, List<FieldSchema> fields);

  /**
   * 获取数据集
   * 迁移对应关系: Go语言repo.IDatasetRepo.GetDataset
   * - 功能: 获取数据集
   * - 参数: spaceID - 空间ID, id - 数据集ID
   * - 返回: 数据集
   * - 用途: 获取单个数据集
   */
  Dataset getDataset(Long spaceID, Long id);

  /**
   * 批量获取数据集
   * 迁移对应关系: Go语言repo.IDatasetRepo.MGetDatasets
   * - 功能: 批量获取数据集
   * - 参数: spaceID - 空间ID, ids - 数据集ID列表
   * - 返回: 数据集列表
   * - 用途: 批量获取数据集
   */
  List<Dataset> mGetDatasets(Long spaceID, List<Long> ids);

  /**
   * 更新数据集
   * 迁移对应关系: Go语言repo.IDatasetRepo.PatchDataset
   * - 功能: 更新数据集
   * - 参数: patch - 更新数据, where - 条件
   * - 用途: 更新数据集
   */
  void patchDataset(Dataset patch, Dataset where);

  /**
   * 删除数据集
   * 迁移对应关系: Go语言repo.IDatasetRepo.DeleteDataset
   * - 功能: 删除数据集
   * - 参数: spaceID - 空间ID, id - 数据集ID
   * - 用途: 删除数据集
   */
  void deleteDataset(Long spaceID, Long id);

  /**
   * 列表数据集
   * 迁移对应关系: Go语言repo.IDatasetRepo.ListDatasets
   * - 功能: 列表数据集
   * - 参数: params - 列表参数
   * - 返回: 数据集列表
   * - 用途: 列表数据集
   */
  PageInfo<Dataset> listDatasets(ListDatasetsParams params);

  /**
   * 统计数据集数量
   * 迁移对应关系: Go语言repo.IDatasetRepo.CountDatasets
   * - 功能: 统计数据集数量
   * - 参数: params - 列表参数
   * - 返回: 数量
   * - 用途: 统计数据集数量
   */
  Long countDatasets(ListDatasetsParams params);
}
