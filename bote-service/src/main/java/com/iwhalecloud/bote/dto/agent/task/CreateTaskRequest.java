package com.iwhalecloud.bote.dto.agent.task;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 创建任务请求
 *
 * @author bianjp
 * @since 2026-04-10
 */
@Getter
@Setter
@ToString
@Schema(description = "创建任务请求")
public class CreateTaskRequest {
  @Schema(description = "A brief title for the task", requiredMode = RequiredMode.REQUIRED)
  private String subject;
  @Schema(description = "What needs to be done", requiredMode = RequiredMode.REQUIRED)
  private String description;
  @Schema(description = "Present continuous form shown in spinner when in_progress (e.g., \"Running tests\")")
  private String activeForm;
}
