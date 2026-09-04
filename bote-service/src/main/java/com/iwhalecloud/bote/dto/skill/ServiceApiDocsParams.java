package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 服务接口文档参数
 *
 * @author qian.sisheng
 * @since 2024/10/15
 */
@Getter
@Setter
@ToString
public class ServiceApiDocsParams {
  @Schema(description = "api-docs 地址")
  private String serviceDocUrl;
  @Schema(description = "openApiJson 数据")
  private String openApiJson;
  @Schema(description = "目录 ID")
  private Long catalogItemId;
  @Schema(description = "平台ID")
  private Long platformId;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "api详情列表")
  private List<OpenApiInfoDTO> openApiInfoList;
}
