package com.iwhalecloud.bote.service.orchestration.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.util.ParamConverterUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.dto.orchestration.step.ParamExtractorStep;
import com.iwhalecloud.bote.dto.orchestration.step.PluginStep;
import com.iwhalecloud.bote.dto.orchestration.step.ScriptStep;
import com.iwhalecloud.bote.dto.orchestration.step.ServiceStep;
import com.iwhalecloud.bote.dto.orchestration.step.SqlStep;
import com.iwhalecloud.bote.dto.orchestration.step.ToolboxStep;
import com.iwhalecloud.bote.dto.orchestration.step.WorkflowStep;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Map;
import java.util.function.Function;
import org.springframework.lang.Nullable;

/**
 * 节点异常处理的设定内容辅助类
 *
 * @author bianjp
 * @since 2025-09-03
 */
public final class StepFallbackValueHelper {
  private StepFallbackValueHelper() {
  }

  /**
   * 节点的出参结构提取器
   */
  private static final Map<Class<? extends AbstractStep>, Function<AbstractStep, ParameterSpec>> stepOutputExtractors = ImmutableMap.<Class<? extends AbstractStep>, Function<AbstractStep, ParameterSpec>>builder()
    .put(ServiceStep.class, step -> ((ServiceStep) step).getResponse())
    .put(SqlStep.class, step -> ((SqlStep) step).getResponse())
    .put(PluginStep.class, step -> ((PluginStep) step).getResponse())
    .put(ParamExtractorStep.class, step -> ((ParamExtractorStep) step).getParameters())
    .put(ScriptStep.class, step -> ((ScriptStep) step).getOutData())
    .put(ToolboxStep.class, step -> ((ToolboxStep) step).getResponse())
    .put(WorkflowStep.class, step -> ((WorkflowStep) step).getResponse())
    .build();

  /**
   * 校验设定内容是否合法
   */
  public static void validateFallbackValue(AbstractStep step, @Nullable String fallbackValue) {
    // 必须配置
    if (fallbackValue == null || fallbackValue.isEmpty()) {
      throw new BssException(String.format("步骤【%s】的异常处理未配置设定内容", step.getName()));
    }
    // JSON 必须合法
    Object value;
    try {
      value = JsonUtil.getObjectMapper().readValue(fallbackValue, Object.class);
    }
    catch (JsonProcessingException e) {
      throw new BssException(String.format("步骤【%s】的异常处理配置的设定内容不是合法的 JSON: %s", step.getName(), e.getMessage()), e);
    }
    // 必须和节点的出参结构一致
    convertFallbackValue(step, value);
  }

  /**
   * 转换节点异常处理配置的设定内容
   */
  @Nullable
  public static Object convertFallbackValue(AbstractStep step, Object value) {
    ParameterSpec spec = getStepOutputSpec(step);
    try {
      return ParamConverterUtil.convert("", spec, value);
    }
    catch (Exception e) {
      throw new BssException(String.format("步骤【%s】的异常处理配置的设定内容不合法: %s", step.getName(), e.getMessage()), e);
    }
  }

  /**
   * 获取节点的出参结构
   */
  @Nullable
  private static ParameterSpec getStepOutputSpec(AbstractStep step) {
    // 部分节点的出参结构是固定的
    ParameterSpec spec = NodeDebugHelper.getStaticStepOutput(step.getType());
    if (spec != null) {
      return spec;
    }
    Function<AbstractStep, ParameterSpec> extractor = stepOutputExtractors.get(step.getClass());
    if (extractor != null) {
      return extractor.apply(step);
    }
    // 无法获取出参结构，表示该节点类型不支持异常处理
    throw new BssException("节点【" + step.getName() + "】不支持异常处理");
  }
}
