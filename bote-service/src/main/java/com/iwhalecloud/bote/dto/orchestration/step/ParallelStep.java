package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 并行步骤
 *
 * @author bianjp
 * @since 2024-11-06
 */
@Getter
@Setter
public class ParallelStep extends AbstractStep {
  /** 分支列表（每个分支中第一个节点的编码） */
  private List<String> branches;

  public ParallelStep() {
    super(StepType.PARALLEL);
  }
}
