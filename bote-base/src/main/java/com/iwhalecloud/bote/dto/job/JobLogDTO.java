package com.iwhalecloud.bote.dto.job;

import com.iwhalecloud.bss.litchi.job.vo.BssJobLogVO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 定时任务日志 DTO
 *
 * @author qian.sisheng
 * @since 2025-11-07
 */
@Getter
@Setter
@ToString(callSuper = true)
public class JobLogDTO {
  @Schema(description = "日志 ID")
  private Long logId;
  @Schema(description = "定时任务 ID")
  private Long jobId;
  @Schema(description = "执行 IP")
  private String ip;
  @Schema(description = "开始时间")
  private Date startTime;
  @Schema(description = "结束时间")
  private Date endTime;
  @Schema(description = "耗时")
  private Integer spentTime;
  @Schema(description = "执行结果（-1 失败 10 成功）")
  private String statusCd;
  @Schema(description = "异常日志")
  private String failReason;
  @Schema(description = "定时任务名称")
  private String jobName;

  /**
   * 从 BssJobLogVO 转换为 JobLogDTO
   */
  public JobLogDTO toBssJobLog(BssJobLogVO log) {
    JobLogDTO jobLog = new JobLogDTO();
    jobLog.setLogId(log.getLogId());
    jobLog.setJobId(Long.valueOf(log.getJobId()));
    jobLog.setIp(log.getIp());
    jobLog.setStartTime(log.getStartTime());
    jobLog.setEndTime(log.getEndTime());
    jobLog.setSpentTime(log.getSpentTime());
    jobLog.setStatusCd(log.getStatusCd());
    jobLog.setFailReason(log.getFailReason());
    return jobLog;
  }
}
