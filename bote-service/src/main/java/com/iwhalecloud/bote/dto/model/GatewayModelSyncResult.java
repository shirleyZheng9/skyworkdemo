package com.iwhalecloud.bote.dto.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 天工 AI 网关模型同步结果
 *
 * @author jiangm
 * @since 2026-08-06
 */
@Getter
@Setter
@ToString
public class GatewayModelSyncResult {
  @Schema(description = "新增数量")
  private int added;
  @Schema(description = "更新数量")
  private int updated;
  @Schema(description = "网关已下线标记数量（项目内保留）")
  private int offlineMarked;
}
