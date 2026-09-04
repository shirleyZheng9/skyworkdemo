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
 * 组织树节点
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
@Getter
@Setter
@ToString
@Schema(description = "组织树节点")
public class OrganizationTreeNodeDTO {
  @Schema(description = "组织ID")
  private Long orgId;
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
  @Schema(description = "成员数量")
  private Integer memberCount;
  @Schema(description = "状态")
  private String statusCd;
  @Schema(description = "排序")
  private Integer sortOrder;
  @Schema(description = "扩展字段")
  private Map<String, Object> extFields;
  @Schema(description = "子组织列表")
  private List<OrganizationTreeNodeDTO> children;
  @Schema(description = "是否展开")
  private Boolean expanded;
  @Schema(description = "是否有子节点")
  private Boolean hasChildren;
  @Schema(description = "是否叶子节点")
  private Boolean isLeaf;
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

  /**
   * 判断是否为叶子节点
   */
  public void setIsLeaf() {
    this.isLeaf = (children == null || children.isEmpty());
  }
}
