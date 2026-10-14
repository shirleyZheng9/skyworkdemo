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
 * 资源链接内容（MCP 协议 2025-06-18 版本引入）
 *
 * @author zhusenliang
 * @since 2026-09-08
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class ResourceLinkContent extends Content {
  /** 听众角色列表 */
  private List<McpAudienceRole> audience;
  /** 优先级 */
  private Double priority;
  /** 资源 URI */
  private String uri;
  /** 资源名称 */
  private String name;
  /** 描述 */
  private String description;
  /** 媒体类型 */
  private String mimeType;
  /** 大小（字节数） */
  private Long size;

  public ResourceLinkContent() {
    super(McpConsts.CONTENT_TYPE_RESOURCE_LINK);
  }
}
