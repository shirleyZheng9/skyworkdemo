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
 * 组织字段配置
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_organization_field_config")
public class OrganizationFieldConfigEntity extends BaseEntity {
  @Id
  @DiffId
  @Schema(description = "主键")
  private Long configId;
  @DiffField(name = "space_id")
  @Schema(description = "企业空间ID")
  private Long spaceId;
  @DiffField(name = "field_key")
  @Schema(description = "字段键名")
  private String fieldKey;
  @DiffField(name = "field_name")
  @Schema(description = "字段名称")
  private String fieldName;
  @DiffField(name = "field_type")
  @Schema(description = "字段类型", allowableValues = {"text", "textarea", "number", "date", "boolean", "select", "multi_select"})
  private String fieldType;
  @DiffField(name = "field_options")
  @Schema(description = "字段选项JSON")
  protected String fieldOptions;
  @DiffField(name = "status_cd")
  @Schema(description = "状态", allowableValues = {"00A", "00X"})
  private String statusCd;
}
