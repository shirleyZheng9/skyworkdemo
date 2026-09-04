package com.iwhalecloud.bote.entity.base;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 关联标签 Entity
 *
 * @author auto
 * @since 2024-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_label_object_rel")
public class LabelObjectRelEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long relId;
  @DiffField(name = "LABEL_ID")
  @Schema(description = "标签 ID")
  private Long labelId;
  @DiffField(name = "OBJECT_ID", parent = true)
  @Schema(description = "对象 ID")
  private Long objectId;
  @DiffField(name = "OBJECT_TYPE")
  @Schema(description = "对象类型")
  private String objectType;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
}
