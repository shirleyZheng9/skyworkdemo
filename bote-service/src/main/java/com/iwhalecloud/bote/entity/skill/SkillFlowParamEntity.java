package com.iwhalecloud.bote.entity.skill;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：流程参数 Entity
 *
 * @author auto
 * @since 2024-09-18
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_skill_flow_param")
public class SkillFlowParamEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long paramId;
  @DiffField(name = "FLOW_ID", parent = true)
  @Schema(description = "流程 ID")
  private Long flowId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
  @DiffField(name = "VARIABLE_JSON")
  @Schema(description = "变量 JSON")
  private String variableJson;
  @DiffField(name = "REQUEST_JSON")
  @Schema(description = "入参 JSON")
  private String requestJson;
  @DiffField(name = "RESPONSE_JSON")
  @Schema(description = "出参 JSON")
  private String responseJson;
}
