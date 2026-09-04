package com.iwhalecloud.bote.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.mcp.dto.capability.CompletionCapabilities;
import com.iwhalecloud.bote.mcp.dto.capability.LoggingCapabilities;
import com.iwhalecloud.bote.mcp.dto.capability.PromptCapabilities;
import com.iwhalecloud.bote.mcp.dto.capability.ResourceCapabilities;
import com.iwhalecloud.bote.mcp.dto.capability.ToolCapabilities;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MCP 服务器功能
 *
 * @author bianjp
 * @since 2025-05-22
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class ServerCapabilities {
  /** 实现特性 */
  private Map<String, Object> experimental;
  /** 补全 */
  private CompletionCapabilities completions;
  /** 日志 */
  private LoggingCapabilities logging;
  /** 提示词 */
  private PromptCapabilities prompts;
  /** 资源 */
  private ResourceCapabilities resources;
  /** 工具 */
  private ToolCapabilities tools;

  @Override
  public String toString() {
    return JsonUtil.toJsonStringCompact(this);
  }
}
