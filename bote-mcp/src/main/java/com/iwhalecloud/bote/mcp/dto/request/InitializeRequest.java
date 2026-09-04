package com.iwhalecloud.bote.mcp.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.mcp.dto.ClientCapabilities;
import com.iwhalecloud.bote.mcp.dto.Implementation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 初始化请求
 *
 * @author bianjp
 * @since 2025-05-22
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class InitializeRequest {
  /** 协议版本 */
  private String protocolVersion;
  /** 客户端功能 */
  private ClientCapabilities capabilities;
  /** 客户端信息 */
  private Implementation clientInfo;
}
