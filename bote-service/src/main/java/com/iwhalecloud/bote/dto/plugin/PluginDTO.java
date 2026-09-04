package com.iwhalecloud.bote.dto.plugin;

import com.iwhalecloud.bote.entity.plugin.PluginEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 插件
 *
 * @author qian.sisheng
 * @since 2025-04-01
 */

@Getter
@Setter
@ToString(callSuper = true)
public class PluginDTO extends PluginEntity {
  @Schema(description = "来源")
  private String sourceFrom;
  @Schema(description = "创建人")
  private String creatorName;
  @Schema(description = "修改人")
  private String updatorName;
  @Schema(description = "用户编码")
  private String userCode;
  @Schema(description = "用户图标")
  private String userIcon;
  @Schema(description = "目录名称")
  private String catalogName;
}
