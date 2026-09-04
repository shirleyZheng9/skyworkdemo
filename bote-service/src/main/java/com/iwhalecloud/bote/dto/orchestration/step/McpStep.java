package com.iwhalecloud.bote.dto.orchestration.step;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.dto.orchestration.step.AgentStep.FileProcessingStrategy;
import com.iwhalecloud.bote.mcp.client.McpClient;
import lombok.Getter;
import lombok.Setter;

/**
 * MCP 技能步骤
 *
 * <p>虚拟步骤，用于调用 MCP 工具</p>
 *
 * @author bianjp
 * @since 2025-05-23
 */
@Getter
@Setter
public class McpStep extends AbstractStep {
  /** MCP 服务 ID */
  private Long serverId;
  /** 工具名称 */
  private String toolName;
  /** 文件处理策略 */
  private FileProcessingStrategy fileProcessingStrategy;
  /** MCP 客户端实例 */
  @JsonIgnore
  private McpClient client;

  public McpStep() {
    super(StepType.MCP);
  }
}
