package com.iwhalecloud.bote.entity.skill;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.Size;

/**
 * 技能：属性 Entity
 *
 * @author auto
 * @since 2024-09-15
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_skill_attr_spec")
public class SkillAttrSpecEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long attrId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
  @DiffField(name = "PAR_ATTR_ID")
  @Schema(description = "上级属性ID")
  private Long parAttrId;
  @DiffField(name = "DATA_TYPE")
  @Schema(description = "数据类型")
  private String dataType;
  @DiffField(name = "ATTR_NAME")
  @Schema(description = "属性名称")
  @Size(max = 20, message = "属性名称超过限定长度20")
  private String attrName;
  @DiffField(name = "ATTR_NBR")
  @Schema(description = "属性编码")
  @Size(max = 50, message = "属性编码超过限定长度50")
  private String attrNbr;
  @DiffField(name = "DEFAULT_VALUE")
  @Schema(description = "默认取值")
  private String defaultValue;
  @DiffField(name = "ATTR_DESC")
  @Schema(description = "属性描述")
  private String attrDesc;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录ID")
  private Long catalogItemId;
}
