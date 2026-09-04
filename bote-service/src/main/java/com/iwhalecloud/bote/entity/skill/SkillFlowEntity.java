package com.iwhalecloud.bote.entity.skill;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.Size;

/**
 * 技能：流程 Entity
 *
 * @author auto
 * @since 2024-09-18
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_skill_flow")
public class SkillFlowEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long flowId;
  @DiffField(name = "FLOW_NAME")
  @Schema(description = "流程名称")
  @Size(max = 20, message = "流程名称超过限定长度20")
  private String flowName;
  @DiffField(name = "FLOW_CODE")
  @Schema(description = "流程编码")
  @Size(max = 50, message = "流程编码超过限定长度50")
  private String flowCode;
  @DiffField(name = "FLOW_DESC")
  @Schema(description = "流程描述")
  @Size(max = 250, message = "流程描述超过限定长度250")
  private String flowDesc;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录 ID")
  private Long catalogItemId;
  @DiffField(name = "FLOW_GRAPH_JSON")
  @Schema(description = "流程图")
  private String flowGraphJson;
  @DiffField(name = "FLOW_DSL")
  @Schema(description = "流程 DSL")
  private String flowDsl;
  @DiffField(name = "FLOW_TYPE")
  @Schema(description = "流程类型")
  private String flowType;
  @DiffField(name = "flow_explanation")
  @Schema(description = "流程解释")
  private String flowExplanation;
  @DiffField(name = "flow_step_json")
  @Schema(description = "流程步骤")
  private String flowStepJson;

  /**
   * 是否是对话型工作流
   */
  @JsonIgnore
  public boolean isMultiStep() {
    return SceneConsts.FLOW_TYPE_MULTI_STEP.equals(flowType);
  }
}
