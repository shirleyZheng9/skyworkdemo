package com.iwhalecloud.bote.controller.job;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.dto.job.JobLogDTO;
import com.iwhalecloud.bote.dto.job.JobDTO;
import com.iwhalecloud.bote.dto.job.query.JobQueryParams;
import com.iwhalecloud.bote.service.job.IJobManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 定时任务管理 controller
 *
 * @author qian.sisheng
 * @since 2025-11-06
 */
@RestController
@RequestMapping(value = CommonConsts.API_PREFIX + "manager/job", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "定时任务管理")
public class JobManageController {

  private final IJobManageService JobManageService;

  @Operation(summary = "查询单个定时任务")
  @GetMapping("findJob")
  public ResultVO<JobDTO> findJob(@RequestParam(name = "jobId") Long jobId) {
    Assert.notNull(jobId, "主键 ID 不能为空");
    return ResultVO.success(JobManageService.findJob(jobId));
  }

  @Operation(summary = "保存定时任务")
  @PostMapping("saveJob")
  public ResultVO<JobDTO> saveJob(@RequestBody JobDTO job) {
    ResultVO<JobDTO> result = JobManageService.saveJob(job);
    if (result.isSuccess()) {
      JobManageService.refreshJob(job.getBssJobId());
    }
    return result;
  }

  @Operation(summary = "删除定时任务")
  @GetMapping("deleteJob")
  public ResultVO<Void> deleteJob(@RequestParam(name = "jobId") Long jobId) {
    Assert.notNull(jobId, "主键 ID 不能为空");
    return JobManageService.deleteJob(jobId);
  }

  @Operation(summary = "查询定时任务列表")
  @PostMapping("queryJobList")
  public ResultVO<List<JobDTO>> queryJobList(@RequestBody JobQueryParams queryParams) {
    return ResultVO.success(JobManageService.queryJobList(queryParams));
  }

  @Operation(summary = "分页查询定时任务")
  @PostMapping("queryJobPage")
  public ResultVO<PageInfo<JobDTO>> queryJobPage(@RequestBody JobQueryParams queryParams) {
    return ResultVO.success(JobManageService.queryJobPage(queryParams));
  }

  @Operation(summary = "检查定时任务是否正在运行")
  @GetMapping("checkJobRunning")
  public ResultVO<String> checkJobRunning(@RequestParam(name = "jobId") Long jobId) {
    return JobManageService.checkJobRunning(jobId);
  }

  @Operation(summary = "暂停定时任务")
  @GetMapping("pauseJob")
  public ResultVO<Void> pauseJob(@RequestParam(name = "jobId") Long jobId) {
    return JobManageService.pauseJob(jobId);
  }

  @Operation(summary = "恢复定时任务")
  @GetMapping("resumeJob")
  public ResultVO<Void> resumeJob(@RequestParam(name = "jobId") Long jobId) {
    return JobManageService.resumeJob(jobId);
  }

  @Operation(summary = "触发定时任务")
  @GetMapping("triggerJob")
  public ResultVO<Void> triggerJob(@RequestParam(name = "jobId") Long jobId) {
    return JobManageService.triggerJob(jobId);
  }

  @Operation(summary = "刷新定时任务")
  @GetMapping("refreshJob")
  public ResultVO<Void> refreshJob(@RequestParam(name = "jobId") Long jobId) {
    return JobManageService.refreshJob(jobId);
  }

  @Operation(summary = "分页查询定时任务日志")
  @PostMapping("queryJobLogPage")
  public ResultVO<PageInfo<JobLogDTO>> queryJobLogPage(@RequestBody JobQueryParams queryParams) {
    return ResultVO.success(JobManageService.queryJobLogPage(queryParams));
  }
}
