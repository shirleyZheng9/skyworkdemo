package com.iwhalecloud.bote.mapper.intent;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.intent.IntentLogDTO;
import com.iwhalecloud.bote.dto.intent.query.IntentQueryParams;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 意图识别日志管理
 *
 * @author auto
 * @since 2024-12-18
 */
public interface IntentLogManageMapper {

  /**
   * 根据主键获取意图识别日志
   *
   * @param logId 意图识别日志主键
   * @return 意图识别日志
   */
  IntentLogDTO getIntentLog(@Param("id") Long logId);

  /**
   * 新增意图识别日志
   *
   * @param intentLog 意图识别日志
   * @return 结果
   */
  int insertIntentLog(@Param("dto") IntentLogDTO intentLog);

  /**
   * 获取意图识别日志列表（分页）
   *
   * @param queryParams 查询条件
   * @return 意图识别日志分页列表
   */
  Page<IntentLogDTO> selectIntentLogPage(@Param("query") IntentQueryParams queryParams, RowBounds rowBounds);

  /**
   * 更新意图识别日志状态
   *
   * @param logId 日志 ID
   * @param markStatus 状态
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int updateIntentLogStatus(@Param("logId") Long logId, @Param("markStatus") String markStatus, @Param("updatorId") Long updatorId);
}
