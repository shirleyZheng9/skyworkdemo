package com.iwhalecloud.bote.doc.module.control.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.iwhalecloud.bote.doc.common.support.serializer.NullBooleanSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档节点信息
 *
 * @author Aiqing
 * @since 2025-08-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Node View")
public class NodeInfoVo extends BaseNodeInfo {

  @Schema(description = "文档库ID", example = "lbr09")
  private String libraryId;

  @Schema(description = "父节点", example = "nod10")
  private String parentId;

  @Schema(description = "前一个节点", example = "nod11")
  private String preNodeId;

  @Schema(description = "节点图标", example = ":smile")
  private String icon;

  @Schema(description = "是否有子节点", example = "true")
  private Boolean hasChildren;

  @Schema(description = "是否是收藏的文档")
  @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
  private Boolean nodeFavorite;

  @Schema(description = "是否是置顶的文档")
  @JsonSerialize(nullsUsing = NullBooleanSerializer.class)
  private Boolean nodePined;

  @Schema(description = "该文档节点是否已设置权限")
  private Boolean nodePermitSet;

  @Schema(description = "创建时间", type = "string", example = "2020-03-18T15:29:59.000")
  private Date createdTime;

  @Schema(description = "更新时间", type = "string", example = "2020-03-18T15:29:59.000")
  private Date updatedTime;

  @Schema(description = "角色编码", example = "editor")
  private String role;

  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "空间ID")
  private Long spaceId;

  @Schema(description = "创建人id")
  private Long creatorId;

  @Schema(description = "创建人名称")
  private String creatorName;

}
