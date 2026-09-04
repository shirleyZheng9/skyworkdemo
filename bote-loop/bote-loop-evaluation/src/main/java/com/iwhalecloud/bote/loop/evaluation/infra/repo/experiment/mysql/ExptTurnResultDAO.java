package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnEvaluatorResultRefEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultRunLogEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemTurnID;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Page;
import java.util.List;
import java.util.Map;

/**
 * 实验轮次结果DAO接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_turn_result.go
 *
 * @author Generated
 * @since 2025-01-27
 */
public interface ExptTurnResultDAO {

  // ========== 轮次结果相关方法 ==========

  /**
   * 分页查询轮次结果
   * 迁移对应关系: Go语言ExptTurnResultDAO.ListTurnResult
   *
   * @param spaceId 空间ID
   * @param exptId 实验ID
   * @param filter 过滤条件
   * @param page 分页信息
   * @param desc 是否降序
   * @return 轮次结果列表和总数
   */
  List<ExptTurnResultEntity> listTurnResult(Long spaceId, Long exptId, ExptTurnResultFilter filter, Page page, Boolean desc);

  /**
   * 根据数据项ID列表分页查询轮次结果
   * 迁移对应关系: Go语言ExptTurnResultDAO.ListTurnResultByItemIDs
   *
   * @param spaceId 空间ID
   * @param exptId 实验ID
   * @param itemIds 数据项ID列表
   * @param filter 过滤条件
   * @param page 分页信息
   * @param desc 是否降序
   * @return 轮次结果列表和总数
   */
  List<ExptTurnResultEntity> listTurnResultByItemIds(Long spaceId, Long exptId, List<Long> itemIds, ExptTurnResultFilter filter, Page page, Boolean desc);

  /**
   * 批量获取轮次结果
   * 迁移对应关系: Go语言ExptTurnResultDAO.BatchGet
   *
   * @param spaceId 空间ID
   * @param exptId 实验ID
   * @param itemIds 数据项ID列表
   * @return 轮次结果列表
   */
  List<ExptTurnResultEntity> batchGet(Long spaceId, Long exptId, List<Long> itemIds);

  /**
   * 创建轮次评估器引用
   * 迁移对应关系: Go语言ExptTurnResultDAO.CreateTurnEvaluatorRefs
   *
   * @param refs 评估器引用列表
   */
  void createTurnEvaluatorRefs(List<ExptTurnEvaluatorResultRefEntity> refs);

  /**
   * 批量创建轮次结果（如果不存在）
   * 迁移对应关系: Go语言ExptTurnResultDAO.BatchCreateNX
   *
   * @param turnResults 轮次结果列表
   */
  void batchCreateNx(List<ExptTurnResultEntity> turnResults);

  /**
   * 获取数据项轮次结果
   * 迁移对应关系: Go语言ExptTurnResultDAO.GetItemTurnResults
   *
   * @param exptId 实验ID
   * @param itemId 数据项ID
   * @param spaceId 空间ID
   * @return 轮次结果列表
   */
  List<ExptTurnResultEntity> getItemTurnResults(Long exptId, Long itemId, Long spaceId);

  List<ExptTurnResultEntity> getItemTurnResults(Long exptId, Long itemId, Long spaceId, Long exptRunId);

  /**
   * 保存轮次结果
   * 迁移对应关系: Go语言ExptTurnResultDAO.SaveTurnResults
   *
   * @param turnResults 轮次结果列表
   */
  void saveTurnResults(List<ExptTurnResultEntity> turnResults);

  /**
   * 扫描轮次结果
   * 迁移对应关系: Go语言ExptTurnResultDAO.ScanTurnResults
   *
   * @param exptId 实验ID
   * @param status 状态列表
   * @param cursor 游标
   * @param limit 限制数量
   * @param spaceId 空间ID
   * @return 轮次结果列表和下一个游标
   */
  PageInfo<ExptTurnResultEntity> scanTurnResults(Long exptId, List<Integer> status, int pageNumber, Long pageSize, Long spaceId);

  /**
   * 更新轮次结果
   * 迁移对应关系: Go语言ExptTurnResultDAO.UpdateTurnResults
   *
   * @param exptId 实验ID
   * @param itemTurnIds 数据项轮次ID列表
   * @param spaceId 空间ID
   * @param ufields 更新字段
   */
  void updateTurnResults(Long exptId, List<ItemTurnID> itemTurnIds, Long spaceId, Map<String, Object> ufields);

  /**
   * 根据数据项ID更新轮次结果
   * 迁移对应关系: Go语言ExptTurnResultDAO.UpdateTurnResultsWithItemIDs
   *
   * @param exptId 实验ID
   * @param itemIds 数据项ID列表
   * @param spaceId 空间ID
   * @param ufields 更新字段
   */
  void updateTurnResultsWithItemIds(Long exptId, List<Long> itemIds, Long spaceId, Map<String, Object> ufields);

  // ========== 轮次结果运行日志相关方法 ==========

  /**
   * 批量创建轮次运行日志（如果不存在）
   * 迁移对应关系: Go语言ExptTurnResultDAO.BatchCreateNXRunLog
   *
   * @param turnResults 轮次运行日志列表
   */
  void batchCreateNxRunLog(List<ExptTurnResultRunLogEntity> turnResults);

  /**
   * 获取数据项轮次运行日志
   * 迁移对应关系: Go语言ExptTurnResultDAO.GetItemTurnRunLogs
   *
   * @param exptId 实验ID
   * @param exptRunId 实验运行ID
   * @param itemId 数据项ID
   * @param spaceId 空间ID
   * @return 轮次运行日志列表
   */
  List<ExptTurnResultRunLogEntity> getItemTurnRunLogs(Long exptId, Long exptRunId, Long itemId, Long spaceId);

  /**
   * 批量获取数据项轮次运行日志
   * 迁移对应关系: Go语言ExptTurnResultDAO.MGetItemTurnRunLogs
   *
   * @param exptId 实验ID
   * @param exptRunId 实验运行ID
   * @param itemIds 数据项ID列表
   * @param spaceId 空间ID
   * @return 轮次运行日志列表
   */
  List<ExptTurnResultRunLogEntity> mGetItemTurnRunLogs(Long exptId, Long exptRunId, List<Long> itemIds, Long spaceId);

  /**
   * 保存轮次运行日志
   * 迁移对应关系: Go语言ExptTurnResultDAO.SaveTurnRunLogs
   *
   * @param runLogs 轮次运行日志列表
   */
  void saveTurnRunLogs(List<ExptTurnResultRunLogEntity> runLogs);

  /**
   * 根据数据项ID更新轮次运行日志
   * 迁移对应关系: Go语言ExptTurnResultDAO.UpdateTurnRunLogWithItemIDs
   *
   * @param spaceId 空间ID
   * @param exptId 实验ID
   * @param exptRunId 实验运行ID
   * @param itemIds 数据项ID列表
   * @param ufields 更新字段
   */
  void updateTurnRunLogWithItemIds(Long spaceId, Long exptId, Long exptRunId, List<Long> itemIds, Map<String, Object> ufields);

  /**
   * 扫描轮次运行日志
   * 迁移对应关系: Go语言ExptTurnResultDAO.ScanTurnRunLogs
   *
   * @param exptId 实验ID
   * @param cursor 游标
   * @param limit 限制数量
   * @param spaceId 空间ID
   * @return 轮次运行日志列表和下一个游标
   */
  List<ExptTurnResultRunLogEntity> scanTurnRunLogs(Long exptId, Long cursor, Long limit, Long spaceId);
}
