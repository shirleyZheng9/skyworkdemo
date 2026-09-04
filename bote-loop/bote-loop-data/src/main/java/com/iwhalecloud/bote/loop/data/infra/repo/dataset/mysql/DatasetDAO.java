package com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListDatasetsParams;
import java.util.List;

/**
 * 数据集DAO接口
 * 迁移对应关系: Go语言IDatasetDAO
 * - 功能: 提供数据集数据访问接口
 * - 方法定义: 各种数据集数据操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的IDatasetDAO接口
 * - 使用Java接口定义，包含数据集数据访问方法
 * - 提供数据集数据CRUD操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface DatasetDAO {

  /**
   * 创建数据集
   * 迁移对应关系: Go语言IDatasetDAO.CreateDataset
   * - 功能: 创建数据集
   * - 参数: dataset - 数据集对象, options - 选项
   * - 用途: 创建新数据集
   */
  void createDataset(DatasetEntity dataset);

  /**
   * 更新数据集
   * 迁移对应关系: Go语言IDatasetDAO.PatchDataset
   * - 功能: 更新数据集
   * - 参数: patch - 更新数据, where - 条件, options - 选项
   * - 用途: 更新数据集
   */
  void patchDataset(DatasetEntity patch, DatasetEntity where);

  /**
   * 删除数据集
   * 迁移对应关系: Go语言IDatasetDAO.DeleteDataset
   * - 功能: 删除数据集
   * - 参数: spaceId - 空间ID, datasetId - 数据集ID, options - 选项
   * - 用途: 软删除数据集
   */
  void deleteDataset(Long spaceId, Long datasetId);

  /**
   * 获取数据集
   * 迁移对应关系: Go语言IDatasetDAO.GetDataset
   * - 功能: 获取数据集
   * - 参数: spaceId - 空间ID, datasetId - 数据集ID, options - 选项
   * - 返回: 数据集对象
   * - 用途: 获取单个数据集
   */
  DatasetEntity getDataset(Long spaceId, Long datasetId);

  /**
   * 批量获取数据集
   * 迁移对应关系: Go语言IDatasetDAO.MGetDatasets
   * - 功能: 批量获取数据集
   * - 参数: spaceId - 空间ID, ids - 数据集ID列表, options - 选项
   * - 返回: 数据集列表
   * - 用途: 批量获取数据集
   */
  List<DatasetEntity> mGetDatasets(Long spaceId, List<Long> ids);

  /**
   * 列表查询数据集
   * 迁移对应关系: Go语言IDatasetDAO.ListDatasets
   * - 功能: 列表查询数据集
   * - 参数: params - 查询参数, options - 选项
   * - 返回: 数据集列表和分页结果
   * - 用途: 分页查询数据集
   */
  PageInfo<DatasetEntity> listDatasets(ListDatasetsParams params);

  /**
   * 统计数据集数量
   * 迁移对应关系: Go语言IDatasetDAO.CountDatasets
   * - 功能: 统计数据集数量
   * - 参数: params - 查询参数, options - 选项
   * - 返回: 数据集数量
   * - 用途: 统计数据集数量
   */
  Long countDatasets(ListDatasetsParams params);
}
