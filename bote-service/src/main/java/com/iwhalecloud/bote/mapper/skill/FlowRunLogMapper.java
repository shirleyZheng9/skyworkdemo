package com.iwhalecloud.bote.mapper.skill;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.skill.FlowRunLogVO;
import com.iwhalecloud.bote.dto.skill.query.FlowRunLogQueryParams;
import com.iwhalecloud.bote.entity.skill.FlowRunLogDTO;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;

/**
 * 流程执行日志相关的数据库操作
 *
 * @author bianjp
 * @since 2025-03-05
 */
public interface FlowRunLogMapper {
  /**
   * 插入流程日志
   */
  int insert(@Param("log") FlowRunLogDTO log);

  /**
   * 根据日志 ID 查询日志详情
   */
  @Nullable
  FlowRunLogVO selectLogById(@Param("logId") Long logId, @Param("tenantId") Long tenantId);

  /**
   * 根据日志 ID 批量查询节点日志
   */
  List<FlowRunLogVO> selectStepLogsByIds(@Param("logIds") List<Long> logIds);

  /**
   * 分页查询流程日志
   */
  Page<FlowRunLogVO> selectLogPage(@Param("query") FlowRunLogQueryParams queryParams, RowBounds rowBounds);

  /**
   * 删除指定日期前的日志
   */
  int deleteByMaxDate(@Param("maxDate") Date maxDate, @Param("limit") int limit);
}
