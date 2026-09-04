package com.iwhalecloud.bote.dto.dashboard.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 数据概览看板租户查询参数。
 * @author zhengxueli
 * @since 2026-08-28
 */
@Getter
@Setter
@ToString
@Schema(description = "数据概览看板租户查询参数")
public class DashboardTenantQuery {

  /** 当前团队/租户 ID。 */
  @NotNull(message = "租户 ID 不能为空")
  @Schema(description = "当前团队/租户 ID", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long tenantId;
}
