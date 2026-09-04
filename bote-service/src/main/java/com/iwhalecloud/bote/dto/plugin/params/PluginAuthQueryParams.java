package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 插件鉴权查询参数
 *
 * @author chen.linfa
 * @since 2025-12-09
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "插件鉴权查询参数")
public class PluginAuthQueryParams extends PagingQueryParams {
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "租户 ID")
  private Long tenantId;
}
