package com.iwhalecloud.bote.dto.a2a.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A2A 平台查询参数
 *
 * @author bianjp
 * @since 2025-09-08
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "A2A 平台查询参数")
public class A2aPlatformQueryParams extends PagingQueryParams {
  @Schema(description = "租户 ID", requiredMode = RequiredMode.REQUIRED)
  private Long tenantId;
  @Schema(description = "模糊查询平台名称、编码")
  private String searchContent;
}
