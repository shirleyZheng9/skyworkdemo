package com.iwhalecloud.bote.doc.module.person.dto.favorite;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.UserInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 收藏列表 DTO
 *
 * @author lizuyin
 * @since 2025-08-21
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "收藏列表")
public class FavoriteListDTO {
  @Schema(description = "收藏ID")
  private Long favoriteId;
  @Schema(description = "目标ID")
  private String targetId;
  @Schema(description = "目标类型：DOCUMENT/FOLDER/LIBRARY/KNOWLEDGE")
  private String targetType;
  @Schema(description = "资源名称")
  private String resourceName;
  @Schema(description = "资源编码-在查回KNOWLEDGE时,用于装载knowledge_id")
  private Long resourceCode;
  @Schema(description = "资源类型")
  private String resourceType;
  @Schema(description = "查询文档时返回文档库id供前端跳转")
  private String libraryId;
  @Schema(description = "内容来源，仅文档/文件夹有值")
  private String contentSource;
  @Schema(description = "资源路径，仅文档有值")
  private String resourcePath;
  @Schema(description = "资源路径编码，仅文档有值")
  private String resourcePathCode;
  @Schema(description = "创建人")
  private UserInfo creator;
  @Schema(description = "收藏时间")
  private Date favoriteTime;
  @Schema(description = "置顶时间，未置顶时为空")
  private Date pinTime;
  @Schema(description = "是否置顶")
  private Boolean isTopPinned;
  @Schema(description = "是否收藏")
  private Boolean isFavorite;
  @Schema(description = "权限，仅文档固定为EDIT")
  private String permissions;
  @Schema(description = "资源图标，仅文档库有值")
  private String resourceIcon;
  @Schema(description = "图标颜色")
  private String color;
  @Schema(description = "文档数量，仅文档库有值")
  private Integer documentCount;

  // 隐藏字段，用于内部组装
  @Schema(hidden = true)
  private Long creatorUserId;
  @Schema(hidden = true)
  private String isFavoritePinned;

  @Schema(description = "租户ID")
  protected Long tenantId;

  @Schema(description = "企业空间ID")
  protected Long spaceId;

  @Schema(description = "是否是我的文档库文档：T为是")
  private String myDoc;
}
