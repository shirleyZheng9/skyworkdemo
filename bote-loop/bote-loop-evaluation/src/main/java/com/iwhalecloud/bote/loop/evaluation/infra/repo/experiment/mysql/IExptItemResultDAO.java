package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptItemResultEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptItemResultRunLogEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemRunLogFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Page;
import java.util.List;
import java.util.Map;

/**
 * 实验数据项结果DAO接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_item_result.go
 *
 * @author Generated
 * @since 2025-01-27
 */
public interface IExptItemResultDAO {

  /**
   * 批量获取实验数据项结果
   * 迁移对应关系: Go语言IExptItemResultDAO.BatchGet
   *
   * @param spaceId 空间ID
   * @param exptId 实验ID
   * @param itemIds 数据项ID列表
   * @return 实验数据项结果列表
   */
  List<ExptItemResultEntity> batchGet(Long spaceId, Long exptId, List<Long> itemIds);

  /**
   * 批量创建实验数据项结果（如果不存在）
   * 迁移对应关系: Go语言IExptItemResultDAO.BatchCreateNX
   *
   * @param itemResults 实验数据项结果列表
   */
  void batchCreateNx(List<ExptItemResultEntity> itemResults);

  /**
   * 扫描实验数据项结果
   * 迁移对应关系: Go语言IExptItemResultDAO.ScanItemResults
   *
   * @param exptId 实验ID
   * @param cursor 游标
   * @param limit 限制数量
   * @param status 状态列表
   * @param spaceId 空间ID
   * @return 扫描结果和下一个游标
   */
  List<ExptItemResultEntity> scanItemResults(Long exptId, Long cursor, Long limit, List<Integer> status, Long spaceId);

  /**
   * 根据实验ID获取数据项ID列表
   * 迁移对应关系: Go语言IExptItemResultDAO.GetItemIDListByExptID
   *
   * @param exptId 实验ID
   * @param spaceId 空间ID
   * @return 数据项ID列表
   */
  List<Long> getItemIdListByExptId(Long exptId, Long spaceId);

  /**
   * 根据实验ID分页查询数据项结果
   * 迁移对应关系: Go语言IExptItemResultDAO.ListItemResultsByExptID
   *
   * @param exptId 实验ID
   * @param spaceId 空间ID
   * @param page 分页参数
   * @param desc 是否降序
   * @return 数据项结果列表和总数
   */
  PageInfo<ExptItemResultEntity> listItemResultsByExptId(Long exptId, Long spaceId, Page page, Boolean desc);

  /**
   * 保存实验数据项结果
   * 迁移对应关系: Go语言IExptItemResultDAO.SaveItemResults
   *
   * @param itemResults 实验数据项结果列表
   */
  void saveItemResults(List<ExptItemResultEntity> itemResults);

  /**
   * 获取数据项轮次结果
   * 迁移对应关系: Go语言IExptItemResultDAO.GetItemTurnResults
   *
   * @param spaceId 空间ID
   * @param exptId 实验ID
   * @param itemId 数据项ID
   * @return 轮次结果列表
   */
  List<ExptTurnResultEntity> getItemTurnResults(Long spaceId, Long exptId, Long itemId);

  /**
   * 更新数据项结果
   * 迁移对应关系: Go语言IExptItemResultDAO.UpdateItemsResult
   *
   * @param spaceId 空间ID
   * @param exptId 实验ID
   * @param itemIds 数据项ID列表
   * @param ufields 更新字段
   */
  void updateItemsResult(Long spaceId, Long exptId, List<Long> itemIds, Map<String, Object> ufields);

  /**
   * 根据实验ID获取最大数据项序号
   * 迁移对应关系: Go语言IExptItemResultDAO.GetMaxItemIdxByExptID
   *
   * @param exptId 实验ID
   * @param spaceId 空间ID
   * @return 最大数据项序号
   */
  Integer getMaxItemIdxByExptId(Long exptId, Long spaceId);

  /**
   * 批量创建实验数据项运行日志（如果不存在）
   * 迁移对应关系: Go语言IExptItemResultDAO.BatchCreateNXRunLogs
   *
   * @param itemRunLogs 实验数据项运行日志列表
   */
  void batchCreateNxRunLogs(List<ExptItemResultRunLogEntity> itemRunLogs);

  /**
   * 扫描实验数据项运行日志
   * 迁移对应关系: Go语言IExptItemResultDAO.ScanItemRunLogs
   *
   * @param exptId 实验ID
   * @param exptRunId 实验运行ID
   * @param filter 过滤条件
   * @param cursor 游标
   * @param limit 限制数量
   * @param spaceId 空间ID
   * @return 扫描结果和下一个游标
   */
  List<ExptItemResultRunLogEntity> scanItemRunLogs(Long exptId, Long exptRunId, ExptItemRunLogFilter filter, Long cursor, Long limit, Long spaceId);

  /**
   * 更新数据项运行日志
   * 迁移对应关系: Go语言IExptItemResultDAO.UpdateItemRunLog
   *
   * @param exptId 实验ID
   * @param exptRunId 实验运行ID
   * @param itemIds 数据项ID列表
   * @param ufields 更新字段
   * @param spaceId 空间ID
   */
  void updateItemRunLog(Long exptId, Long exptRunId, List<Long> itemIds, Map<String, Object> ufields, Long spaceId);

  /**
   * 获取数据项运行日志
   * 迁移对应关系: Go语言IExptItemResultDAO.GetItemRunLog
   *
   * @param exptId 实验ID
   * @param exptRunId 实验运行ID
   * @param itemId 数据项ID
   * @param spaceId 空间ID
   * @return 数据项运行日志
   */
  ExptItemResultRunLogEntity getItemRunLog(Long exptId, Long exptRunId, Long itemId, Long spaceId);

  /**
   * 批量获取数据项运行日志
   * 迁移对应关系: Go语言IExptItemResultDAO.MGetItemRunLog
   *
   * @param exptId 实验ID
   * @param exptRunId 实验运行ID
   * @param itemIds 数据项ID列表
   * @param spaceId 空间ID
   * @return 数据项运行日志列表
   */
  List<ExptItemResultRunLogEntity> mGetItemRunLog(Long exptId, Long exptRunId, List<Long> itemIds, Long spaceId);
}
