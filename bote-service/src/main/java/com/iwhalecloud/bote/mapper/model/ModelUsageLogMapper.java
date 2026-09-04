package com.iwhalecloud.bote.mapper.model;

import com.iwhalecloud.bote.entity.model.ModelUsageLogEntity;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 大模型使用量日志
 *
 * @author chen.linfa
 * @since 2026-04-07
 */
public interface ModelUsageLogMapper {
  /**
   * 新增日志
   */
  int insert(@Param("log") ModelUsageLogEntity log);

  /**
   * 查询待归档的日志ID
   */
  List<Long> selectLogIdsForArchive(@Param("maxDate") Date maxDate, @Param("limit") int limit);

  /**
   * 归档日志
   */
  int archiveByLogIds(@Param("logIds") List<Long> logIds);

  /**
   * 查询日志
   */
  List<ModelUsageLogEntity> selectLogListByLogIds(@Param("logIds") List<Long> logIds);

  /**
   * 批量新增历史日志
   */
  int batchInsertHistory(@Param("list") List<ModelUsageLogEntity> list);

  /**
   * 删除日志
   */
  int deleteByLogIds(@Param("logIds") List<Long> logIds);

  /**
   * 删除指定日期前的日志
   */
  int clearHistoryByMaxDate(@Param("maxDate") Date maxDate, @Param("limit") int limit);
}
