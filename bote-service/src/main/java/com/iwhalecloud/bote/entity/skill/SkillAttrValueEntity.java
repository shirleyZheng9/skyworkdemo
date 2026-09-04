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
 * 技能：属性值 Entity
 *
 * @author auto
 * @since 2024-09-15
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_skill_attr_value")
public class SkillAttrValueEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long attrValueId;
  @DiffField(name = "ATTR_ID", parent = true)
  @Schema(description = "属性 ID")
  private Long attrId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
  @DiffField(name = "ATTR_VALUE_NAME")
  @Schema(description = "名称")
  private String attrValueName;
  @DiffField(name = "ATTR_VALUE")
  @Schema(description = "属性值")
  private String attrValue;
  @DiffField(name = "SORTBY")
  @Schema(description = "排序")
  private Integer sortby;
  @DiffField(name = "ATTR_VALUE_CODE")
  @Schema(description = "属性值编码")
  private String attrValueCode;
  @DiffField(name = "ATTR_VALUE_DESC")
  @Schema(description = "描述")
  private String attrValueDesc;
}
