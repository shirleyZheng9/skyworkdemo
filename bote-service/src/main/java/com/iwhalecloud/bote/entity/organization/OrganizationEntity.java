package com.iwhalecloud.bote.entity.organization;

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
 * 组织
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_organization")
@Schema(hidden = true)
public class OrganizationEntity extends BaseEntity {
  @Id
  @DiffId
  @Schema(description = "主键")
  private Long orgId;
  @DiffField(name = "space_id")
  @Schema(description = "租户ID")
  private Long spaceId;
  @DiffField(name = "org_code")
  @Schema(description = "组织编码")
  private String orgCode;
  @DiffField(name = "org_name")
  @Schema(description = "组织名称")
  private String orgName;
  @DiffField(name = "org_type")
  @Schema(description = "组织类型", allowableValues = {"department", "company", "team"})
  private String orgType;
  @DiffField(name = "parent_org_id")
  @Schema(description = "父组织ID")
  private Long parentOrgId;
  @DiffField(name = "org_level")
  @Schema(description = "组织层级")
  private Integer orgLevel;
  @DiffField(name = "org_path")
  @Schema(description = "组织路径")
  private String orgPath;
  @DiffField(name = "org_description")
  @Schema(description = "组织描述")
  private String orgDescription;
  @DiffField(name = "status_cd")
  @Schema(description = "组织状态", allowableValues = {"active", "inactive"})
  private String statusCd;
  @DiffField(name = "sort_order")
  @Schema(description = "排序顺序")
  private Integer sortOrder;
  @DiffField(name = "ext_fields")
  @Schema(description = "扩展字段JSON")
  private String extFields;
}
