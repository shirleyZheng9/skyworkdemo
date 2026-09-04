package com.iwhalecloud.bote.entity.app;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作台应用授权 Entity
 *
 * @author wang.tingyun
 * @since 2025-09-12
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_workbench_app_auth")
public class WorkbenchAppAuthEntity extends BaseEntity {

  @DiffId
  @Schema(description = "授权ID")
  private Long authId;

  @DiffField(name = "WORKBENCH_APP_ID")
  @Schema(description = "工作台应用ID")
  private Long workbenchAppId;

  @DiffField(name = "USER_ID")
  @Schema(description = "授权用户ID")
  private Long userId;

  @DiffField(name = "ORG_ID")
  @Schema(description = "授权组织ID")
  private Long orgId;

  @DiffField(name = "SPACE_ID")
  @Schema(description = "工作空间ID")
  private Long spaceId;

}