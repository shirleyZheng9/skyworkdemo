package com.iwhalecloud.bote.doc.module.person.dto.homepage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.doc.common.utils.PinLinkedSortHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 置顶文档库
 *
 * @author yangran
 * @since 2025-01-06
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "置顶文档库")
public class PinnedLibraryDTO implements PinLinkedSortHelper.LinkedListNode<PinnedLibraryDTO> {
  @Schema(description = "置顶记录ID")
  private Long pinId;
  @Schema(description = "前一个置顶记录ID")
  private Long prevPinId;
  @Schema(description = "文档库ID")
  private String libraryId;
  @Schema(description = "文档库名称")
  private String libraryName;
  @Schema(description = "文档库图标")
  private String libraryIcon;
  @Schema(description = "图标颜色")
  private String color;
  @Schema(description = "租户id")
  private String tenantId;
  @Schema(description = "描述")
  private String description;
  @Schema(description = "可见范围：PUBLIC-公开，MEMBERS-成员可见")
  private String visibilityScope;
  @Schema(description = "文档数量")
  private Integer documentCount;
  @Schema(description = "最后更新时间")
  private Date lastUpdateTime;
  @Schema(description = "最后更新人")
  private UserInfo lastUpdater;
  @Schema(description = "置顶时间")
  private Date pinTime;

  @Schema(description = "企业空间ID")
  protected Long spaceId;

  // 临时字段，用于存储用户信息
  @Schema(hidden = true)
  private Long lastUpdaterUserId;
  @Schema(hidden = true)
  private String lastUpdaterUsername;
}
