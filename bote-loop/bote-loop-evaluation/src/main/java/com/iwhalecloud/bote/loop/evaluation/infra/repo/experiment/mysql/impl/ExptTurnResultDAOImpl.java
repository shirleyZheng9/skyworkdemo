package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnEvaluatorResultRefEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultRunLogEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemTurnID;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Page;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.ExptTurnResultDAO;
import com.iwhalecloud.bote.mapper.loop.evaluation.ExptTurnResultMapper;
import com.iwhalecloud.bote.mapper.loop.evaluation.ExptTurnResultRunLogMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

/**
 * 实验轮次结果DAO实现类
 *
 * @author Generated
 * @since 2025-01-27
 */
@Repository
@RequiredArgsConstructor
public class ExptTurnResultDAOImpl implements ExptTurnResultDAO {
  private final ExptTurnResultMapper exptTurnResultMapper;
  private final ExptTurnResultRunLogMapper exptTurnResultRunLogMapper;

  // ========== 轮次结果相关方法实现 ==========

  /**
   * 分页查询轮次结果
   */
  @Override
  public List<ExptTurnResultEntity> listTurnResult(Long spaceId, Long exptId, ExptTurnResultFilter filter, Page page, Boolean desc) {
    RowBounds rowBounds = new RowBounds(page.getOffset(), page.getLimit());
    return exptTurnResultMapper.listTurnResult(spaceId, exptId, filter, desc, rowBounds);
  }

  /**
   * 根据数据项ID列表分页查询轮次结果
   */
  @Override
  public List<ExptTurnResultEntity> listTurnResultByItemIds(Long spaceId, Long exptId, List<Long> itemIds, ExptTurnResultFilter filter, Page page, Boolean desc) {
    RowBounds rowBounds = new RowBounds(page.getOffset(), page.getLimit());
    return exptTurnResultMapper.listTurnResultByItemIds(spaceId, exptId, itemIds, filter, desc, rowBounds);
  }

  /**
   * 批量获取轮次结果
   */
  @Override
  public List<ExptTurnResultEntity> batchGet(Long spaceId, Long exptId, List<Long> itemIds) {
    return exptTurnResultMapper.batchGet(spaceId, exptId, itemIds);
  }

  /**
   * 创建轮次评估器引用
   */
  @Override
  public void createTurnEvaluatorRefs(List<ExptTurnEvaluatorResultRefEntity> refs) {
    if (refs == null || refs.isEmpty()) {
      return;
    }
    exptTurnResultMapper.createTurnEvaluatorRefs(refs);
  }

  /**
   * 批量创建轮次结果（如果不存在）
   */
  @Override
  public void batchCreateNx(List<ExptTurnResultEntity> turnResults) {
    if (turnResults == null || turnResults.isEmpty()) {
      return;
    }
    exptTurnResultMapper.batchCreateNx(turnResults);
  }

  /**
   * 获取数据项轮次结果
   */
  @Override
  public List<ExptTurnResultEntity> getItemTurnResults(Long exptId, Long itemId, Long spaceId) {
    return getItemTurnResults(exptId, itemId, spaceId, null);
  }

  @Override
  public List<ExptTurnResultEntity> getItemTurnResults(Long exptId, Long itemId, Long spaceId, Long exptRunId) {
    return exptTurnResultMapper.getItemTurnResults(exptId, itemId, spaceId, exptRunId);
  }

  /**
   * 保存轮次结果
   */
  @Override
  public void saveTurnResults(List<ExptTurnResultEntity> turnResults) {
    if (turnResults == null || turnResults.isEmpty()) {
      return;
    }
    for (ExptTurnResultEntity result : turnResults) {
      exptTurnResultMapper.saveTurnResult(result);
    }
  }

  /**
   * 扫描轮次结果
   */
  @Override
  public PageInfo<ExptTurnResultEntity> scanTurnResults(Long exptId, List<Integer> status, int pageNumber, Long pageSize, Long spaceId) {
    RowBounds rowBounds = new RowBounds((pageNumber - 1) * pageSize.intValue(), pageSize.intValue());
    return exptTurnResultMapper.scanTurnResults(exptId, status, spaceId, rowBounds).toPageInfo();
  }

  /**
   * 更新轮次结果
   */
  @Override
  public void updateTurnResults(Long exptId, List<ItemTurnID> itemTurnIds, Long spaceId, Map<String, Object> ufields) {
    if (itemTurnIds == null || itemTurnIds.isEmpty() || ufields == null || ufields.isEmpty()) {
      return;
    }
    exptTurnResultMapper.updateTurnResults(exptId, itemTurnIds, spaceId, ufields);
  }

  /**
   * 根据数据项ID更新轮次结果
   */
  @Override
  public void updateTurnResultsWithItemIds(Long exptId, List<Long> itemIds, Long spaceId, Map<String, Object> ufields) {
    if (itemIds == null || itemIds.isEmpty() || ufields == null || ufields.isEmpty()) {
      return;
    }
    exptTurnResultMapper.updateTurnResultsWithItemIds(exptId, itemIds, spaceId, ufields);
  }

  // ========== 轮次结果运行日志相关方法实现 ==========

  /**
   * 批量创建轮次运行日志（如果不存在）
   */
  @Override
  public void batchCreateNxRunLog(List<ExptTurnResultRunLogEntity> turnResults) {
    if (turnResults == null || turnResults.isEmpty()) {
      return;
    }
    exptTurnResultRunLogMapper.batchCreateNxRunLog(turnResults);
  }

  /**
   * 获取数据项轮次运行日志
   */
  @Override
  public List<ExptTurnResultRunLogEntity> getItemTurnRunLogs(Long exptId, Long exptRunId, Long itemId, Long spaceId) {
    return exptTurnResultRunLogMapper.getItemTurnRunLogs(exptId, exptRunId, itemId, spaceId);
  }

  /**
   * 批量获取数据项轮次运行日志
   */
  @Override
  public List<ExptTurnResultRunLogEntity> mGetItemTurnRunLogs(Long exptId, Long exptRunId, List<Long> itemIds, Long spaceId) {
    return exptTurnResultRunLogMapper.mGetItemTurnRunLogs(exptId, exptRunId, itemIds, spaceId);
  }

  /**
   * 保存轮次运行日志
   */
  @Override
  public void saveTurnRunLogs(List<ExptTurnResultRunLogEntity> runLogs) {
    if (runLogs == null || runLogs.isEmpty()) {
      return;
    }
    for (ExptTurnResultRunLogEntity result : runLogs) {
      exptTurnResultRunLogMapper.saveTurnRunLog(result);
    }
  }

  /**
   * 根据数据项ID更新轮次运行日志
   */
  @Override
  public void updateTurnRunLogWithItemIds(Long spaceId, Long exptId, Long exptRunId, List<Long> itemIds, Map<String, Object> ufields) {
    if (itemIds == null || itemIds.isEmpty() || ufields == null || ufields.isEmpty()) {
      return;
    }
    exptTurnResultRunLogMapper.updateTurnRunLogWithItemIds(spaceId, exptId, exptRunId, itemIds, ufields);
  }

  /**
   * 扫描轮次运行日志
   */
  @Override
  public List<ExptTurnResultRunLogEntity> scanTurnRunLogs(Long exptId, Long cursor, Long limit, Long spaceId) {
    return exptTurnResultRunLogMapper.scanTurnRunLogs(exptId, cursor, limit, spaceId);
  }
}
