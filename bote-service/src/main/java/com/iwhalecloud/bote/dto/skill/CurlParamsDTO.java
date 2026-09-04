package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * curl参数
 *
 * @author qian.sisheng
 * @since 2025-09-15
 */
@Getter
@Setter
@ToString
public class CurlParamsDTO {
  @Schema(description = "curl命令")
  private String curlCommand;
  @Schema(description = "忽略通用header")
  private Boolean ignoreCommonHeaders;
}
