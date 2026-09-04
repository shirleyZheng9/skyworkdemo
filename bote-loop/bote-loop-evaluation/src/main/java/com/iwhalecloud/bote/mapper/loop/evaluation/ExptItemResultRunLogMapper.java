package com.iwhalecloud.bote.mapper.loop.evaluation;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptItemResultRunLogEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptItemRunLogFilter;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 实验数据项运行日志Mapper接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_item_result.go
 *
 * @author Generated
 * @since 2025-01-27
 */

public interface ExptItemResultRunLogMapper {

  /**
   * 批量创建实验数据项运行日志（如果不存在）
   * 迁移对应关系: Go语言IExptItemResultDAO.BatchCreateNxRunLogs
   */
  int batchCreateNxRunLogs(@Param("itemRunLogs") List<ExptItemResultRunLogEntity> itemRunLogs);

  /**
   * 扫描实验数据项运行日志
   * 迁移对应关系: Go语言IExptItemResultDAO.ScanItemRunLogs
   */
  List<ExptItemResultRunLogEntity> scanItemRunLogs(@Param("exptId") Long exptId,
                                                   @Param("exptRunId") Long exptRunId,
                                                   @Param("filter") ExptItemRunLogFilter filter,
                                                   @Param("cursor") Long cursor,
                                                   @Param("limit") Long limit,
                                                   @Param("spaceId") Long spaceId);

  /**
   * 更新数据项运行日志
   * 迁移对应关系: Go语言IExptItemResultDAO.UpdateItemRunLog
   */
  int updateItemRunLog(@Param("exptId") Long exptId,
                       @Param("exptRunId") Long exptRunId,
                       @Param("itemIds") List<Long> itemIds,
                       @Param("ufields") Map<String, Object> ufields,
                       @Param("spaceId") Long spaceId);

  /**
   * 获取数据项运行日志
   * 迁移对应关系: Go语言IExptItemResultDAO.GetItemRunLog
   */
  ExptItemResultRunLogEntity getItemRunLog(@Param("exptId") Long exptId,
                                           @Param("exptRunId") Long exptRunId,
                                           @Param("itemId") Long itemId,
                                           @Param("spaceId") Long spaceId);

  /**
   * 批量获取数据项运行日志
   * 迁移对应关系: Go语言IExptItemResultDAO.MGetItemRunLog
   */
  List<ExptItemResultRunLogEntity> mGetItemRunLog(@Param("exptId") Long exptId,
                                                  @Param("exptRunId") Long exptRunId,
                                                  @Param("itemIds") List<Long> itemIds,
                                                  @Param("spaceId") Long spaceId);

  // 新增方法：拆分MySQL特殊语法

  /**
   * 检查数据项运行日志是否存在
   * 根据spaceId、exptId、exptRunId、itemId判断记录是否存在
   */
  boolean existsByKeys(@Param("spaceId") Long spaceId,
                       @Param("exptId") Long exptId,
                       @Param("exptRunId") Long exptRunId,
                       @Param("itemId") Long itemId);

  /**
   * 插入数据项运行日志
   * 直接插入新记录
   */
  int insertItemRunLog(ExptItemResultRunLogEntity itemRunLog);

  /**
   * 更新数据项运行日志（保持原有条件）
   * 根据spaceId、exptId、exptRunId、itemId更新记录
   */
  int updateItemRunLogByKeys(ExptItemResultRunLogEntity itemRunLog);
}
