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
 * 工作台应用关联 Entity
 *
 * @author wang.tingyun
 * @since 2025-09-08
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_workbench_app_rel")
public class WorkbenchAppRelEntity extends BaseEntity {

  @DiffId
  @Schema(description = "关联ID")
  private Long relId;

  @DiffField(name = "WORKBENCH_APP_ID")
  @Schema(description = "工作台应用ID")
  private Long workbenchAppId;

  @DiffField(name = "REL_APP_ID")
  @Schema(description = "关联的应用ID")
  private Long relAppId;

  @DiffField(name = "REL_APP_TYPE")
  @Schema(description = "关联的应用类型")
  private String relAppType;

  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;

  @DiffField(name = "SPACE_ID")
  @Schema(description = "工作空间ID")
  private Long spaceId;

}


