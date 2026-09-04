package com.iwhalecloud.bote.dto.organization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 简单组织信息
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
@Getter
@Setter
@ToString
@Schema(description = "简单组织信息")
public class SimpleOrganizationDTO {
  @Schema(description = "组织ID")
  private Long orgId;
  @Schema(description = "企业空间ID")
  private Long spaceId;
  @Schema(description = "组织编码")
  private String orgCode;
  @Schema(description = "组织名称")
  private String orgName;
  @Schema(description = "组织类型")
  private String orgType;
  @Schema(description = "上级组织ID")
  private Long parentOrgId;
  @Schema(description = "组织层级")
  private Integer orgLevel;
  @Schema(description = "组织路径")
  private String orgPath;
  @Schema(description = "状态")
  private String statusCd;
  @Schema(description = "排序")
  private Integer sortOrder;
  @Schema(description = "扩展字段")
  private Map<String, Object> extFields;
  @Schema(description = "子组织列表")
  private List<SimpleOrganizationDTO> children;
  @JsonIgnore
  private String extFieldsJson;

  /**
   * 解析扩展字段JSON
   */
  public void parseExtFields() {
    if (StringUtils.isNotEmpty(extFieldsJson)) {
      this.extFields = JsonUtil.parseJson(extFieldsJson, new TypeReference<Map<String, Object>>() { });
    }
    this.extFieldsJson = null;
  }
}
