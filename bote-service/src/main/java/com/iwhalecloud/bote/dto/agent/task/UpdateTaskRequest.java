package com.iwhalecloud.bote.dto.agent.task;

import com.iwhalecloud.bote.common.consts.GeneralAgentConsts;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 更新任务请求
 *
 * @author bianjp
 * @since 2026-04-10
 */
@Getter
@Setter
@ToString
@Schema(description = "更新任务请求")
public class UpdateTaskRequest {
  @Schema(description = "The ID of the task to update", requiredMode = Schema.RequiredMode.REQUIRED)
  private Integer taskId;
  @Schema(description = "New subject for the task")
  private String subject;
  @Schema(description = "New description for the task")
  private String description;
  @Schema(description = "Present continuous form shown in spinner when in_progress (e.g., \"Running tests\")")
  private String activeForm;
  @Schema(description = "New status for the task", allowableValues = {GeneralAgentConsts.AGENT_TASK_STATUS_PENDING, GeneralAgentConsts.AGENT_TASK_STATUS_PROCESSING, GeneralAgentConsts.AGENT_TASK_STATUS_COMPLETED, GeneralAgentConsts.AGENT_TASK_STATUS_DELETED})
  private String status;
  @Schema(description = "Task IDs that this task blocks")
  private List<Integer> addBlocks;
  @Schema(description = "Task IDs that block this task")
  private List<Integer> addBlockedBy;
}
