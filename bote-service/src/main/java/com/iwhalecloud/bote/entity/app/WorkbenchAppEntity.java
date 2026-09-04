package com.iwhalecloud.bote.entity.app;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作台应用 Entity
 *
 * @author tingyun.wang
 * @since 2025-09-05
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_workbench_app")
public class WorkbenchAppEntity extends BaseEntity {

  @DiffId
  @Schema(description = "应用ID")
  private Long workbenchAppId;

  @DiffField(name = "SPACE_ID")
  @Schema(description = "工作空间ID")
  private Long spaceId;

  @DiffField(name = "APP_NAME")
  @Schema(description = "应用名称")
  @Size(max = 20, message = "应用名称超过限定长度20")
  private String appName;

  @DiffField(name = "APP_DESC")
  @Schema(description = "应用描述")
  @Size(max = 500, message = "应用描述超过限定长度500")
  private String appDesc;

  @DiffField(name = "APP_ICON")
  @Schema(description = "应用图标")
  private String appIcon;

  @DiffField(name = "APP_SCENE")
  @Schema(description = "应用场景")
  private String appScene;

  @DiffField(name = "APP_STATUS")
  @Schema(description = "应用状态（A:启用, X:停用）")
  private String appStatus;

  @DiffField(name = "APP_SETTING_STATUS")
  @Schema(description = "应用能力配置状态（T:已配置, F:未配置完成）")
  private String appSettingStatus;

  @DiffField(name = "AUTH_TYPE")
  @Schema(description = "授权类型")
  private String authType;

  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "应用分类")
  private Long catalogItemId;

}
