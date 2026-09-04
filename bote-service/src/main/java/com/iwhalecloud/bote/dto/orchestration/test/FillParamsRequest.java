package com.iwhalecloud.bote.dto.orchestration.test;

import com.iwhalecloud.bote.dto.base.ParameterSpec;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AI 填充参数请求
 *
 * @author bianjp
 * @since 2025-08-04
 */
@Getter
@Setter
@ToString
@Schema(description = "AI 填充参数请求")
public class FillParamsRequest {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "是否忽略现有参数，默认 false")
  private Boolean ignoreParams;
  @Schema(description = "参数的 JSON 格式")
  private Map<String, Object> params;
  @Schema(description = "参数规格")
  private List<ParameterSpec> specs;
}
