package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 循环步骤
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
public class LoopStep extends AbstractStep {
  /** 循环类型 */
  private String loopType;
  /** 循环列表(取值表达式) */
  private String list;
  /** 循环对象(取值表达式) */
  private String object;
  /** 起始值(取值表达式) */
  private String start;
  /** 结束值(取值表达式)，包含 */
  private String end;
  /** 步长(取值表达式) */
  private String step;
  /** 子步骤编码 */
  private String childStep;
  /** 循环内所有节点的编码 */
  private List<String> childrenCodes;

  public LoopStep() {
    super(StepType.LOOP);
  }
}
