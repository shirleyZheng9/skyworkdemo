package com.iwhalecloud.bote.entity.base;

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
 * 发布步骤信息 Entity
 *
 * @author auto
 * @since 2024-10-21
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_publish_step")
public class PublishStepEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long id;
  @DiffField(name = "PUBLISH_ID")
  @Schema(description = "记录 ID")
  private Long publishId;
  @DiffField(name = "STEP_INDEX")
  @Schema(description = "步骤序号")
  private Integer stepIndex;
  @DiffField(name = "STEP_TYPE")
  @Schema(description = "步骤类型")
  private Integer stepType;
  @DiffField(name = "STEP_NAME")
  @Schema(description = "步骤名称")
  private String stepName;
  @DiffField(name = "STEP_STATUS")
  @Schema(description = "步骤状态")
  private Integer stepStatus;
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
  @DiffField(name = "FAIL_STACK")
  @Schema(description = "堆栈信息")
  private String failStack;
  @DiffField(name = "INPUT")
  @Schema(description = "输入信息")
  private String input;
  @DiffField(name = "OUTPUT_JSON")
  @Schema(description = "输出信息")
  private String outputJson;
}
