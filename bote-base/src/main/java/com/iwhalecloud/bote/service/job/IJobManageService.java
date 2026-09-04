package com.iwhalecloud.bote.service.job;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.job.JobLogDTO;
import com.iwhalecloud.bote.dto.job.query.JobQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bote.dto.job.JobDTO;
import java.util.List;

/**
 * 定时任务管理服务
 *
 * @author qian.sisheng
 * @since 2025-11-06
 */
public interface IJobManageService {

  /**
   * 查询单个定时任务
   *
   * @param jobId 定时任务主键
   * @return 定时任务
   */
  JobDTO findJob(Long jobId);

  /**
   * 保存定时任务
   *
   * @param job 定时任务
   * @return 结果
   */
  ResultVO<JobDTO> saveJob(JobDTO job);

  /**
   * 删除定时任务
   *
   * @param bssJobId 定时任务主键
   * @return 结果
   */
  ResultVO<Void> deleteJob(Long bssJobId);

  /**
   * 查询定时任务列表
   *
   * @param queryParams 查询条件
   * @return 定时任务列表
   */
  List<JobDTO> queryJobList(JobQueryParams queryParams);

  /**
   * 查询定时任务列表（分页）
   *
   * @param queryParams 查询条件
   * @return 定时任务分页列表
   */
  PageInfo<JobDTO> queryJobPage(JobQueryParams queryParams);

  /**
   * 检查定时任务是否正在运行
   *
   * @param jobId 定时任务主键
   * @return 是否正在运行
   */
  ResultVO<String> checkJobRunning(Long jobId);

  /**
   * 恢复定时任务
   *
   * @param jobId 定时任务主键
   * @return 恢复结果
   */
  ResultVO<Void> resumeJob(Long jobId);

  /**
   * 暂停定时任务
   *
   * @param jobId 定时任务主键
   * @return 暂停结果
   */
  ResultVO<Void> pauseJob(Long jobId);

  /**
   * 触发定时任务
   *
   * @param jobId 定时任务主键
   * @return 触发结果
   */
  ResultVO<Void> triggerJob(Long jobId);

  /**
   * 刷新定时任务
   *
   * @param jobId 定时任务主键
   * @return 刷新结果
   */
  ResultVO<Void> refreshJob(Long jobId);

  /**
   * 查询定时任务日志列表（分页）
   *
   * @param queryParams 查询条件
   * @return 定时任务日志分页列表
   */
  PageInfo<JobLogDTO> queryJobLogPage(JobQueryParams queryParams);

  /**
   * 查询定时任务列表（运行态使用）
   *
   * @param queryParams 查询条件
   * @return 定时任务分页列表
   */
  PageInfo<JobDTO> queryJobPageForRuntime(JobQueryParams queryParams);
}
