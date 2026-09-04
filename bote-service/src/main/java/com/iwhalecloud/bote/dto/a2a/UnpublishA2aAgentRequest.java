package com.iwhalecloud.bote.dto.a2a;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 取消发布 A2A 智能体请求参数
 *
 * @author bianjp
 * @since 2025-09-09
 */
@Getter
@Setter
@ToString
@Schema(description = "取消发布 A2A 智能体请求参数")
public class UnpublishA2aAgentRequest {
  @Schema(description = "租户 ID", requiredMode = RequiredMode.REQUIRED)
  private Long tenantId;
  @Schema(description = "系统编码", requiredMode = RequiredMode.REQUIRED)
  private String systemCode;
  @Schema(description = "智能体 ID", requiredMode = RequiredMode.REQUIRED)
  private String agentId;
}
