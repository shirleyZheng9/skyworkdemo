package com.iwhalecloud.bote.dto.mcp;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * mcp查询参数
 *
 * @author auto
 * @since 2025-05-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "mcp查询参数")
public class McpServerQueryParams extends PagingQueryParams {
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "服务类型")
  private String serverType;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "服务有效标识")
  private String serverEffect;
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "标签ID")
  private Long labelId;
  @Schema(description = "是否用于配置管理", example = "T/F")
  private String configFlag;
}
