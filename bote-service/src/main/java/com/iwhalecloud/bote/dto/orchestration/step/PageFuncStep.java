package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

/**
 * 页面步骤
 *
 * @author bianjp
 * @since 2024-08-30
 */
@Getter
@Setter
public class PageFuncStep extends AbstractStep {
  /** 页面函数 ID */
  private Long pageFuncId;
  /** 是否用作会话记忆，默认不开启 */
  private Boolean memorized;
  /** 是否自定义记忆内容，默认使用入参 */
  private Boolean customMemorized;
  /** 自定义记忆内容（取值表达式） */
  private String memoryContent;
  /** 参数 */
  private ParameterSpec parameters;

  public PageFuncStep() {
    super(StepType.PAGE_FUNC);
  }
}
