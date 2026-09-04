package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

/**
 * 异步工作流步骤
 *
 * @author wangtingyun
 * @since 2026-02-25
 */
@Getter
@Setter
public class AsyncWorkFlowStep extends AbstractStep {
  /** 流程 ID */
  private Long flowId;
  /** 参数 */
  private ParameterSpec parameters;
  /** 出参结构 */
  private ParameterSpec response;

  public AsyncWorkFlowStep() {
    super(StepType.ASYNC_WORKFLOW);
  }
}
