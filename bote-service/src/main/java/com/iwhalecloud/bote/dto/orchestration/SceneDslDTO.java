package com.iwhalecloud.bote.dto.orchestration;

import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.base.SimpleFlowStepDTO;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 场景编排 DSL 对象
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
@ToString
public class SceneDslDTO {
  /** 场景 ID */
  private Long id;
  /** 场景编码 */
  private String code;
  /** 场景名称 */
  private String name;
  /** 是否是对话流 */
  private Boolean chatflow;
  /** 入参配置(仅用于单步流程) */
  private ParameterSpec input;
  /** 变量列表 */
  private List<ParameterSpec> variables;
  /** 步骤列表 */
  private List<AbstractStep> steps;
  /** 流程步骤列表 */
  private List<SimpleFlowStepDTO> flowSteps;
}
