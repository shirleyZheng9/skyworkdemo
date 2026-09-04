package com.iwhalecloud.bote.mapper.loop.data.dataset;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListDatasetsParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 数据集MyBatis Mapper接口
 * 迁移对应关系: Go语言IDatasetDAO
 * - 功能: 提供数据集数据访问接口
 * - 方法定义: 各种数据集数据操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的IDatasetDAO接口
 * - 使用MyBatis注解和XML映射
 * - 提供数据集数据CRUD操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java MyBatis接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface DatasetMapper {

  /**
   * 创建数据集
   * 迁移对应关系: Go语言IDatasetDAO.CreateDataset
   * - 功能: 创建数据集
   * - 参数: dataset - 数据集对象
   * - 用途: 创建新数据集
   */
  void createDataset(DatasetEntity dataset);

  /**
   * 更新数据集
   * 迁移对应关系: Go语言IDatasetDAO.PatchDataset
   * - 功能: 更新数据集
   * - 参数: patch - 更新数据, where - 条件
   * - 用途: 更新数据集
   */
  void patchDataset(@Param("patch") DatasetEntity patch, @Param("where") DatasetEntity where);

  /**
   * 删除数据集
   * 迁移对应关系: Go语言IDatasetDAO.DeleteDataset
   * - 功能: 删除数据集
   * - 参数: spaceId - 空间ID, datasetId - 数据集ID, deletedAt - 删除时间戳
   * - 用途: 软删除数据集
   */
  void deleteDataset(@Param("spaceId") Long spaceId, @Param("datasetId") Long datasetId, @Param("deletedAt") Long deletedAt);

  /**
   * 获取数据集
   * 迁移对应关系: Go语言IDatasetDAO.GetDataset
   * - 功能: 获取数据集
   * - 参数: spaceId - 空间ID, datasetId - 数据集ID
   * - 返回: 数据集对象
   * - 用途: 获取单个数据集
   */
  DatasetEntity getDataset(@Param("spaceId") Long spaceId, @Param("datasetId") Long datasetId);

  /**
   * 批量获取数据集
   * 迁移对应关系: Go语言IDatasetDAO.MGetDatasets
   * - 功能: 批量获取数据集
   * - 参数: spaceId - 空间ID, ids - 数据集ID列表
   * - 返回: 数据集列表
   * - 用途: 批量获取数据集
   */
  List<DatasetEntity> mGetDatasets(@Param("spaceId") Long spaceId, @Param("ids") List<Long> ids);

  /**
   * 列表查询数据集
   * 迁移对应关系: Go语言IDatasetDAO.ListDatasets
   * - 功能: 列表查询数据集
   * - 参数: params - 查询参数, rowBounds - 分页参数
   * - 返回: 数据集分页列表
   * - 用途: 分页查询数据集
   */
  Page<DatasetEntity> listDatasets(ListDatasetsParams params, RowBounds rowBounds);

  /**
   * 统计数据集数量
   * 迁移对应关系: Go语言IDatasetDAO.CountDatasets
   * - 功能: 统计数据集数量
   * - 参数: params - 查询参数
   * - 返回: 数据集数量
   * - 用途: 统计数据集数量
   */
  Long countDatasets(ListDatasetsParams params);
}
