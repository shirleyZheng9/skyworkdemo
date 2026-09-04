package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import lombok.Getter;
import lombok.Setter;

/**
 * @author qian.sisheng
 * @since 2025-04-22
 */
@Getter
@Setter
public class PluginStep extends AbstractStep {
  /** 插件ID */
  private Long pluginId;
  /** 大模型 ID（字面量或引用表达式） */
  private String modelId;
  /** 是否对接插件市场 */
  private Boolean isPluginHub;
  /** 工具名称 */
  private String toolName;
  /** 参数 */
  private ParameterSpec parameters;
  /** 出参结构 */
  private ParameterSpec response;

  public PluginStep() {
    super(StepType.PLUGIN);
  }
}
