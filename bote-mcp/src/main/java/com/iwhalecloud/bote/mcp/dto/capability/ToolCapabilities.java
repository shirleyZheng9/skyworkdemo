package com.iwhalecloud.bote.mcp.dto.capability;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工具能力
 *
 * @author bianjp
 * @since 2025-05-22
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class ToolCapabilities {
  /** 工具列表变化时是否发送通知 */
  private Boolean listChanged;
}
