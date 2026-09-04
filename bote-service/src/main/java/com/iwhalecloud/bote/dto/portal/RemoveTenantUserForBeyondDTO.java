package com.iwhalecloud.bote.dto.portal;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 移除租户成员请求 DTO（通过userCode，用于Beyond系统）
 *
 * @author auto
 * @since 2025-01-20
 */
@Getter
@Setter
@ToString
@Schema(description = "移除租户成员请求（通过userCode，用于Beyond系统）")
public class RemoveTenantUserForBeyondDTO {
  @Schema(description = "租户 ID", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long tenantId;

  @Schema(description = "用户编码列表", requiredMode = Schema.RequiredMode.REQUIRED)
  private List<String> userCodes;
}

