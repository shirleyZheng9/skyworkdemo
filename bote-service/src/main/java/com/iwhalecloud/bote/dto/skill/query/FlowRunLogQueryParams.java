package com.iwhalecloud.bote.dto.skill.query;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 流程执行日志查询参数
 *
 * @author bianjp
 * @since 2025-03-05
 */
@Getter
@Setter
@ToString(callSuper = true)
public class FlowRunLogQueryParams extends PagingQueryParams {
  @Schema(description = "租户 ID", requiredMode = RequiredMode.REQUIRED)
  private Long tenantId;
  @Schema(description = "对象类型(scene: 场景, flow: 工作流)", allowableValues = {"scene", "flow"})
  private String objType;
  @Schema(description = "场景 ID")
  private Long sceneId;
  @Schema(description = "工作流 ID")
  private Long flowId;
  @Schema(description = "上下文 ID")
  private String contextId;
  @Schema(description = "用户 ID")
  private Long userId;
  @Schema(description = "最小开始时间")
  private Date minStartTime;
  @Schema(description = "最大开始时间")
  private Date maxStartTime;
  @Schema(description = "处理状态(S: 成功, F: 失败)", allowableValues = {BaseConsts.STATE_SUCCESS, BaseConsts.STATE_FAIL})
  private String logStatus;

}
