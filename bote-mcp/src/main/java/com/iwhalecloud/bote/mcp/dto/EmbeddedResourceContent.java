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
 * 嵌入资源内容
 *
 * @author zhusenliang
 * @since 2026-09-08
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class EmbeddedResourceContent extends Content {
  /** 听众角色列表 */
  private List<McpAudienceRole> audience;
  /** 优先级 */
  private Double priority;
  /** 资源内容 */
  private ResourceContent resource;

  public EmbeddedResourceContent() {
    super(McpConsts.CONTENT_TYPE_RESOURCE);
  }
}
