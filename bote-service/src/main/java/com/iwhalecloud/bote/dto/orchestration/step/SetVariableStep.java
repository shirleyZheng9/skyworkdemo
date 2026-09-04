package com.iwhalecloud.bote.dto.orchestration.step;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 设置变量步骤
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
public class SetVariableStep extends AbstractStep {
  /** 变量规格列表 */
  private List<VariableSpec> variables;

  public SetVariableStep() {
    super(StepType.SET_VARIABLE);
  }

  /**
   * 变量规格
   */
  @Getter
  @Setter
  @ToString(callSuper = true)
  @JsonInclude(Include.NON_NULL)
  public static class VariableSpec extends ParameterSpec {
    /** 动作(assign: 赋值, addElement: 列表增加元素。为空时默认为 assign) */
    private String operation;

    /**
     * 是否是添加数组元素
     *
     * @return 是否是添加数组元素
     */
    @JsonIgnore
    public boolean isAddElement() {
      return "addElement".equals(operation);
    }
  }
}
