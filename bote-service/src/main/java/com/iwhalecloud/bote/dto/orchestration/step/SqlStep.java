package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

/**
 * SQL 步骤
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
public class SqlStep extends AbstractStep {
  /** SQL ID */
  private Long sqlId;
  /** 参数 */
  private ParameterSpec parameters;
  /** 出参结构 */
  private ParameterSpec response;

  public SqlStep() {
    super(StepType.SQL);
  }
}
