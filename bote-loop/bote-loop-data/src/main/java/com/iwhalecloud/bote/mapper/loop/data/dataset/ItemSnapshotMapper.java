package com.iwhalecloud.bote.mapper.loop.data.dataset;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetItemSnapshotEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListItemSnapshotsParams;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * 条目快照MyBatis Mapper接口
 * 迁移对应关系: Go语言IItemSnapshotDAO
 * - 功能: 提供条目快照数据访问接口
 * - 方法定义: 各种条目快照数据操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的IItemSnapshotDAO接口
 * - 使用MyBatis注解和XML映射
 * - 提供条目快照数据CRUD操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java MyBatis接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */

public interface ItemSnapshotMapper {

  /**
   * 批量插入条目快照
   * 迁移对应关系: Go语言IItemSnapshotDAO.BatchUpsertItemSnapshots
   * - 功能: 批量插入条目快照
   * - 参数: snapshots - 快照列表
   * - 返回: 插入数量
   * - 用途: 批量插入快照
   */
  Long batchUpsertItemSnapshots(List<DatasetItemSnapshotEntity> snapshots);

  /**
   * 列表查询条目快照
   * 迁移对应关系: Go语言IItemSnapshotDAO.ListItemSnapshots
   * - 功能: 列表查询条目快照
   * - 参数: params - 查询参数
   * - 返回: 快照列表
   * - 用途: 分页查询快照
   */
  List<DatasetItemSnapshotEntity> listItemSnapshots(ListItemSnapshotsParams params);
  Page<DatasetItemSnapshotEntity> listItemSnapshots(ListItemSnapshotsParams params, RowBounds rowBounds);

  /**
   * 统计条目快照数量
   * 迁移对应关系: Go语言IItemSnapshotDAO.CountItemSnapshots
   * - 功能: 统计条目快照数量
   * - 参数: params - 查询参数
   * - 返回: 快照数量
   * - 用途: 统计快照数量
   */
  Long countItemSnapshots(ListItemSnapshotsParams params);
}
