package com.iwhalecloud.bote.doc.module.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单知识
 *
 * @author bianjp
 * @since 2024-12-18
 */
@Getter
@Setter
@ToString
public class SimpleKnowledgeBaseDTO {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "空间 ID")
  private Long spaceId;
  @Schema(description = "知识库 ID")
  private Long knowledgeId;
  @Schema(description = "知识库名称")
  private String knowledgeName;
  @Schema(description = "知识库描述")
  private String knowledgeDesc;
  @Schema(description = "知识库图标")
  private String knowledgeIcon;
  @Schema(description = "知识库类型")
  private String knowledgeType;
  @Schema(description = "是否置顶")
  private Boolean isTopPinned;
  @Schema(description = "权限类型：MANAGE-可管理，EDIT-可编辑，READ-只读")
  private String permissionType;
  @Schema(description = "可见范围：PRIVATE-私有，PUBLIC-全员可见，MEMBERS-成员可见")
  private String visibilityScope;
  @Schema(description = "颜色")
  private String color;
  @Schema(description = "是否收藏")
  private Boolean isFavorite;
  @Schema(description = "创建时间")
  private Date createdTime;
  @Schema(description = "知识库所有者")
  private Long ownerId;

  @JsonIgnore
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("tenantId", tenantId);
    map.put("knowledgeId", knowledgeId);
    map.put("knowledgeName", knowledgeName);
    map.put("knowledgeDesc", knowledgeDesc);
    map.put("knowledgeIcon", knowledgeIcon);
    map.put("knowledgeType", knowledgeType);
    map.put("isTopPinned", isTopPinned);
    map.put("permissionType", permissionType);
    map.put("visibilityScope", visibilityScope);
    map.put("color", color);
    map.put("isFavorite", isFavorite);
    return map;
  }
}
