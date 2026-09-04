package com.iwhalecloud.bote.dto.portal.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 外部门户查询条件
 *
 * @author bianjp
 * @since 2025-02-24
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "外部门户查询条件")
public class ExternalPortalQueryParams extends PagingQueryParams {
  @Schema(description = "关键字（模糊搜索门户名称、门户编码")
  private String keyword;
  @Schema(description = "门户类型")
  private String portalType;
  @Schema(description = "是否启用")
  private Boolean enabled;
  @Schema(description = "租户 ID")
  private Long tenantId;
}
