package com.iwhalecloud.bote.mcp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.mcp.dto.Content;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 调用工具结果
 *
 * @author bianjp
 * @since 2025-05-22
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class CallToolResult {
  /** 是否错误(为空表示成功) */
  private Boolean isError;
  /** 内容列表 */
  private List<Content> content;
}
