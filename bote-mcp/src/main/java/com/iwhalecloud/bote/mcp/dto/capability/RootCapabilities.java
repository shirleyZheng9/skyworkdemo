package com.iwhalecloud.bote.mcp.dto.capability;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 根目录能力
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
public class RootCapabilities {
  /** 根目录列表变化时是否发送通知 */
  private Boolean listChanged;
}
