package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：简单流程
 *
 * @author bianjp
 * @since 2024-12-18
 */
@Getter
@Setter
@ToString
public class SimpleSkillFlowDTO {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "流程 ID")
  private Long flowId;
  @Schema(description = "流程编码")
  private String flowCode;
  @Schema(description = "流程名称")
  private String flowName;
  @Schema(description = "流程类型")
  private String flowType;
  @Schema(description = "流程描述")
  private String flowDesc;

  @JsonIgnore
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("tenantId", tenantId);
    map.put("flowId", flowId);
    map.put("flowCode", flowCode);
    map.put("flowName", flowName);
    map.put("flowType", flowType);
    map.put("flowDesc", flowDesc);
    return map;
  }
}
