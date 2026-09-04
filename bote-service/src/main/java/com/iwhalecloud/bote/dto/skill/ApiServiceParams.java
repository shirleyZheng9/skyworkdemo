package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * API 服务参数
 *
 * @author bianjp
 * @since 2024-12-16
 */
@Getter
@Setter
@ToString
@Schema(description = "API 服务参数")
public class ApiServiceParams {
  @Schema(description = "请求路径参数")
  private Map<String, Object> path;
  @Schema(description = "请求头")
  private Map<String, Object> header;
  @Schema(description = "URL 参数")
  private Map<String, Object> query;
  @Schema(description = "请求体")
  private Object body;
}
