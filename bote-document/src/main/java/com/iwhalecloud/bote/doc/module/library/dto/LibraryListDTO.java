package com.iwhalecloud.bote.doc.module.library.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.UserInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档库列表 DTO
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "文档库列表项")
public class LibraryListDTO {
  @Schema(description = "文档库ID")
  private String libraryId;
  @Schema(description = "文档库名称")
  private String libraryName;
  @Schema(description = "描述")
  private String description;
  @Schema(description = "文档库图标")
  private String libraryIcon;
  @Schema(description = "文档库图标颜色")
  private String color;
  @Schema(description = "可见范围：PUBLIC/MEMBERS/PRIVATE/OTHER")
  private String visibilityScope;
  @Schema(description = "创建人")
  private UserInfo creator;
  @Schema(description = "拥有者")
  private UserInfo owner;
  @Schema(description = "成员数量（去重subject_id计数）")
  private Integer memberCount;
  @Schema(description = "文档数量")
  private Integer documentCount;
  @Schema(description = "创建时间")
  private Date createdTime;
  @Schema(description = "更新时间")
  private Date updatedTime;
  @Schema(description = "最后访问时间")
  private Date lastAccessTime;
  @Schema(description = "当前登录用户对该库的权限：MANAGE/EDIT/READ/NULL")
  private String permissions;
  @Schema(description = "是否共享给我（subject_type=USER 且 subject_id=当前用户）")
  private Boolean isSharedToMe;
  @Schema(description = "共享人")
  private UserInfo sharedBy;
  @Schema(description = "是否已收藏")
  private Boolean isFavorite;
  @Schema(description = "是否置顶")
  private Boolean isPin;
  // 内部装配字段
  @Schema(hidden = true)
  private Long creatorUserId;
  @Schema(hidden = true)
  private Long ownerUserId;
  @Schema(hidden = true)
  private Long sharedByUserId;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "空间ID")
  private Long spaceId;
}


