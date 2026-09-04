package com.iwhalecloud.bote.mcp.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.mcp.consts.LoggingLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 设置日志级别请求
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
public class SetLevelRequest {
  /** 日志级别 */
  private LoggingLevel level;
}
