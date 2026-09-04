package com.iwhalecloud.bote.mcp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.mcp.dto.Implementation;
import com.iwhalecloud.bote.mcp.dto.ServerCapabilities;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 初始化结果
 *
 * @author bianjp
 * @since 2025-05-22
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class InitializeResult {
  /** 协议版本 */
  private String protocolVersion;
  /** 服务器功能 */
  private ServerCapabilities capabilities;
  /** 服务器信息 */
  private Implementation serverInfo;
  /** 指令 */
  private String instructions;
}
