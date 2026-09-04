package com.iwhalecloud.bote.dto.base.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 环境变量查询参数
 *
 * @author qian.sisheng
 * @since 2025-11-03
 */
@Setter
@Getter
@ToString(callSuper = true)
@Schema(description = "环境变量查询参数")
public class EnvVariableQueryParams extends PagingQueryParams {

  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "租户ID")
  private Long tenantId;
}
