package com.iwhalecloud.bote.dto.base.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 账号事件日志查询参数
 *
 * @author tingyun.wang
 * @since 2025-07-18
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "账号事件日志查询参数")
public class AccountEventLogQueryParams extends PagingQueryParams {

  @Schema(description = "操作人姓名")
  private String operatorName;

  @Schema(description = "事件类型")
  private String eventType;

  @Schema(description = "事件编码")
  private String eventCode;

}
