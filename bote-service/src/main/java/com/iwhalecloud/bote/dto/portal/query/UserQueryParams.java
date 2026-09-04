package com.iwhalecloud.bote.dto.portal.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户查询参数
 *
 * @author auto
 * @since 2024-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
public class UserQueryParams extends PagingQueryParams {
  @Schema(description = "用户名称")
  private String userName;
  @Schema(description = "系统编码")
  private String systemCode;
  @Schema(description = "用户ID")
  private Long userId;
  @Schema(description = "创建租户ID")
  private Long creatTenantId;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "企业空间ID")
  private Long spaceId;
  @Schema(description = "过滤组织ID")
  private Long excludeOrgId;
}
