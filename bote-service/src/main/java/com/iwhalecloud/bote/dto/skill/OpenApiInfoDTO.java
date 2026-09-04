package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * api详情实体
 *
 * @author qian.sisheng
 * @since 2024/10/15
 */
@Getter
@Setter
@ToString
public class OpenApiInfoDTO {
  @Schema(description = "服务请求地址")
  private String path;
  @Schema(description = "服务请求类型")
  private String serviceMethod;
  @Schema(description = "服务备注")
  private String serviceName;
}
