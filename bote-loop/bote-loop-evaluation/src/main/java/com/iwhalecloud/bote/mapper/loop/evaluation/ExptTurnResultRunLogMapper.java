package com.iwhalecloud.bote.mapper.loop.evaluation;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultRunLogEntity;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 实验轮次结果运行日志Mapper接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_turn_result.go
 *
 * @author Generated
 * @since 2025-01-27
 */

public interface ExptTurnResultRunLogMapper {

  /**
   * 批量创建轮次运行日志（如果不存在）
   * 迁移对应关系: Go语言ExptTurnResultDAOImpl.BatchCreateNXRunLog
   */
  int batchCreateNxRunLog(@Param("turnResults") List<ExptTurnResultRunLogEntity> turnResults);

  /**
   * 获取数据项轮次运行日志
   * 迁移对应关系: Go语言ExptTurnResultDAOImpl.GetItemTurnRunLogs
   */
  List<ExptTurnResultRunLogEntity> getItemTurnRunLogs(@Param("exptId") Long exptId,
                                                      @Param("exptRunId") Long exptRunId,
                                                      @Param("itemId") Long itemId,
                                                      @Param("spaceId") Long spaceId);

  /**
   * 批量获取数据项轮次运行日志
   * 迁移对应关系: Go语言ExptTurnResultDAOImpl.MGetItemTurnRunLogs
   */
  List<ExptTurnResultRunLogEntity> mGetItemTurnRunLogs(@Param("exptId") Long exptId,
                                                       @Param("exptRunId") Long exptRunId,
                                                       @Param("itemIds") List<Long> itemIds,
                                                       @Param("spaceId") Long spaceId);

  /**
   * 更新单个轮次运行日志
   * 根据 id 更新记录
   */
  int saveTurnRunLog(@Param("result") ExptTurnResultRunLogEntity result);

  /**
   * 根据数据项ID更新轮次运行日志
   * 迁移对应关系: Go语言ExptTurnResultDAOImpl.UpdateTurnRunLogWithItemIDs
   */
  int updateTurnRunLogWithItemIds(@Param("spaceId") Long spaceId,
                                  @Param("exptId") Long exptId,
                                  @Param("exptRunId") Long exptRunId,
                                  @Param("itemIds") List<Long> itemIds,
                                  @Param("ufields") Map<String, Object> ufields);

  /**
   * 扫描轮次运行日志
   * 迁移对应关系: Go语言ExptTurnResultDAOImpl.ScanTurnRunLogs
   */
  List<ExptTurnResultRunLogEntity> scanTurnRunLogs(@Param("exptId") Long exptId,
                                                   @Param("cursor") Long cursor,
                                                   @Param("limit") Long limit,
                                                   @Param("spaceId") Long spaceId);
}
