package com.iwhalecloud.bote.doc.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * 组织机构信息
 *
 * @author Aiqing
 * @since 2025/8/18
 */
@Getter
@Setter
@ToString
public class OrgDTO {

  @Schema(description = "组织ID")
  private Long orgId;
  @Schema(description = "组织名称")
  private String orgName;
  @Schema(description = "组织等级")
  private Integer orgLevel;
  @Schema(description = "父级组织ID")
  private Long parentOrgId;
  @Schema(description = "组织路径")
  private String orgPath;

}
