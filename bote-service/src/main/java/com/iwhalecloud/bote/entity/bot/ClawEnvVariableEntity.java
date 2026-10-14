package com.iwhalecloud.bote.entity.bot;

import com.cmsr.tiangong.diffc.annotations.DiffField;
import com.cmsr.tiangong.diffc.annotations.DiffId;
import com.cmsr.tiangong.diffc.annotations.DiffNode;
import com.cmsr.tiangong.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * claw 环境变量关联 Entity
 *
 * @author chen.linfa
 * @since 2026-04-23
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_CLAW_ENV_VARIABLE")
public class ClawEnvVariableEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long id;

  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;

  @DiffField(name = "SCENE_ID", parent = true)
  @Schema(description = "场景 ID")
  private Long sceneId;

  @DiffField(name = "VARIABLE_ID")
  @Schema(description = "环境变量 ID")
  private Long variableId;
}
