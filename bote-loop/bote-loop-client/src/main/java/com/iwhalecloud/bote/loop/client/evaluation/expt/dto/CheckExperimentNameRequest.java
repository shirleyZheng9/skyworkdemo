package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.SessionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 检查实验名称请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "检查实验名称请求")
public class CheckExperimentNameRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "实验名称")
  private String name;

  @Schema(description = "会话信息")
  private SessionDTO session;

  @Schema(description = "基础信息")
  private Base base;
}
