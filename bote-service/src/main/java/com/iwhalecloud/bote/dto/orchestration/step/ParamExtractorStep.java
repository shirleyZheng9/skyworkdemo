package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.model.MemoryConfig;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import java.util.List;

import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import lombok.Getter;
import lombok.Setter;

/**
 * 参数提取步骤
 *
 * @author bianjp
 * @since 2024-09-04
 */
@Getter
@Setter
public class ParamExtractorStep extends AbstractStep {
  /** 大模型 ID（字面量或引用表达式） */
  private String modelId;
  /** 输入（模板字符串，支持引用变量） */
  private String input;
  /** 指令的提示词 ID, 可选 */
  private Long promptId;
  /** 提示词参数 */
  private List<ParameterSpec> promptParameters;
  /** 指令（模板字符串，支持引用变量），可选 */
  private String instruction;
  /** 要提取的参数 */
  private ParameterSpec parameters;
  /** 记忆配置，可选 */
  private MemoryConfig memory;
  /** 自定义模型配置 */
  private CustomModelConfig customModelConfig;

  public ParamExtractorStep() {
    super(StepType.PARAM_EXTRACTOR);
  }
}
