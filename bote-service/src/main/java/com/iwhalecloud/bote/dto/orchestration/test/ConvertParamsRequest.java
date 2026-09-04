package com.iwhalecloud.bote.dto.orchestration.test;

import com.iwhalecloud.bote.dto.base.ParameterSpec;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 转换参数请求
 *
 * @author bianjp
 * @since 2025-08-04
 */
@Getter
@Setter
@ToString
@Schema(description = "转换参数请求")
public class ConvertParamsRequest {
  @Schema(description = "参数的 JSON 格式")
  private Map<String, Object> params;
  @Schema(description = "参数规格")
  private List<ParameterSpec> specs;
}
