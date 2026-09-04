package com.iwhalecloud.bote.dto.publish;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 在线环境维护查询参数
 *
 * @author lizuyin
 * @since 2026-01-21
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "在线环境维护查询参数")
public class PublishGatewayQueryParams extends PagingQueryParams {
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "模糊查询（网关名称）")
  private String searchContent;
}

