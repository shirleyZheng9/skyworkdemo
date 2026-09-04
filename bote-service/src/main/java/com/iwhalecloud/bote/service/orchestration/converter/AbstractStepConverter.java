package com.iwhalecloud.bote.service.orchestration.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.dto.orchestration.StepExceptionConfig;
import com.iwhalecloud.bote.dto.orchestration.StepExceptionConfig.StepExceptionProcessingStrategy;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.generator.flow.converter.AbstractNodeConverter;
import com.iwhalecloud.bote.service.orchestration.helper.StepFallbackValueHelper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 步骤转换器抽象类
 *
 * @author bianjp
 * @since 2024-08-29
 */
public abstract class AbstractStepConverter<T extends AbstractStep> {
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** 步骤工厂实例 */
  protected final Supplier<T> stepFactory;

  protected AbstractStepConverter() {
    this.stepFactory = null;
  }

  protected AbstractStepConverter(Supplier<T> stepFactory) {
    this.stepFactory = stepFactory;
  }

  /**
   * 转换步骤
   * <p>子类请实现 {@link #convertStep}，不要覆盖本方法</p>
   *
   * @param node 节点对象
   * @param context 转换上下文
   * @return 步骤实例
   */
  public final T convert(SceneGraphNodeDTO node, ConverterContext context) {
    Assert.notNull(stepFactory, () -> "不支持转换步骤: " + node.getNodeType());
    T step = stepFactory.get();
    step.setName(node.getNodeName());
    step.setCode(node.getNodeCode());
    try {
      convertStep(node, step, context);
    }
    catch (BssException e) {
      // 确保报错信息中包含步骤名称
      if (!Strings.CS.contains(e.getFailMsg(), node.getNodeName())) {
        e.setFailMsg(String.format("步骤【%s】转换失败: %s", node.getNodeName(), e.getMessage()));
      }
      throw e;
    }
    catch (Exception e) {
      throw new BssException(String.format("步骤【%s】转换失败: %s", node.getNodeName(), ExpUtil.getMsg(e)), e);
    }

    // 解析异常处理配置
    step.setExceptionConfig(parseExceptionConfig(node, step, context));
    return step;
  }

  /**
   * 解析节点的异常处理配置
   */
  @Nullable
  private StepExceptionConfig parseExceptionConfig(SceneGraphNodeDTO node, T step, ConverterContext context) {
    // 单节点调试时忽略异常处理配置，避免解析失败影响调试
    if (context.isSingleNodeDebug()) {
      return null;
    }
    // 异常处理配置
    StepExceptionConfig exceptionConfig = parseJsonAttr(node, "exceptionConfig", StepExceptionConfig.class);
    if (exceptionConfig == null || exceptionConfig.getStrategy() == null || exceptionConfig.getStrategy() == StepExceptionProcessingStrategy.ABORT) {
      return null;
    }

    // 返回设定内容
    if (exceptionConfig.getStrategy() == StepExceptionProcessingStrategy.FALLBACK) {
      String fallbackValue = StringUtils.trimToNull(exceptionConfig.getFallbackValue());
      // 校验设定内容是否合法
      StepFallbackValueHelper.validateFallbackValue(step, fallbackValue);
      exceptionConfig.setFallbackValue(fallbackValue);
    }
    else {
      exceptionConfig.setFallbackValue(null);
    }

    // 执行异常分支
    if (exceptionConfig.getStrategy() == StepExceptionProcessingStrategy.BRANCH) {
      String exceptionBranch = context.getTargetEdges(node).stream()
        .filter(e -> SceneConsts.EXCEPTION_EDGE_PORT.equals(e.getLeft()))
        .map(e -> e.getRight().getNodeCode())
        .findFirst()
        .orElseThrow(() -> new BssException(String.format("步骤【%s】未配置异常流程", node.getNodeName())));
      exceptionConfig.setExceptionBranch(exceptionBranch);
    }
    return exceptionConfig;
  }

  /**
   * 转换步骤逻辑，由子类实现
   *
   * @param step 步骤实例
   * @param context 转换上下文
   */
  protected void convertStep(SceneGraphNodeDTO node, T step, ConverterContext context) {
    throw new UnsupportedOperationException("不支持转换步骤: " + node.getNodeType());
  }

  /**
   * 获取节点的入参列表
   */
  @Nullable
  protected final ParameterSpec getInputParams(SceneGraphNodeDTO node) {
    Map<?, ?> parameters = MapUtils.getMap(node.getNodeData(), "parameters");
    if (MapUtils.isNotEmpty(parameters)) {
      return JsonUtil.convert(parameters, ParameterSpec.class);
    }
    return null;
  }

  /**
   * 获取节点的出参配置
   */
  @Nullable
  protected final ParameterSpec getOutputParams(SceneGraphNodeDTO node) {
    Object outData = MapUtils.getObject(node.getNodeData(), "outData");
    if (ObjectUtils.isNotEmpty(outData)) {
      ParameterSpec root = JsonUtil.convert(outData, ParameterSpec.class);
      return AbstractNodeConverter.simplifyParameter(root);
    }
    return null;
  }

  /**
   * 解析 JSON 配置的属性
   */
  @Nullable
  protected final <V> V parseJsonAttr(SceneGraphNodeDTO node, String attr, Class<V> clazz) {
    Object value = MapUtils.getObject(node.getNodeData(), attr);
    if (value == null) {
      return null;
    }
    if (clazz.isInstance(value)) {
      return clazz.cast(value);
    }
    return JsonUtil.convert(value, clazz);
  }

  /**
   * 解析 JSON 配置的属性
   */
  @Nullable
  protected final <V> V parseJsonAttr(SceneGraphNodeDTO node, String attr, TypeReference<V> typeReference) {
    Object value = MapUtils.getObject(node.getNodeData(), attr);
    if (value == null) {
      return null;
    }
    return JsonUtil.convert(value, typeReference);
  }

  /**
   * 解析 JSON 配置的必填属性
   */
  protected final <V> V parseRequiredJsonAttr(SceneGraphNodeDTO node, String attrName, Class<V> clazz) {
    return parseRequiredJsonAttr(node, attrName, attrName, clazz);
  }

  /**
   * 解析 JSON 配置的必填属性
   */
  protected final <V> V parseRequiredJsonAttr(SceneGraphNodeDTO node, String attrName, String attrDesc, Class<V> clazz) {
    V value = parseJsonAttr(node, attrName, clazz);
    if (ObjectUtils.isEmpty(value)) {
      throw new BssException(String.format("步骤【%s】缺少必要配置: %s", node.getNodeName(), attrDesc));
    }
    return value;
  }

  /**
   * 解析 JSON 配置的必填属性
   */
  protected final <V> V parseRequiredJsonAttr(SceneGraphNodeDTO node, String attrName, TypeReference<V> typeReference) {
    return parseRequiredJsonAttr(node, attrName, attrName, typeReference);
  }

  /**
   * 解析 JSON 配置的必填属性
   */
  protected final <V> V parseRequiredJsonAttr(SceneGraphNodeDTO node, String attrName, String attrDesc, TypeReference<V> typeReference) {
    V value = parseJsonAttr(node, attrName, typeReference);
    if (ObjectUtils.isEmpty(value)) {
      throw new BssException(String.format("步骤【%s】缺少必要配置: %s", node.getNodeName(), attrDesc));
    }
    return value;
  }
}
