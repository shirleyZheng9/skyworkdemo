package com.iwhalecloud.bote.dto.skill.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 插件查询参数
 *
 * @author auto
 * @since 2025-04-01
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "插件查询参数")
public class PluginQueryParams extends PagingQueryParams {
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "来源")
  private String sourceFrom;
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "是否用于配置管理", example = "T/F")
  private String configFlag;
  @Schema(description = "插件状态")
  private String pluginStatus;
  @Schema(description = "插件类型")
  private String pluginType;
  @Schema(description = "插件子类型")
  private String pluginSubType;
}
