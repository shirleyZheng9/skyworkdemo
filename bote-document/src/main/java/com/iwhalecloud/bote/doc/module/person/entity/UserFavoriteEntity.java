package com.iwhalecloud.bote.doc.module.person.entity;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户收藏
 *
 * @author yangran
 * @since 2025-08-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_dc_user_favorite")
@Schema(hidden = true)
public class UserFavoriteEntity extends BaseEntity {
  @Id
  @DiffId
  @Schema(description = "收藏ID")
  private Long favoriteId;
  @DiffField(name = "user_id")
  @Schema(description = "用户ID")
  private Long userId;
  @DiffField(name = "target_id")
  @Schema(description = "目标ID（文档ID或文档库ID或知识库ID）")
  private String targetId;
  @DiffField(name = "target_type")
  @Schema(description = "目标类型：DOCUMENT-文档，FOLDER-文件夹，LIBRARY-文档库，KNOWLEDGE-知识库")
  private String targetType;
  @DiffField(name = "is_favorite_pinned")
  @Schema(description = "收藏列表中是否置顶：F-否，T-是")
  private String isFavoritePinned;
  @DiffField(name = "favorite_pin_order")
  @Schema(description = "收藏列表置顶排序：置顶时为1-5，非置顶时为0")
  private Integer favoritePinOrder;
  @DiffField(name = "tenant_id")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "space_id")
  @Schema(description = "企业空间ID")
  private Long spaceId;
}
