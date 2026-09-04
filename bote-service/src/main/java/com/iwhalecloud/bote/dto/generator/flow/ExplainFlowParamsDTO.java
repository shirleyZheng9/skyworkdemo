package com.iwhalecloud.bote.dto.generator.flow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 解释流程请求参数
 *
 * @author bianjp
 * @since 2025-03-31
 */
@Getter
@Setter
@ToString
@Schema(description = "解释流程请求参数")
public class ExplainFlowParamsDTO {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "流程 ID")
  private Long flowId;
  @Schema(description = "客户端 ID(流式接口中断请求使用)")
  private String clientId;
}
