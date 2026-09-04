package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

/**
 * 结束步骤
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
public class EndStep extends AbstractStep {
  /** 出参 */
  private ParameterSpec parameters;

  public EndStep() {
    super(StepType.END);
  }
}
