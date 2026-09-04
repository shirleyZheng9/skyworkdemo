package com.iwhalecloud.bote.dto.plugin.response;

import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.SimplePluginAuthParam;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 插件信息
 *
 * @author chen.linfa
 * @since 2025-12-09
 */
@Getter
@Setter
@ToString
public class PluginDefinition {
  @Schema(description = "插件 ID")
  private Long pluginId;
  @Schema(description = "插件名称")
  private String pluginName;
  @Schema(description = "插件编码")
  private String pluginCode;
  @Schema(description = "插件描述")
  private String remark;
  @Schema(description = "插件功能介绍")
  private String pluginIntro;
  @Schema(description = "插件状态")
  private String pluginStatus;
  @Schema(description = "插件图标")
  private String pluginIcon;
  @Schema(description = "目录 ID")
  private Long catalogItemId;
  @Schema(description = "插件类型")
  private String pluginType;
  @Schema(description = "插件子分类")
  private String pluginSubType;
  @Schema(description = "服务地址")
  private String relativePath;
  @Schema(description = "认证信息")
  private List<SimplePluginAuthParam> authParams;
  @Schema(description = "网关 ID")
  private Long gatewayId;
  @Schema(description = "订阅时是否提醒展示认证信息")
  private Boolean configOnUse;
  @Schema(description = "订阅状态")
  private Boolean subscribeStatus;
  @Schema(description = "自动发现工具方式")
  private String discoveryToolType;
  @Schema(description = "自动发现工具地址")
  private String discoveryToolPath;
  @Schema(description = "插件状态（00A:有效, 00X:无效）")
  private String statusCd;

  @Schema(description = "网关")
  private PluginGatewayDTO gateway;
  @Schema(description = "工具列表")
  private List<PluginToolSpec> tools;
  @Schema(description = "本地服务类型手工维护的工具列表")
  private List<PluginToolDTO> apiTools;

  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PluginGatewayDTO {
    @Schema(description = "网关 ID")
    private Long gatewayId;
    @Schema(description = "网关名称")
    private String gatewayName;
    @Schema(description = "网关类型")
    private String gatewayType;
    @Schema(description = "链接")
    private String url;
    @Schema(description = "用户名")
    private String userName;
    @Schema(description = "用户密码")
    private String password;
    @Schema(description = "用户密钥")
    private String apiKey;
    @Schema(description = "Higress 运行网关")
    private String higressRuntimeUrl;
  }

  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PluginToolDTO {
    @Schema(description = "插件 ID")
    private Long pluginId;
    @Schema(description = "工具名称")
    private String toolName;
    @Schema(description = "服务路径")
    private String relativePath;
    @Schema(description = "请求方式")
    private String reqMethod;
    @Schema(description = "body 类型")
    private String bodyType;
    @Schema(description = "入参结构")
    private ParameterSpec request;
    @Schema(description = "出参结构")
    private ParameterSpec response;
  }
}
