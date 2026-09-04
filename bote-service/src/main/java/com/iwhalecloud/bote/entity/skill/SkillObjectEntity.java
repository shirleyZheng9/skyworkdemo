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
 * 技能：对象 Entity
 *
 * @author auto
 * @since 2024-09-16
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_skill_object")
public class SkillObjectEntity extends BaseEntity {
  @DiffId
  @Schema(description = "对象ID")
  private Long busiObjectId;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @DiffField(name = "BUSI_OBJECT_CODE")
  @Schema(description = "对象编码")
  private String busiObjectCode;
  @DiffField(name = "BUSI_OBJECT_NAME")
  @Schema(description = "对象名称")
  private String busiObjectName;
  @DiffField(name = "DATA_SOURCE_ID")
  @Schema(description = "数据源ID")
  private Long dataSourceId;
  @DiffField(name = "ATTR_JSON")
  @Schema(description = "业务对象属性")
  private String attrJson;
  @Schema(description = "租户ID")
  @DiffField(name = "TENANT_ID")
  private Long tenantId;
}
