package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * API 协议信息
 *
 * @author auto
 * @since 2025-01-22
 */
@Getter
@Setter
@ToString
public class ApiDTO {
  @Schema(description = "主键")
  private Long apiId;
  @Schema(description = "名称")
  private String apiName;
  @Schema(description = "描述")
  private String apiDesc;
  @Schema(description = "请求路径")
  private String relativePath;
  @Schema(description = "请求类型")
  private String reqMethod;
  @Schema(description = "API 详情")
  private String apiDetail;
  @Schema(description = "path 入数")
  private String pathJson;
  @Schema(description = "query 入参")
  private String queryJson;
  @Schema(description = "body 入参")
  private String bodyJson;
  @Schema(description = "响应参数")
  private String responseJson;
  @Schema(description = "示例")
  private String exampleJson;
  @Schema(description = "头部 入参")
  private String headerJson;
  @Schema(description = "目录 ID")
  private Long catalogItemId;
  @Schema(description = "是否 SSE")
  private String isSse;
  @Schema(description = "目录名称")
  private String catalogName;
}
