package com.iwhalecloud.bote.dto.datasync.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 在线发布数据入参
 *
 * @author lizuyin
 * @since 2026-01-21
 */
@Getter
@Setter
@ToString(callSuper = true)
public class OnlinePublishParams extends ExportDataParams {
  @Schema(description = "网关ID，用于指定发布目标环境")
  private Long gatewayId;
  @Schema(description = "企业空间ID")
  private Long spaceId;
}
