package com.iwhalecloud.bote.mcp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.mcp.consts.McpAudienceRole;
import com.iwhalecloud.bote.mcp.consts.McpConsts;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文本内容
 *
 * @author bianjp
 * @since 2025-05-22
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class TextContent extends Content {
  /** 听众角色列表 */
  private List<McpAudienceRole> audience;
  /** 优先级 */
  private Double priority;
  /** 文本 */
  private String text;

  public TextContent() {
    super(McpConsts.CONTENT_TYPE_TEXT);
  }
}
