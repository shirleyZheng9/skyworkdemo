package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 代码块步骤
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
public class ScriptStep extends AbstractStep {
  /** 脚本类型 */
  private String scriptType;
  /** 脚本内容 */
  private String scriptContent;
  /** 参数 */
  private ParameterSpec parameters;
  /** 出参 */
  private ParameterSpec outData;
  /** python 依赖包列表 */
  private List<String> pyPackageList;

  public ScriptStep() {
    super(StepType.SCRIPT);
  }
}
