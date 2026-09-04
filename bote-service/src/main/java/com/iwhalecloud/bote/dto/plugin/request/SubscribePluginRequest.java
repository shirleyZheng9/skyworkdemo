package com.iwhalecloud.bote.dto.plugin.request;

import com.iwhalecloud.bote.dto.plugin.SimplePluginAuthParam;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 订阅插件入参
 *
 * @author chen.linfa
 * @since 2025-12-09
 */
@Getter
@Setter
@ToString
public class SubscribePluginRequest {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "插件 ID")
  private Long pluginId;
  @Schema(description = "认证信息")
  private List<SimplePluginAuthParam> authParams;
}
