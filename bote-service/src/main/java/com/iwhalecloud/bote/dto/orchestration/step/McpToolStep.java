package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

/**
 * MCP 工具步骤
 *
 * @author bianjp
 * @since 2025-05-23
 */
@Getter
@Setter
public class McpToolStep extends AbstractStep {
  /** 是否是平台级服务 */
  private Boolean platform;
  /** MCP 服务 ID */
  private Long serverId;
  /** 工具名称 */
  private String toolName;
  /** 入参 */
  private ParameterSpec parameters;

  public McpToolStep() {
    super(StepType.MCP_TOOL);
  }
}
