package com.iwhalecloud.bote.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MCP 客户端/服务器信息
 *
 * @author bianjp
 * @since 2025-05-22
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Implementation {
  /** 名称 */
  private String name;
  /** 版本 */
  private String version;

  @Override
  public String toString() {
    return name + "/" + version;
  }
}
