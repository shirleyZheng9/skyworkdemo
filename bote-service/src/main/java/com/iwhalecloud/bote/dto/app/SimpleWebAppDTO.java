package com.iwhalecloud.bote.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 网页应用简单信息
 *
 * @author chen.linfa
 * @since 2025-09-15
 */
@Getter
@Setter
@ToString
public class SimpleWebAppDTO {
  @Schema(description = "主键")
  private Long webAppId;
  @Schema(description = "应用名称")
  private String appName;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "鉴权类型")
  private String authType;
  @Schema(description = "打开方式")
  private String openType;
  @Schema(description = "访问网址")
  private String accessUrl;
  @Schema(description = "企业空间ID")
  private Long spaceId;
}
