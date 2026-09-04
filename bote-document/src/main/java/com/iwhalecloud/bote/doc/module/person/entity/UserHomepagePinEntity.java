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
 * 用户首页置顶
 *
 * @author yangran
 * @since 2025-08-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_dc_user_homepage_pin")
@Schema(hidden = true)
public class UserHomepagePinEntity extends BaseEntity {
  @Id
  @DiffId
  @Schema(description = "置顶ID")
  private Long pinId;
  @DiffField(name = "user_id")
  @Schema(description = "用户ID")
  private Long userId;
  @DiffField(name = "target_id")
  @Schema(description = "目标ID（文档ID或文档库ID或知识库ID）")
  private String targetId;
  @DiffField(name = "target_type")
  @Schema(description = "目标类型：DOCUMENT-文档，LIBRARY-文档库，KNOWLEDGE-知识库")
  private String targetType;
  @DiffField(name = "prev_pin_id")
  @Schema(description = "前一个置顶项ID，用于快速排序")
  private Long prevPinId;
  @DiffField(name = "tenant_id")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "space_id")
  @Schema(description = "空间ID")
  private Long spaceId;
}
