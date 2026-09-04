package com.iwhalecloud.bote.dto.orchestration.test;

import com.iwhalecloud.bote.dto.base.ParameterSpec;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 提取节点参数结果
 *
 * @author bianjp
 * @since 2025-08-05
 */
@Getter
@Setter
@ToString
@Schema(description = "提取节点参数结果")
public class ExtractNodeParamsResultDTO {
  @Schema(description = "参数规格")
  private List<ParameterSpec> specs;
  @Schema(description = "参数的 JSON 格式")
  private Map<String, Object> params;
}
