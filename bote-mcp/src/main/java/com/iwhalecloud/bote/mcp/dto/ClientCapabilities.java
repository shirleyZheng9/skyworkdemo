package com.iwhalecloud.bote.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.mcp.dto.capability.RootCapabilities;
import com.iwhalecloud.bote.mcp.dto.capability.Sampling;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 客户端功能
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
public class ClientCapabilities {
  /** 空的客户端功能对象，不支持任何功能 */
  public static final ClientCapabilities EMPTY = new ClientCapabilities();

  /** 实验特性 */
  private Map<String, Object> experimental;
  /** 根目录能力 */
  private RootCapabilities roots;
  /** 采样能力 */
  private Sampling sampling;
}
