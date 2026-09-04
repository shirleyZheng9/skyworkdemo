package com.iwhalecloud.bote.dto.dashboard;

import java.util.Date;

/** 模型使用量汇总任务检查点。 */
public class ModelUsageAggregationCheckpointDTO {
  /** 汇总任务的稳定业务编码。 */
  private String taskCode;
  /** 对外声明的累计统计起点，起点之前的日志永不纳入。 */
  private Date statisticsStartTime;
  /** 最近成功处理日志的入库时间，是复合游标第一部分。 */
  private Date lastCreatedTime;
  /** 同一入库时间下最近成功处理的日志 ID，是复合游标第二部分。 */
  private Long lastLogId;

  public String getTaskCode() {
    return taskCode;
  }

  public void setTaskCode(String taskCode) {
    this.taskCode = taskCode;
  }

  public Date getStatisticsStartTime() {
    return statisticsStartTime;
  }

  public void setStatisticsStartTime(Date statisticsStartTime) {
    this.statisticsStartTime = statisticsStartTime;
  }

  public Date getLastCreatedTime() {
    return lastCreatedTime;
  }

  public void setLastCreatedTime(Date lastCreatedTime) {
    this.lastCreatedTime = lastCreatedTime;
  }

  public Long getLastLogId() {
    return lastLogId;
  }

  public void setLastLogId(Long lastLogId) {
    this.lastLogId = lastLogId;
  }
}
