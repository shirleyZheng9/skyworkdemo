package com.iwhalecloud.bote.dto.plugin;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 插件鉴权 DTO
 *
 * @author chen.linfa
 * @since 2025-12-09
 */
@Getter
@Setter
@ToString
public class PluginAuthDTO {
  @Schema(description = "鉴权 ID")
  private Long authId;

  @Schema(description = "租户 ID")
  private Long tenantId;

  @Schema(description = "插件 ID")
  private Long pluginId;
  @Schema(description = "插件名称")
  private String pluginName;
  @Schema(description = "插件图标")
  private String pluginIcon;
  @Schema(description = "插件类型")
  private String pluginType;
  @Schema(description = "插件子类型")
  private String pluginSubType;

  @Schema(description = "参数列表")
  private List<SimplePluginAuthParam> params;
}
