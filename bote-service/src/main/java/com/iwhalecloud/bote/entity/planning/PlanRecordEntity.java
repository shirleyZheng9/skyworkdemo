package com.iwhalecloud.bote.entity.planning;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 规划执行记录 Entity
 *
 * @author chen.linfa
 * @since 2025-05-14
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_plan_record")
public class PlanRecordEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long planId;

  @DiffField(name = "PLAN_TITLE")
  @Schema(description = "计划标题")
  private String planTitle;

  @DiffField(name = "USER_REQUEST")
  @Schema(description = "用户的原始请求")
  private String userRequest;

  @DiffField(name = "STATUS")
  @Schema(description = "执行状态")
  private Integer status;

  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;

  @DiffField(name = "BOT_ID")
  @Schema(description = "应用 ID")
  private Long botId;

  @DiffField(name = "START_TIME")
  @Schema(description = "开始时间")
  private Date startTime;

  @DiffField(name = "END_TIME")
  @Schema(description = "结束时间")
  private Date endTime;

  @DiffField(name = "SPENT_TIME")
  @Schema(description = "耗时")
  private Integer spentTime;

  @DiffField(name = "FAILED_STEP_NAME")
  @Schema(description = "失败步骤名称")
  private String failedStepName;

  @DiffField(name = "FAIL_REASON")
  @Schema(description = "失败原因")
  private String failReason;

  @DiffField(name = "SCENE_DSL")
  @Schema(description = "编排 DSL")
  private String sceneDsl;
}
