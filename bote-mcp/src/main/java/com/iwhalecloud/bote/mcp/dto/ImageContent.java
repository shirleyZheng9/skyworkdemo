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
 * 图片内容
 *
 * @author bianjp
 * @since 2025-05-22
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class ImageContent extends Content {
  /** 听众角色列表 */
  private List<McpAudienceRole> audience;
  /** 优先级 */
  private Double priority;
  /** 数据内容 */
  private String data;
  /** 媒体类型 */
  private String mimeType;

  public ImageContent() {
    super(McpConsts.CONTENT_TYPE_IMAGE);
  }
}
