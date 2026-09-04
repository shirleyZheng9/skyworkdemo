package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptItemResultEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptItemResultRunLogEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemRunLogFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Page;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.IExptItemResultDAO;
import com.iwhalecloud.bote.mapper.loop.evaluation.ExptItemResultMapper;
import com.iwhalecloud.bote.mapper.loop.evaluation.ExptItemResultRunLogMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

/**
 * 实验数据项结果DAO实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_item_result.go
 *
 * @author Generated
 * @since 2025-01-27
 */
@Repository
@RequiredArgsConstructor
public class ExptItemResultDAOImpl implements IExptItemResultDAO {
  private final ExptItemResultMapper exptItemResultMapper;
  private final ExptItemResultRunLogMapper exptItemResultRunLogMapper;

  /**
   * 批量获取实验数据项结果
   * 迁移对应关系: Go语言exptItemResultDAOImpl.BatchGet
   */
  @Override
  public List<ExptItemResultEntity> batchGet(Long spaceId, Long exptId, List<Long> itemIds) {
    try {
      return exptItemResultMapper.batchGet(spaceId, exptId, itemIds);
    }
    catch (Exception e) {
      throw new BssException("批量获取实验数据项结果失败: " + e.getMessage(), e);
    }
  }

  /**
   * 批量创建实验数据项结果（如果不存在）
   * 迁移对应关系: Go语言exptItemResultDAOImpl.BatchCreateNx
   */
  @Override
  public void batchCreateNx(List<ExptItemResultEntity> itemResults) {
    try {
      exptItemResultMapper.batchCreateNx(itemResults);
    }
    catch (Exception e) {
      throw new BssException("批量创建实验数据项结果失败: " + e.getMessage(), e);
    }
  }

  /**
   * 扫描实验数据项结果
   * 迁移对应关系: Go语言exptItemResultDAOImpl.ScanItemResults
   */
  @Override
  public List<ExptItemResultEntity> scanItemResults(Long exptId, Long cursor, Long limit, List<Integer> status, Long spaceId) {
    try {
      return exptItemResultMapper.scanItemResults(exptId, cursor, limit, status, spaceId);
    }
    catch (Exception e) {
      throw new BssException("扫描实验数据项结果失败: " + e.getMessage(), e);
    }
  }

  /**
   * 根据实验ID获取数据项ID列表
   * 迁移对应关系: Go语言exptItemResultDAOImpl.GetItemIdListByExptID
   */
  @Override
  public List<Long> getItemIdListByExptId(Long exptId, Long spaceId) {
    try {
      return exptItemResultMapper.getItemIdListByExptId(exptId, spaceId);
    }
    catch (Exception e) {
      throw new BssException("根据实验ID获取数据项ID列表失败: " + e.getMessage(), e);
    }
  }

  /**
   * 根据实验ID分页查询数据项结果
   * 迁移对应关系: Go语言exptItemResultDAOImpl.ListItemResultsByExptID
   */
  @Override
  public PageInfo<ExptItemResultEntity> listItemResultsByExptId(Long exptId, Long spaceId, Page page, Boolean desc) {
    RowBounds rowBounds = new RowBounds(page.getOffset(), page.getLimit());
    return exptItemResultMapper.listItemResultsByExptId(exptId, spaceId, desc, rowBounds).toPageInfo();
  }

  /**
   * 保存实验数据项结果
   * 迁移对应关系: Go语言exptItemResultDAOImpl.SaveItemResults
   * 使用先查询再决定插入或更新的方式，兼容所有数据库
   */
  @Override
  public void saveItemResults(List<ExptItemResultEntity> itemResults) {
    try {
      if (itemResults == null || itemResults.isEmpty()) {
        return;
      }

      for (ExptItemResultEntity itemResult : itemResults) {
        // 检查记录是否存在
        boolean exists = exptItemResultMapper.existsByKeys(
          itemResult.getSpaceId(),
          itemResult.getExptId(),
          itemResult.getExptRunId(),
          itemResult.getItemId()
        );

        if (exists) {
          // 记录存在，执行更新（保持原有条件）
          exptItemResultMapper.updateItemResult(itemResult);
        }
        else {
          // 记录不存在，执行插入
          exptItemResultMapper.insertItemResult(itemResult);
        }
      }
    }
    catch (Exception e) {
      throw new BssException("保存实验数据项结果失败: " + e.getMessage(), e);
    }
  }

  /**
   * 获取数据项轮次结果
   * 迁移对应关系: Go语言exptItemResultDAOImpl.GetItemTurnResults
   */
  @Override
  public List<ExptTurnResultEntity> getItemTurnResults(Long spaceId, Long exptId, Long itemId) {
    try {
      return exptItemResultMapper.getItemTurnResults(spaceId, exptId, itemId);
    }
    catch (Exception e) {
      throw new BssException("获取数据项轮次结果失败: " + e.getMessage(), e);
    }
  }

