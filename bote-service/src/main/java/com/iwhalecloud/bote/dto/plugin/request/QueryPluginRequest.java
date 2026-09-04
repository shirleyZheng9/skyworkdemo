package com.iwhalecloud.bote.dto.plugin.request;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
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
@ToString(callSuper = true)
public class QueryPluginRequest extends PagingQueryParams {
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "目录 ID")
  private Long catalogItemId;
  @Schema(description = "订阅状态，0 未订阅 1 已订阅")
  private String subscribeStatus;
  @Schema(description = "插件类型")
  private String pluginType;
  @Schema(description = "插件子类型")
  private String pluginSubType;
}

