package com.iwhalecloud.bote.dto.plugin;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 插件执行参数
 *
 * @author qian.sisheng
 * @since 2025-04-09
 */
@Getter
@Setter
@ToString(callSuper = true)
public class PluginExecuteParams {
  @Schema(description = "插件ID")
  private Long pluginId;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "模型ID")
  private Long modelId;
  @Schema(description = "是否依赖模型")
  private String needModel;
  @Schema(description = "插件输入参数")
  private Map<String, Object> params;
  @Schema(description = "是否插件市场")
  private Boolean isPluginHub;
  @Schema(description = "工具名称")
  private String toolName;
  @Schema(description = "是否工具调用")
  private Boolean isToolCall;

  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PluginToolParams {
    @Schema(description = "请求路径参数")
    private Map<String, Object> path;
    @Schema(description = "请求头")
    private Map<String, Object> header;
    @Schema(description = "URL 参数")
    private Map<String, Object> query;
    @Schema(description = "请求体")
    private Object body;
  }
}