  /**
   * 更新数据项结果
   * 迁移对应关系: Go语言exptItemResultDAOImpl.UpdateItemsResult
   */
  @Override
  public void updateItemsResult(Long spaceId, Long exptId, List<Long> itemIds, Map<String, Object> ufields) {
    try {
      exptItemResultMapper.updateItemsResult(spaceId, exptId, itemIds, ufields);
    }
    catch (Exception e) {
      throw new BssException("更新数据项结果失败: " + e.getMessage(), e);
    }
  }

  /**
   * 根据实验ID获取最大数据项序号
   * 迁移对应关系: Go语言exptItemResultDAOImpl.GetMaxItemIdxByExptID
   */
  @Override
  public Integer getMaxItemIdxByExptId(Long exptId, Long spaceId) {
    try {
      return exptItemResultMapper.getMaxItemIdxByExptId(exptId, spaceId);
    }
    catch (Exception e) {
      throw new BssException("根据实验ID获取最大数据项序号失败: " + e.getMessage(), e);
    }
  }

  /**
   * 批量创建实验数据项运行日志（如果不存在）
   * 迁移对应关系: Go语言exptItemResultDAOImpl.BatchCreateNxRunLogs
   */
  @Override
  public void batchCreateNxRunLogs(List<ExptItemResultRunLogEntity> itemRunLogs) {
    try {
      if (itemRunLogs == null || itemRunLogs.isEmpty()) {
        return;
      }

      for (ExptItemResultRunLogEntity itemRunLog : itemRunLogs) {
        // 检查记录是否存在
        boolean exists = exptItemResultRunLogMapper.existsByKeys(
          itemRunLog.getSpaceId(),
          itemRunLog.getExptId(),
          itemRunLog.getExptRunId(),
          itemRunLog.getItemId()
        );

        if (exists) {
          // 记录存在，执行更新（保持原有条件）
          exptItemResultRunLogMapper.updateItemRunLogByKeys(itemRunLog);
        }
        else {
          // 记录不存在，执行插入
          exptItemResultRunLogMapper.insertItemRunLog(itemRunLog);
        }
      }
    }
    catch (Exception e) {
      throw new BssException("批量创建实验数据项运行日志失败: " + e.getMessage(), e);
    }
  }

  /**
   * 扫描实验数据项运行日志
   * 迁移对应关系: Go语言exptItemResultDAOImpl.ScanItemRunLogs
   */
  @Override
  public List<ExptItemResultRunLogEntity> scanItemRunLogs(Long exptId, Long exptRunId, ExptItemRunLogFilter filter, Long cursor, Long limit, Long spaceId) {
    try {
      return exptItemResultRunLogMapper.scanItemRunLogs(exptId, exptRunId, filter, cursor, limit, spaceId);
    }
    catch (Exception e) {
      throw new BssException("扫描实验数据项运行日志失败: " + e.getMessage(), e);
    }
  }

  /**
   * 更新数据项运行日志
   * 迁移对应关系: Go语言exptItemResultDAOImpl.UpdateItemRunLog
   */
  @Override
  public void updateItemRunLog(Long exptId, Long exptRunId, List<Long> itemIds, Map<String, Object> ufields, Long spaceId) {
    try {
      exptItemResultRunLogMapper.updateItemRunLog(exptId, exptRunId, itemIds, ufields, spaceId);
    }
    catch (Exception e) {
      throw new BssException("更新数据项运行日志失败: " + e.getMessage(), e);
    }
  }

  /**
   * 获取数据项运行日志
   * 迁移对应关系: Go语言exptItemResultDAOImpl.GetItemRunLog
   */
  @Override
  public ExptItemResultRunLogEntity getItemRunLog(Long exptId, Long exptRunId, Long itemId, Long spaceId) {
    try {
      return exptItemResultRunLogMapper.getItemRunLog(exptId, exptRunId, itemId, spaceId);
    }
    catch (Exception e) {
      throw new BssException("获取数据项运行日志失败: " + e.getMessage(), e);
    }
  }

  /**
   * 批量获取数据项运行日志
   * 迁移对应关系: Go语言exptItemResultDAOImpl.MGetItemRunLog
   */
  @Override
  public List<ExptItemResultRunLogEntity> mGetItemRunLog(Long exptId, Long exptRunId, List<Long> itemIds, Long spaceId) {
    try {
      return exptItemResultRunLogMapper.mGetItemRunLog(exptId, exptRunId, itemIds, spaceId);
    }
    catch (Exception e) {
      throw new BssException("批量获取数据项运行日志失败: " + e.getMessage(), e);
    }
  }
}
