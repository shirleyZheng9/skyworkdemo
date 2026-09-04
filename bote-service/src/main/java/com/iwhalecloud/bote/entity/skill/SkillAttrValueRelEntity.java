package com.iwhalecloud.bote.entity.skill;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 属性值关联 Entity
 *
 * @author qian.sisheng
 * @since 2025-1-16
 */

@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_skill_attr_value_rel")
public class SkillAttrValueRelEntity extends BaseEntity {

  @DiffId
  @Schema(description = "属性值关联ID")
  private Long relId;

  @DiffField(required = false, parent = true, name = "A_ATTR_VALUE_ID")
  @Schema(description = "A端属性值ID")
  private Long aAttrValueId;

  @DiffField(required = false, root = true, name = "A_ATTR_ID")
  @Schema(description = "A端属性ID")
  private Long aAttrId;

  @DiffField(name = "Z_ATTR_VALUE_ID")
  @Schema(description = "Z端属性值ID")
  private Long zAttrValueId;

  @DiffField(name = "Z_ATTR_ID")
  @Schema(description = "Z端属性ID")
  private Long zAttrId;

  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户id")
  private Long tenantId;
}
