package com.iwhalecloud.bote.dto.plugin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 新增门户用户入参
 *
 * @author chen.linfa
 * @since 2025-12-09
 */
@Getter
@Setter
@ToString
public class QueryCatalogRequest {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "目录类型")
  private String catalogType;
  @Schema(description = "目录名称")
  private String catalogName;
  @Schema(description = "目录ID")
  private Long catalogId;
}

