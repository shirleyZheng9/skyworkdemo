package com.iwhalecloud.bote.dto.a2a.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A2A 服务查询参数
 *
 * @author bianjp
 * @since 2025-09-08
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "A2A 服务查询参数")
public class A2aAgentQueryParams extends PagingQueryParams {
  @Schema(description = "租户 ID", requiredMode = RequiredMode.REQUIRED)
  private Long tenantId;
  @Schema(description = "平台 ID")
  private Long platformId;
  @Schema(description = "目录 ID")
  private Long catalogItemId;
  @Schema(description = "目录 ID 列表，后端查询数据库使用，包含前端所传目录及其子目录", hidden = true)
  private List<Long> catalogItemIds;
  @Schema(description = "模糊查询名称、描述")
  private String searchContent;
}
