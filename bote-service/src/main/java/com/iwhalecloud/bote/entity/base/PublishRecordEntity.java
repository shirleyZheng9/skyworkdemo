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
 * 发布记录 Entity
 *
 * @author auto
 * @since 2024-10-21
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_publish_record")
public class PublishRecordEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long id;
  @DiffField(name = "PUBLISH_TYPE")
  @Schema(description = "类型")
  private String publishType;
  @DiffField(name = "PUBLISH_STATUS")
  @Schema(description = "状态(0: 未开始, 1: 运行中, 10: 成功, -1: 失败)")
  private Integer publishStatus;
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
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
  @DiffField(name = "OBJ_ID")
  @Schema(description = "对象 ID")
  private Long objId;
  @DiffField(name = "INPUT")
  @Schema(description = "入参")
  private String input;
}
