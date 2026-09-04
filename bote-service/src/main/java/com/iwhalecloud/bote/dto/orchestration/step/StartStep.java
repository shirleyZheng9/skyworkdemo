package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

/**
 * 开始步骤
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
public class StartStep extends AbstractStep {
  public StartStep() {
    super(StepType.START);
  }
}
