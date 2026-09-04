package com.iwhalecloud.bote.entity.job;

import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import com.iwhalecloud.bss.litchi.job.vo.BssJobVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 定时任务 Entity
 *
 * @author qian.sisheng
 * @since 2025-11-06
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_JOB")
public class JobEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long bssJobId;
  @DiffField(name = "BSS_JOB_NAME")
  @Schema(description = "定时任务名称")
  private String bssJobName;
  @DiffField(name = "BSS_JOB_CLASS")
  @Schema(description = "执行类")
  private String bssJobClass;
  @DiffField(name = "BSS_JOB_CRON")
  @Schema(description = "执行周期")
  private String bssJobCron;
  @DiffField(name = "IP")
  @Schema(description = "服务器IP")
  private String ip;
  @DiffField(name = "CENTER")
  @Schema(description = "中心编码")
  private String center;
  @DiffField(name = "STATE")
  @Schema(description = "状态")
  private String state;
  @DiffField(name = "SHARDING_TOTAL_COUNT")
  @Schema(description = "分片总数")
  private Long shardingTotalCount;
  @DiffField(name = "SHARDING_ITEM_PARAMETERS")
  @Schema(description = "分片参数")
  private String shardingItemParameters;
  @DiffField(name = "JOB_EXCEPTION_HANDLER")
  @Schema(description = "异常处理类")
  private String jobExceptionHandler;
  @DiffField(name = "EXECUTOR_SERVICE_HANDLER")
  @Schema(description = "线程池")
  private String executorServiceHandler;
  @DiffField(name = "MONITOR_PORT")
  @Schema(description = "监控端口")
  private String monitorPort;
  @DiffField(name = "JOB_SHARDING_STRATEGY_CLASS")
  @Schema(description = "分片策略")
  private String jobShardingStrategyClass;
  @DiffField(name = "DESCRIPTION")
  @Schema(description = "定时任务描述")
  private String description;
  @DiffField(name = "JOB_PARAMETER")
  @Schema(description = "自定义任务参数")
  private String jobParameter;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "SPACE_ID")
  @Schema(description = "空间ID")
  private Long spaceId;
  @DiffField(name = "BOTE_ID")
  @Schema(description = "应用ID")
  private Long botId;
  @DiffField(name = "CRON_DESC")
  @Schema(description = "cron表达式描述")
  private String cronDesc;
  @DiffField(name = "JOB_CRON_PARAMETER")
  @Schema(description = "结构化调度参数（JSON）")
  private String jobCronParameter;
  @DiffField(name = "DATA_FROM")
  @Schema(description = "数据来源")
  private String dataFrom;

  /**
   * 转为 BSS 任务类
   */
  public BssJobVO toBssJob() {
    BssJobVO vo = new BssJobVO();
    vo.setBssJobId(bssJobId.toString());
    vo.setBssJobName(bssJobName);
    vo.setDescription(null);
    vo.setBssJobClass(bssJobClass);
    vo.setBssJobCron(bssJobCron);
    vo.setCenter(center);
    vo.setState(CommonConsts.STATUS_CD_VALID);
    vo.setJobShardingStrategyClass(jobShardingStrategyClass);
    vo.setShardingTotalCount(shardingTotalCount == null ? 1 : shardingTotalCount.intValue());
    vo.setShardingItemParameters(shardingItemParameters);
    vo.setIp(ip);
    vo.setJobExceptionHandler(jobExceptionHandler);
    vo.setExecutorServiceHandler(executorServiceHandler);
    vo.setMonitorPort(monitorPort);
    vo.setJobParameter(jobParameter);
    return vo;
  }
}
