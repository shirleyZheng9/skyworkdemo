package com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.entity.loop.data.dataset.DatasetItemSnapshotEntity;
import com.iwhalecloud.bote.loop.data.domain.dataset.repo.dto.ListItemSnapshotsParams;
import com.iwhalecloud.bote.loop.data.infra.repo.dataset.mysql.ItemSnapshotDAO;
import com.iwhalecloud.bote.loop.data.pkg.pagination.Paginator;
import com.iwhalecloud.bote.mapper.loop.data.dataset.ItemSnapshotMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

/**
 * 条目快照DAO实现类
 * 迁移对应关系: Go语言ItemSnapshotDAOImpl
 * - 功能: 实现条目快照数据访问
 * - 方法实现: 各种条目快照数据操作方法实现
 * <p>
 * Java实现说明:
 * - 对应Go的ItemSnapshotDAOImpl结构体
 * - 使用MyBatis实现数据库操作
 * - 提供条目快照数据CRUD操作实现
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis
 * - Go上下文 -> Java上下文
 * - Go错误处理 -> Java异常处理
 */
@Repository
@RequiredArgsConstructor
public class ItemSnapshotDAOImpl implements ItemSnapshotDAO {
  private final ItemSnapshotMapper itemSnapshotMapper;

  /**
   * 批量插入条目快照
   */
  @Override
  public Long batchUpsertItemSnapshots(List<DatasetItemSnapshotEntity> snapshots) {
    if (CollectionUtils.isEmpty(snapshots)) {
      return 0L;
    }
    return itemSnapshotMapper.batchUpsertItemSnapshots(snapshots);
  }

  /**
   * 列表查询条目快照
   */
  @Override
  public PageInfo<DatasetItemSnapshotEntity> listItemSnapshots(ListItemSnapshotsParams params) {
    if (params == null) {
      throw new BssException("params is required");
    }
    Paginator paginator = params.getPaginator();
    if (paginator != null && paginator.getPageNum() != null && paginator.getPageSize() != null) {
      RowBounds rowBounds = paginator.buildRowBounds();
      return itemSnapshotMapper.listItemSnapshots(params, rowBounds).toPageInfo();
    }
    return new PageInfo<>(itemSnapshotMapper.listItemSnapshots(params));
  }

  /**
   * 统计条目快照数量
   */
  @Override
  public Long countItemSnapshots(ListItemSnapshotsParams params) {
    if (params == null) {
      throw new BssException("params is required");
    }

    try {
      Long count = itemSnapshotMapper.countItemSnapshots(params);
      return count != null ? count : 0L;
    }
    catch (Exception e) {
      throw new BssException("count item snapshots failed", e);
    }
  }
}
