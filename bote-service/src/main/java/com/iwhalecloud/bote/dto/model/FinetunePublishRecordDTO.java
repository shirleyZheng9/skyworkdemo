package com.iwhalecloud.bote.dto.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微调发布记录
 *
 * @author chen.linfa
 * @since 2025-03-04
 */
@Getter
@Setter
@ToString
public class FinetunePublishRecordDTO {

  @Schema(description = "主键")
  private Long id;
  @Schema(description = "类型")
  private String publishType;
  @Schema(description = "状态(0: 未开始, 1: 运行中, 10: 成功, -1: 失败)")
  private Integer publishStatus;
  @Schema(description = "开始时间")
  private Date startTime;
  @Schema(description = "结束时间")
  private Date endTime;
  @Schema(description = "耗时")
  private Integer spentTime;
  @Schema(description = "失败步骤名称")
  private String failedStepName;
  @Schema(description = "失败原因")
  private String failReason;
  @Schema(description = "租户 ID")
  private Long tenantId;

  @Schema(description = "模型 ID")
  private Long modelId;
  @Schema(description = "模型名称")
  private String modelName;
  @Schema(description = "模型图标")
  private String modelIcon;
  @Schema(description = "修改人名称")
  private String updatorName;
  @Schema(description = "微调用途")
  private String useType;
}
