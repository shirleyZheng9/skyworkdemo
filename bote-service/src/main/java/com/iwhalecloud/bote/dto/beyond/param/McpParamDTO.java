package com.iwhalecloud.bote.dto.beyond.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * MCP参数DTO
 *
 * @author lizuyin
 * @since 2025-07-21
 */
@Getter
@Setter
@ToString
public class McpParamDTO {
  /** 传输类型 */
  private String mcpTransferType;
  /** 请求头 */
  private String mcpHeader;
  /** 命令 */
  private String mcpCommand;
  /** 参数 */
  private List<String> mcpArgs;
  /** 环境变量 */
  private Map<String, Object> mcpEnv;
  /** 超时时间(毫秒) */
  private Integer mcpTimeout;
  /** MCP服务地址 */
  private String mcpServerUrlOri;
}
