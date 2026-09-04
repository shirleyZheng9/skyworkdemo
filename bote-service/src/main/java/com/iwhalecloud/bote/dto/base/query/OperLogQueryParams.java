package com.iwhalecloud.bote.dto.base.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 操作日志查询参数
 *
 * @author auto
 * @since 2024-10-26
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "操作日志查询参数")
public class OperLogQueryParams extends PagingQueryParams {
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "日志类型")
  private String operClass;
  @Schema(description = "业务对象ID")
  private Long objId;
  @Schema(description = "业务对象类型")
  private String operType;
}
