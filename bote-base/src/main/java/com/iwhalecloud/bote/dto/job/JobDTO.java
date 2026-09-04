package com.iwhalecloud.bote.dto.job;

import com.iwhalecloud.bote.entity.job.JobEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 定时任务 DTO
 *
 * @author qian.sisheng
 * @since 2025-11-06
 */
@Getter
@Setter
@ToString(callSuper = true)
public class JobDTO extends JobEntity {
  @Schema(description = "是否运行")
  private String isRunning;
  @Schema(description = "智能应用名称")
  private String botName;
  @Schema(description = "最后运行时间")
  private Date lastRunTime;
}
