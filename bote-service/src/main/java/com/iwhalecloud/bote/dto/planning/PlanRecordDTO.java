package com.iwhalecloud.bote.dto.planning;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.dto.orchestration.SceneDslDTO;
import com.iwhalecloud.bote.entity.planning.PlanRecordEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 规划执行记录 DTO
 *
 * @author chen.linfa
 * @since 2025-05-14
 */
@Getter
@Setter
@ToString(callSuper = true)
public class PlanRecordDTO extends PlanRecordEntity {
  private List<PlanStepDTO> steps;

  /** 编排的 DSL */
  private SceneDslDTO dsl;

  /**
   * 作为对话事件内容，简化参数结果
   *
   * @see com.iwhalecloud.bote.common.consts.ChatMessageType#UPDATE_PLAN_STATE
   */
  @JsonIgnore
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("planId", getPlanId());
    map.put("status", getStatus());
    List<Map<String, Object>> steps = new ArrayList<>(getSteps().size());
    map.put("steps", steps);
    for (PlanStepDTO step : getSteps()) {
      Map<String, Object> stepMap = new HashMap<>();
      stepMap.put("stepId", step.getStepId());
      stepMap.put("agentId", step.getAgentId());
      stepMap.put("agentName", step.getAgentName());
      stepMap.put("agentRequest", step.getAgentRequest());
      stepMap.put("stepStatus", step.getStepStatus());
      stepMap.put("stepIndex", step.getStepIndex());
      stepMap.put("flows", step.getFlowSteps());
      steps.add(stepMap);
    }
    return map;
  }
}
