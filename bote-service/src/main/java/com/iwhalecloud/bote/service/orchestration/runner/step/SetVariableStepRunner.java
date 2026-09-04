package com.iwhalecloud.bote.service.orchestration.runner.step;

import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.SceneParamUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.step.SetVariableStep;
import com.iwhalecloud.bote.dto.orchestration.step.SetVariableStep.VariableSpec;
import com.iwhalecloud.bote.service.orchestration.runner.AbstractStepRunner;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.lang.Nullable;

/**
 * 设置变量步骤执行器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class SetVariableStepRunner extends AbstractStepRunner<SetVariableStep> {
  @Override
  protected void doRun(SceneOrchestrationContext context, SetVariableStep step) {
    if (CollectionUtils.isEmpty(step.getVariables())) {
      return;
    }

    Map<String, Object> localVariables = context.getGlobalVariables();
    for (VariableSpec spec : step.getVariables()) {
      // 列表添加元素需要特殊处理
      if (spec.isAddElement()) {
        addElementToList(context, spec);
        continue;
      }

      // 解析变量值
      Object value = resolveVariableValue(spec);
      // ConcurrentHashMap 不支持 null 值，值为 null 为需要删除 Map 元素
      if (value != null) {
        localVariables.put(spec.getName(), value);
      }
      else {
        localVariables.remove(spec.getName());
      }
      context.addStepLog("设置变量: name=%s, value=%s", spec.getName(), value);
    }
  }

  /**
   * 解析变量值
   */
  @Nullable
  private Object resolveVariableValue(VariableSpec spec) {
    Object value;
    // 对象
    if (spec.isObject()) {
      value = resolveObjectParameterValue(spec.getName(), spec);
    }
    // 列表，只支持直接给列表赋值，不支持指定元素
    else if (spec.isList()) {
      value = resolveListParameterValue(spec.getName(), spec);
    }
    // 属性，需要转换数据类型
    else {
      value = SceneParamUtil.getParamValue(spec.getValue());
      value = AttrDataType.convert(spec.getName(), spec.getType(), value);
    }
    return value;
  }

  /**
   * 添加元素到列表中
   */
  @SuppressWarnings("unchecked")
  private void addElementToList(SceneOrchestrationContext context, VariableSpec spec) {
    if (!spec.hasChildren()) {
      return;
    }

    // 解析要添加的元素值，支持一次添加多个元素
    List<Object> values = new ArrayList<>(spec.getChildren().size());
    for (ParameterSpec child : spec.getChildren()) {
      Object value = resolveParameterValue(child.getName(), child);
      // 忽略值为 null 的元素
      if (value != null) {
        values.add(value);
      }
    }

    // 没有非空元素时不处理
    if (values.isEmpty()) {
      return;
    }

    // 找出原列表，将元素添加到原列表中
    List<Object> list = (List<Object>) context.getGlobalVariables().get(spec.getName());
    list = CollectionUtils.isEmpty(list) ? values : ListUtils.union(list, values);
    context.getGlobalVariables().put(spec.getName(), list);
    context.addStepLog("设置变量-添加元素: name=%s, elements=%s", spec.getName(), values);
  }
}
