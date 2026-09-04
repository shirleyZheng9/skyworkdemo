package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

/**
 * 工作流步骤
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
public class WorkflowStep extends AbstractStep {
  /** 流程 ID */
  private Long flowId;
  /** 参数 */
  private ParameterSpec parameters;
  /** 出参结构 */
  private ParameterSpec response;

  public WorkflowStep() {
    super(StepType.WORKFLOW);
  }
}
