package com.iwhalecloud.bote.loop.data.domain.dataset.repo;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.loop.data.domain.dataset.entity.ItemSnapshot;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListItemSnapshotsParams;
import java.util.List;

/**
 * 项目快照仓库接口
 * 迁移对应关系: Go语言repo.IItemSnapshotRepo
 * - 功能: 提供项目快照数据访问接口
 * - 方法定义: 各种项目快照数据操作方法
 * <p>
 * Java实现说明:
 * - 对应Go的repo.IItemSnapshotRepo接口
 * - 使用Java接口定义，包含项目快照数据访问方法
 * - 提供项目快照数据CRUD操作
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
public interface IItemSnapshotRepo {

  /**
   * 批量插入或更新项目快照
   * 迁移对应关系: Go语言repo.IItemSnapshotRepo.BatchUpsertItemSnapshots
   * - 功能: 批量插入或更新项目快照
   * - 参数: snapshots - 项目快照列表
   * - 返回: 处理数量
   * - 用途: 批量插入或更新项目快照
   */
  Long batchUpsertItemSnapshots(List<ItemSnapshot> snapshots);

  /**
   * 列表项目快照
   * 迁移对应关系: Go语言repo.IItemSnapshotRepo.ListItemSnapshots
   * - 功能: 列表项目快照
   * - 参数: params - 列表参数
   * - 返回: 项目快照列表
   * - 用途: 列表项目快照
   */
  PageInfo<ItemSnapshot> listItemSnapshots(ListItemSnapshotsParams params);

  /**
   * 统计项目快照数量
   * 迁移对应关系: Go语言repo.IItemSnapshotRepo.CountItemSnapshots
   * - 功能: 统计项目快照数量
   * - 参数: params - 列表参数
   * - 返回: 数量
   * - 用途: 统计项目快照数量
   */
  Long countItemSnapshots(ListItemSnapshotsParams params);
}
