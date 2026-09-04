package com.iwhalecloud.bote.mcp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.mcp.dto.McpTool;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 查询工具列表结果
 *
 * @author bianjp
 * @since 2025-05-22
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class ListToolsResult {
  /** 工具列表 */
  private List<McpTool> tools;
  /** 下一页游标 */
  private String nextCursor;
}
