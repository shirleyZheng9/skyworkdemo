package com.iwhalecloud.bote.dto.base.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * API 鉴权查询参数
 *
 * @author auto
 * @since 2024-09-19
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "API 鉴权查询参数")
public class ApiAuthQueryParams extends PagingQueryParams {
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "租户 ID")
  private Long tenantId;
}
