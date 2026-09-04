package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

/**
 * 大模型能力步骤
 *
 * @author bianjp
 * @since 2024-08-30
 */
@Getter
@Setter
public class LlmSkillStep extends AbstractStep {
  /** 大模型能力 ID */
  private Long apiId;
  /** 参数 */
  private ParameterSpec parameters;

  public LlmSkillStep() {
    super(StepType.LLM_SKILL);
  }
}
