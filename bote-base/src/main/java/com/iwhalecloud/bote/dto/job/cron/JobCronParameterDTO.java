package com.iwhalecloud.bote.dto.job.cron;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Data;

/**
 * AI 智能体定时任务：结构化调度参数（与 job_cron_parameter JSON 字段对应）
 *
 * <p>支持的调度类型（{@code type}）：
 * <ul>
 *   <li>DAILY —— 每天 xx:xx</li>
 *   <li>WEEKLY —— 每周指定若干天 xx:xx</li>
   *   <li>MONTHLY —— 每月指定日（可多日，如 2 号与 10 号）xx:xx</li>
 *   <li>WORKDAY —— 工作日（周一至周五）xx:xx</li>
 *   <li>ONCE —— 不重复，仅在指定日期 xx:xx 执行一次</li>
 *   <li>CRON —— 自定义 cron 表达式（兜底）</li>
 * </ul>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobCronParameterDTO {

  private int schemaVersion;
  /** 调度类型：DAILY / WEEKLY / MONTHLY / WORKDAY / ONCE / CRON */
  private String type;
  /** 执行时间（时、分） */
  private TimePayload time;
  /** WEEKLY：需要执行的星期列表，ISO 标准 1=周一 ~ 7=周日 */
  private List<Integer> weekdays;
  /** MONTHLY：需要执行的日期列表，1~31，如 [15] 或 [2,10] */
  private List<Integer> daysOfMonth;
  /** ONCE：执行日期，格式 yyyy-MM-dd */
  private String date;
  /** CRON：自定义 Quartz cron 表达式（6 段） */
  private String cronExpression;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class TimePayload {
    private int hour;
    private int minute;
  }
}
