package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

/**
 * 微调模型检索步骤
 *
 * @author chen.linfa
 * @since 2025-04-19
 */
@Getter
@Setter
public class SlmRetrievalStep extends AbstractStep {
  /** 问题（取值表达式） */
  private String question;
  /** 微调模型 ID */
  private Long modelId;

  public SlmRetrievalStep() {
    super(StepType.SLM_RETRIEVAL);
  }
}
