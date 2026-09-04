package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

/**
 * 服务步骤(HTTP 服务)
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
public class ServiceStep extends AbstractStep {
  /** 服务 ID */
  private Long serviceId;
  /** 参数 */
  private ParameterSpec parameters;
  /** 出参结构 */
  private ParameterSpec response;

  public ServiceStep() {
    super(StepType.SERVICE);
  }
}
