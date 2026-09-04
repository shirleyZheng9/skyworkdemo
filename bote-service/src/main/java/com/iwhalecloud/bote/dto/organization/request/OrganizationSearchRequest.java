package com.iwhalecloud.bote.dto.organization.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 组织搜索请求
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
@Getter
@Setter
@ToString
@Schema(description = "组织搜索请求")
public class OrganizationSearchRequest {
  @Schema(description = "企业ID")
  @NotNull(message = "企业空间ID不能为空")
  private Long spaceId;
  @Schema(description = "关键词搜索")
  private String keywords;
  @Schema(description = "组织类型")
  private String orgType;
  @Schema(description = "上级组织ID")
  private Long parentOrgId;
  @Schema(description = "状态")
  private String statusCd;
  @Schema(description = "页码")
  private Integer page;
  @Schema(description = "每页大小")
  private Integer size;
}
