package com.iwhalecloud.bote.mapper.loop.data.dataset;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetItemEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListItemsParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 数据集项目Mapper接口
 * 迁移对应关系: Go语言ItemMapper
 * - 功能: 提供数据集项目数据访问接口
 * - 方法定义: 各种数据集项目数据操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的ItemMapper接口
 * - 使用MyBatis注解定义，包含数据集项目数据访问方法
 * - 提供数据集项目数据CRUD操作
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface ItemMapper {

  /**
   * 根据条件统计项目数量
   * 迁移对应关系: Go语言ItemMapper.CountByCondition
   * - 功能: 根据条件统计项目数量
   * - 参数: params - 查询参数
   * - 返回: 项目数量
   * - 用途: 条件统计项目数量
   */
  Long countByCondition(ListItemsParams params);

  /**
   * 批量插入项目
   * 迁移对应关系: Go语言ItemMapper.BatchInsertItems
   * - 功能: 批量插入项目
   * - 参数: items - 项目列表
   * - 返回: 影响行数
   * - 用途: 批量插入项目
   */
  int batchInsertItems(List<DatasetItemEntity> items);

  /**
   * 更新项目
   * 迁移对应关系: Go语言ItemMapper.UpdateItem
   * - 功能: 更新项目
   * - 参数: item - 项目对象
   * - 返回: 影响行数
   * - 用途: 更新项目
   */
  int updateItem(DatasetItemEntity item);

  /**
   * 根据条件查询项目
   * 迁移对应关系: Go语言ItemMapper.SelectByCondition
   * - 功能: 根据条件查询项目
   * - 参数: params - 查询参数
   * - 返回: 项目列表
   * - 用途: 条件查询项目
   */
  List<DatasetItemEntity> selectByCondition(ListItemsParams params);
  Page<DatasetItemEntity> selectByCondition(ListItemsParams params, RowBounds rowBounds);

  /**
   * 根据spaceId、id、addVn、delVn更新项目
   * 迁移对应关系: Go语言ItemMapper.UpdateBySpaceIdAndIdAndAddVnAndDelVn
   * - 功能: 根据spaceId、id、addVn、delVn更新项目
   * - 参数: item - 项目对象
   * - 用途: 更新项目
   */
  void updateBySpaceIdAndIdAndAddVnAndDelVn(DatasetItemEntity item);

  /**
   * 根据spaceId和ids删除项目
   * 迁移对应关系: Go语言ItemMapper.DeleteBySpaceIdAndIds
   * - 功能: 根据spaceId和ids删除项目
   * - 参数: spaceId - 空间ID, ids - 项目ID列表, deletedAt - 删除时间戳
   * - 用途: 删除项目
   */
  void deleteBySpaceIdAndIds(@Param("spaceId") Long spaceId, @Param("ids") List<Long> ids, @Param("deletedAt") Long deletedAt);

  /**
   * 根据spaceId和ids更新delVn
   * 迁移对应关系: Go语言ItemMapper.UpdateDelVnBySpaceIdAndIds
   * - 功能: 根据spaceId和ids更新delVn
   * - 参数: spaceId - 空间ID, delVN - 删除版本号, ids - 项目ID列表
   * - 用途: 归档项目
   */
  void updateDelVnBySpaceIdAndIds(@Param("spaceId") Long spaceId, @Param("delVN") Long delVN, @Param("ids") List<Long> ids);

  /**
   * 根据spaceId、datasetId和delVn查询项目用于清空
   * 迁移对应关系: Go语言ItemMapper.SelectBySpaceIdAndDatasetIdAndDelVnForClear
   * - 功能: 根据spaceId、datasetId和delVn查询项目用于清空
   * - 参数: spaceId - 空间ID, datasetId - 数据集ID, delVN - 删除版本号, rowBounds - 分页参数
   * - 返回: 项目列表
   * - 用途: 清空数据集时查询项目
   */
  List<DatasetItemEntity> selectBySpaceIdAndDatasetIdAndDelVnForClear(
    @Param("spaceId") Long spaceId,
    @Param("datasetId") Long datasetId,
    @Param("delVN") Long delVN,
    RowBounds rowBounds);
}
