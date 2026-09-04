package com.iwhalecloud.bote.mcp.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MCP 客户端配置
 *
 * @author bianjp
 * @since 2025-08-07
 */
@Component
@ConfigurationProperties("bote.mcp.client")
@Getter
@Setter
@ToString
public class McpClientProperties {
  /** 初始化超时时间(秒) */
  private int initializationTimeout = 20;
  /** 请求超时时间(秒) */
  private int requestTimeout = 60;
}
