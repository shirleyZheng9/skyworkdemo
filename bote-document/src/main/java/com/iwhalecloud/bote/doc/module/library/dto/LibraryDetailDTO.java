package com.iwhalecloud.bote.doc.module.library.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.UserInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档库详情 DTO
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "文档库详情")
public class LibraryDetailDTO {
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
  @Schema(description = "成员数量")
  private Integer memberCount;
  @Schema(description = "文档数量")
  private Integer documentCount;
  @Schema(description = "创建时间")
  private Date createdTime;
  @Schema(description = "更新时间")
  private Date updatedTime;
  @Schema(description = "当前登录用户对该库的权限：MANAGE/EDIT/READ/NULL")
  private String permissions;
  @Schema(description = "是否已收藏：true-已收藏，false-未收藏")
  private Boolean isFavorite;
  @Schema(description = "成员列表")
  private List<MemberInfo> members;
  // 内部装配字段
  @Schema(hidden = true)
  private Long creatorUserId;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "空间ID")
  private Long spaceId;

  @Getter
  @Setter
  @ToString
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Schema(description = "文档库成员信息")
  public static class MemberInfo {
    @Schema(description = "主体类型：USER/ORG")
    private String subjectType;
    @Schema(description = "主体ID")
    private Long subjectId;
    @Schema(description = "主体名称")
    private String subjectName;
    @Schema(description = "头像（暂空）")
    private String avatar;
    @Schema(description = "权限类型：MANAGE/EDIT/READ")
    private String permissionType;
    @Schema(description = "权限ID")
    private Long permissionId;
    @Schema(description = "是否为文档库所有者")
    private Boolean isOwner;
    @Schema(description = "授权人ID")
    private Long grantedBy;
    @Schema(description = "授权人名称")
    private String grantedByName;
    @Schema(description = "授权时间")
    private Date grantedTime;
  }
}


