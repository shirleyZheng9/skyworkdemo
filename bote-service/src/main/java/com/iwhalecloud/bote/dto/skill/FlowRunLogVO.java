package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 流程执行日志
 *
 * @author bianjp
 * @since 2025-03-05
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
@Schema(description = "流程执行日志")
public class FlowRunLogVO {
  @Schema(description = "日志 ID")
  private Long logId;
  @Schema(description = "对象类型(scene/flow)")
  private String objType;
  @Schema(description = "对象 ID (场景 ID 或工作流 ID)")
  private Long objId;
  @Schema(description = "对象名称(场景名称或工作流名称)")
  private String objName;
  @Schema(description = "上下文 ID")
  private String contextId;
  @Schema(description = "场景类型")
  private String sceneType;
  @Schema(description = "状态")
  private String logStatus;
  @Schema(description = "开始时间")
  private Date startTime;
  @Schema(description = "耗时(ms)")
  private Integer timeSpent;
  @Schema(description = "入参", hidden = true)
  private Map<String, Object> input;
  @Schema(description = "出参", hidden = true)
  private Object output;
  @Schema(description = "节点日志", hidden = true)
  private List<OrchestrationStepRunLog> stepLogs;
  @Schema(description = "失败信息")
  private String failMsg;
  @Schema(description = "异常堆栈")
  private String failStack;
  @Schema(description = "用户 ID")
  private Long userId;
  @Schema(description = "用户姓名")
  private String userName;

  @Schema(description = "入参", hidden = true)
  private String inputJson;
  @Schema(description = "出参", hidden = true)
  private String outputJson;
  @Schema(description = "节点日志", hidden = true)
  private String stepLogJson;

  @Schema(description = "流程ID")
  private Long flowId;
  @Schema(description = "智能体ID")
  private Long sceneId;
  @Schema(description = "租户ID")
  private Long tenantId;
}
