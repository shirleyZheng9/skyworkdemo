package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 服务模拟参数
 *
 * @author qian.sisheng
 * @since 2025-09-17
 */
@Getter
@Setter
@ToString
public class ServiceMockParams {
  @Schema(description = "api服务ID列表，不传递时，更新全部服务")
  private List<Long> serviceIds;
  @Schema(description = "是否mock, T/F")
  private String isMock;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "更新人ID")
  private Long updatorId;
}
