package com.iwhalecloud.bote.mapper.job;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.job.JobLogDTO;
import com.iwhalecloud.bote.dto.job.JobDTO;
import com.iwhalecloud.bote.dto.job.query.JobQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 定时任务管理
 *
 * @author qian.sisheng
 * @since 2025-11-06
 */
public interface JobManageMapper {
  /**
   * 校验定时任务的名称唯一性
   *
   * @param job 定时任务
   * @return 结果
   */
  boolean existsJobName(@Param("dto") JobDTO job);

  /**
   * 根据主键获取定时任务
   *
   * @param jobId 定时任务主键
   * @return 定时任务
   */
  JobDTO getJob(@Param("jobId") Long jobId);

  /**
   * 新增定时任务
   *
   * @param job 定时任务
   * @return 结果
   */
  int insertJob(@Param("dto") JobDTO job);

  /**
   * 新增定时任务日志
   *
   * @param jobLog 定时任务日志
   * @return 结果
   */
  int insertJobLog(@Param("dto") JobLogDTO jobLog);

  /**
   * 修改定时任务
   *
   * @param job 定时任务
   * @return 结果
   */
  int updateJob(@Param("dto") JobDTO job);

  /**
   * 删除属性
   *
   * @param jobId 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteJob(@Param("jobId") Long jobId, @Param("updatorId") Long updatorId);

  /**
   * 获取定时任务列表
   *
   * @param queryParams 查询条件
   * @return 定时任务列表
   */
  List<JobDTO> selectJobList(@Param("query") JobQueryParams queryParams);

  /**
   * 获取定时任务列表（分页）
   *
   * @param queryParams 查询条件
   * @return 定时任务分页列表
   */
  Page<JobDTO> selectJobPage(@Param("query") JobQueryParams queryParams, RowBounds rowBounds);

  /**
   * 获取定时任务日志列表（分页）
   *
   * @param queryParams 查询条件
   * @return 定时任务日志分页列表
   */
  Page<JobLogDTO> selectJobLogPage(@Param("query") JobQueryParams queryParams, RowBounds rowBounds);

  /**
   * 获取定时任务列表（运行态使用）
   *
   * @param queryParams 查询条件
   * @return 定时任务分页列表
   */
  Page<JobDTO> selectJobPageForRuntime(@Param("query") JobQueryParams queryParams, RowBounds rowBounds);
}
