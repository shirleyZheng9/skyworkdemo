package com.iwhalecloud.bote.entity.plugin;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author qian.sisheng
 * @since 2025-04-01
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_plugin")
public class PluginEntity extends BaseEntity {
  @DiffId
  @Schema(description = "插件ID")
  private Long pluginId;
  @DiffField(name = "PLUGIN_CODE")
  @Schema(description = "插件编码")
  private String pluginCode;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "PLUGIN_NAME")
  @Schema(description = "插件名称")
  private String pluginName;
  @DiffField(name = "PLUGIN_DESC")
  @Schema(description = "插件描述")
  private String pluginDesc;
  @DiffField(name = "PLUGIN_ICON")
  @Schema(description = "插件图标")
  private String pluginIcon;
  @DiffField(name = "REQ_JSON")
  @Schema(description = "请求JSON")
  private String reqJson;
  @DiffField(name = "RESPONSE_JSON")
  @Schema(description = "响应JSON")
  private String responseJson;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @DiffField(name = "PLUGIN_STATUS")
  @Schema(description = "状态, 0: 禁用，1: 启用")
  private String pluginStatus;
  @DiffField(name = "PLUGIN_DETAIL")
  @Schema(description = "插件详情")
  private String pluginDetail;
  @DiffField(name = "NEED_MODEL")
  @Schema(description = "是否需要模型, T/F")
  private String needModel;
  @DiffField(name = "MODEL_TYPE")
  @Schema(description = "模型类型")
  private String modelType;
}
