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
 * 规划执行记录步骤 Entity
 *
 * @author chen.linfa
 * @since 2025-05-14
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_plan_step")
public class PlanStepEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long stepId;

  @DiffField(name = "PLAN_ID")
  @Schema(description = "计划 ID")
  private Long planId;

  @DiffField(name = "AGENT_ID")
  @Schema(description = "智能体 ID")
  private Long agentId;

  @DiffField(name = "AGENT_NAME")
  @Schema(description = "智能体名称")
  private String agentName;

  @DiffField(name = "AGENT_TYPE")
  @Schema(description = "智能体类型")
  private String agentType;

  @DiffField(name = "IS_AUTO_RUN")
  @Schema(description = "是否自动运行")
  private String isAutoRun;

  @DiffField(name = "AGENT_REQUEST")
  @Schema(description = "用于智能体执行的请求内容")
  private String agentRequest;

  @DiffField(name = "AGENT_PARAM")
  @Schema(description = "智能体变量")
  private String agentParam;

  @DiffField(name = "AGENT_RESULT")
  @Schema(description = "智能体执行结果")
  private String agentResult;

  @DiffField(name = "STEP_STATUS")
  @Schema(description = "执行状态")
  private Integer stepStatus;

  @DiffField(name = "STEP_INDEX")
  @Schema(description = "步骤顺序")
  private Integer stepIndex;

  @DiffField(name = "SESSION_ID")
  @Schema(description = "会话 ID")
  private Long sessionId;

  @DiffField(name = "CONTEXT_ID")
  @Schema(description = "对话上下文 ID")
  private String contextId;

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

  @DiffField(name = "FAIL_REASON")
  @Schema(description = "失败原因")
  private String failReason;

  @DiffField(name = "FLOW_STEP_JSON")
  @Schema(description = "流程步骤 JSON")
  private String flowStepJson;
}
