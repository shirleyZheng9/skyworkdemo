package com.iwhalecloud.bote.service.orchestration.runner;

import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.ParamConverterUtil;
import com.iwhalecloud.bote.common.util.SceneContextUtil;
import com.iwhalecloud.bote.common.util.SceneParamUtil;
import com.iwhalecloud.bote.common.util.ScenePromptUtil;
import com.iwhalecloud.bote.common.util.TemplateUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.dto.orchestration.StepExceptionConfig;
import com.iwhalecloud.bote.dto.orchestration.StepExceptionConfig.StepExceptionProcessingStrategy;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.file.AbstractFile;
import com.iwhalecloud.bote.dto.orchestration.file.DataUrlFile;
import com.iwhalecloud.bote.dto.orchestration.file.FileServerFile;
import com.iwhalecloud.bote.dto.orchestration.file.UrlFile;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bote.dto.orchestration.step.AgentStep;
import com.iwhalecloud.bote.dto.orchestration.step.KnowledgeChatStep;
import com.iwhalecloud.bote.dto.orchestration.step.LlmStep;
import com.iwhalecloud.bote.dto.orchestration.step.QuestionClassifierStep;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.service.orchestration.SceneStepRegistry;
import com.iwhalecloud.bote.service.orchestration.helper.StepFallbackValueHelper;
import com.iwhalecloud.bote.service.orchestration.runner.step.QuestionClassifierStepRunner;
import com.iwhalecloud.bote.service.plugin.impl.helper.PluginParameterHelper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 场景步骤执行器抽象类
 *
 * @param <T> 步骤类型
 * @author bianjp
 * @since 2024-08-29
 */
@SuppressWarnings("PMD.GuardLogStatement")
public abstract class AbstractStepRunner<T extends AbstractStep> {
  protected final Logger logger = LoggerFactory.getLogger(getClass());
  protected final IFileStoreService fileStoreService = SpringUtil.getBean(IFileStoreService.class);
  private static final TenantSettingInfoCache tenantSettingInfoCache = SpringUtil.getBean(TenantSettingInfoCache.class);

  /**
   * 执行步骤
   *
   * @param step 步骤
   * @return 下一步步骤编码，返回 null 表示停止执行
   */
  @Nullable
  @SuppressFBWarnings("BC_IMPOSSIBLE_INSTANCEOF")
  @SuppressWarnings({"PMD.AvoidCatchingThrowable", "PMD.AvoidInstanceofChecksInCatchClause", "PMD.AvoidRethrowingException"})
  public final String run(T step) {
    logger.trace("Execute step start: name={}, code={}", step.getName(), step.getCode());
    SceneOrchestrationContext context = SceneContextUtil.getContext();
    // 记录日志
    context.startStepLog(step);
    // Future#cancel 只会设置中断标记，不会强制停止线程，线程中需要自行检测
    if (isInterrupted()) {
      logger.warn("Execute step interrupted: id={}, name={}, code={}", context.getDsl().getId(), step.getName(), step.getCode());
      throw new BssException(buildFailMsg(context, "执行被中断", step.getName()));
    }
    String nextStep;
    try {
      checkExecutionLimit(context, step);
      nextStep = doRunWithReturn(context, step);
      context.succeedStepLog();
    }
    catch (BreakLoopException | ContinueLoopException e) {
      // 循环的退出、跳过需要在循环节点中捕获
      throw e;
    }
    // 执行异常分支
    catch (ExecuteExceptionBranchException e) {
      return e.getExceptionBranch();
    }
    catch (Throwable e) {
      if (isInterrupted()) {
        logger.warn("Execute step interrupted: id={}, name={}, code={}", context.getDsl().getId(), step.getName(), step.getCode());
      }
      else {
        logger.error("Execute step failed: id={}, name={}, code={}", context.getDsl().getId(), step.getName(), step.getCode(), e);
      }
      nextStep = processException(e, step, context);
    }
    logger.trace("Execute step end: name={}, code={}", step.getName(), step.getCode());
    return nextStep;
  }

  /**
   * 处理节点执行异常
   */
  @SuppressWarnings("unchecked")
  @Nullable
  private String processException(Throwable throwable, T step, SceneOrchestrationContext context) {
    // 转换异常
    BssException exception = convertException(throwable, step, context);
    // 检查异常处理策略配置
    StepExceptionProcessingStrategy strategy = step.getExceptionProcessingStrategy();
    // 中断流程
    if (strategy == StepExceptionProcessingStrategy.ABORT) {
      context.failStepLog(throwable);
      throw exception;
    }

    context.setExceptionInfo(step, exception.getFailCode(), exception.getFailMsg());
    StepExceptionConfig exceptionConfig = step.getExceptionConfig();

    // 返回设定内容
    if (exceptionConfig.getStrategy() == StepExceptionProcessingStrategy.FALLBACK) {
      context.addStepLog("执行失败，返回设定内容: %s", ExpUtil.getMsg(throwable));
      Object output = exceptionConfig.parseFallbackValue(step, StepFallbackValueHelper::convertFallbackValue);
      context.setStepOutput(step, output);
      context.succeedStepLog();
      // 继续执行下一步
      // 问题分类节点需要特殊处理，根据分类名称决定执行哪个分支
      if (step instanceof QuestionClassifierStep) {
        String category = output instanceof Map ? MapUtils.getString((Map<String, Object>) output, "name") : null;
        return QuestionClassifierStepRunner.getNextStep((QuestionClassifierStep) step, category);
      }
      return step.getNext();
    }

    // 执行异常分支
    context.addStepLog("执行失败，执行异常分支: %s", ExpUtil.getMsg(throwable));
    context.failStepLog(throwable);
    return exceptionConfig.getExceptionBranch();
  }

  /**
   * 处理流式输出过程中的异常
   */
  @SuppressWarnings("unchecked")
  protected final void processStreamException(SceneOrchestrationContext context, T step, Throwable throwable, @Nullable OrchestrationStepRunLog log) {
    // 转换异常
    BssException exception = convertException(throwable, step, context);
    // 检查异常处理策略配置
    StepExceptionProcessingStrategy strategy = step.getExceptionProcessingStrategy();
    // 中断流程
    if (strategy == StepExceptionProcessingStrategy.ABORT) {
      if (log != null) {
        log.fail(throwable);
      }
      throw exception;
    }

    context.setExceptionInfo(step, exception.getFailCode(), exception.getFailMsg());
    StepExceptionConfig exceptionConfig = step.getExceptionConfig();

    // 返回设定内容
    if (exceptionConfig.getStrategy() == StepExceptionProcessingStrategy.FALLBACK) {
      if (log != null) {
        log.addLog("执行失败，返回设定内容: %s", ExpUtil.getMsg(throwable));
      }
      Object output = exceptionConfig.parseFallbackValue(step, StepFallbackValueHelper::convertFallbackValue);
      context.setStepOutput(step, output, log);
      // 通过异常返回文本内容，供回复节点使用。只返回文本，不考虑其它出参（比如思考内容、参考文档等）
      String text = output instanceof Map ? MapUtils.getString((Map<String, Object>) output, "text") : null;
      throw new FallbackOutputException(text);
    }

    // 执行异常分支
    context.setExceptionInfo(step, exception.getFailCode(), exception.getFailMsg());
    if (log != null) {
      log.addLog("执行失败，执行异常分支: %s", ExpUtil.getMsg(throwable));
      log.fail(throwable);
    }
    throw new ExecuteExceptionBranchException(exceptionConfig.getExceptionBranch());
  }

  /**
   * 转换异常
   */
  private BssException convertException(Throwable throwable, T step, SceneOrchestrationContext context) {
    BssException exception;
    if (throwable instanceof BssException) {
      // 确保报错信息中包含服务名称和步骤名称
      exception = (BssException) throwable;
      exception.setFailMsg(buildFailMsg(context, throwable.getMessage(), step.getName()));
    }
    else {
      exception = new BssException(buildFailMsg(context, ExpUtil.getMsg(throwable), step.getName()), throwable);
    }
    return exception;
  }

  /**
   * 执行普通步骤，返回下一步的步骤编码
   *
   * <p>分支节点需要根据不同条件执行不同的分支，不能固定使用 step.getNext(), 可覆盖此方法</p>
   */
  @Nullable
  protected String doRunWithReturn(SceneOrchestrationContext context, T step) {
    doRun(context, step);
    return step.getNext();
  }

  /**
   * 执行普通步骤
   */
  protected void doRun(SceneOrchestrationContext context, T step) {
    throw new UnsupportedOperationException("不支持节点执行: " + step.getType());
  }

  /**
   * 作为大模型工具调用执行
   *
   * @param sceneChatParams 场景会话参数
   * @param step 步骤
   * @param toolCallId 工具调用标识
   * @param toolArguments 大模型组装的工具参数
   * @return 工具出参
   */
  @Nullable
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public Object runAsTool(SceneChatParamsDTO sceneChatParams, T step, String toolCallId, @Nullable Map<String, Object> toolArguments, Optional<OrchestrationStepRunLog> log) {
    throw new UnsupportedOperationException("不支持工具调用: " + step.getType());
  }

  /**
   * 单步调试单个节点
   *
   * <p>默认调用 doRun 方法，有特殊需要的节点需要覆盖此方法</p>
   */
  @SuppressWarnings("unchecked")
  public void debugStep(SceneOrchestrationContext context, T step, OrchestrationStepRunLog log) {
    doRun(context, step);

    // 处理流式输出
    if (step instanceof LlmStep || step instanceof AgentStep || step instanceof KnowledgeChatStep) {
      Object output = context.getStepOutput(step.getCode());
      if (output instanceof Map) {
        Object text = MapUtils.getObject((Map<String, Object>) output, "text");
        if (text instanceof SseInvoker) {
          context.getReplyHandler().stream((SseInvoker) text, step.getCode(), step.getName(), null);
        }
        else if (text instanceof String) {
          context.getReplyHandler().reply(ChatMessageType.TEXT, text, step.getCode(), step.getName());
        }
      }
    }
  }

  /**
   * 检查线程是否被中断
   */
  private boolean isInterrupted() {
    return Thread.currentThread().isInterrupted() || SceneContextUtil.getRootContext().isInterrupted();
  }

  /**
   * 检查执行限制
   */
  private void checkExecutionLimit(SceneOrchestrationContext context, AbstractStep step) {
    // 使用最外层的工作流检查
    SceneOrchestrationContext rootContext = SceneContextUtil.getRootContext();
    // 节点执行次数限制，防止死循环
    int maxNodeCount = SystemParameter.FLOW_EXECUTION_NODE_LIMIT.getRequiredIntegerValueFromDb();
    if (maxNodeCount > 0 && rootContext.getStepExecutionCount().incrementAndGet() > maxNodeCount) {
      throw new BssException(buildFailMsg(context, "节点执行次数超过限制，请检查是否存在死循环", step.getName()));
    }
  }

  /**
   * 构造失败信息，确保失败信息中包含场景名称、步骤名称
   */
  protected final String buildFailMsg(SceneOrchestrationContext context, String originalFailMsg, String stepName) {
    StringBuilder failMsg = new StringBuilder();
    String sceneNameKeyword = "【" + context.getDsl().getName() + "】";
    if (!Strings.CS.contains(originalFailMsg, sceneNameKeyword)) {
      failMsg.append(context.getRequest().getFlowId() != null ? "工作流" : "智能体").append(sceneNameKeyword);
    }
    if (!Strings.CS.contains(originalFailMsg, "步骤【")) {
      failMsg.append("步骤【").append(stepName).append("】");
    }
    return failMsg.append(originalFailMsg).toString();
  }

  /**
   * 执行步骤，及其所有后继步骤
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public static void executeSteps(SceneOrchestrationContext context, @Nullable String stepCode) {
    String nextStepCode = stepCode;
    AbstractStep step;
    AbstractStepRunner runner;
    while (StringUtils.isNotEmpty(nextStepCode)) {
      step = context.findStep(nextStepCode);
      runner = SceneStepRegistry.getRunner(step);
      nextStepCode = runner.run(step);
      if (context.isReturned()) {
        break;
      }
    }
  }

  /**
   * 获取有效的模型 ID
   */
  protected final Long getEffectiveModelId(Long tenantId, @Nullable Long modelId) {
    if (modelId == null || ModelConsts.DEFAULT_MODEL.equals(modelId)) {
      return tenantSettingInfoCache.getModelId(tenantId);
    }
    return modelId;
  }

  /**
   * 获取有效的模型 ID
   */
  protected final Long getEffectiveModelId(SceneOrchestrationContext context, @Nullable String modelIdOrExpr) {
    if (StringUtils.isEmpty(modelIdOrExpr) || ModelConsts.DEFAULT_MODEL.toString().equals(modelIdOrExpr)) {
      return tenantSettingInfoCache.getModelId(context.getTenantId());
    }
    Long resolvedModelId;
    if (modelIdOrExpr.startsWith("$")) {
      // 解析表达式
      Object value = SceneParamUtil.getParamValue(modelIdOrExpr);
      if (ObjectUtils.isEmpty(value)) {
        resolvedModelId = null;
      }
      else if (value instanceof Number) {
        resolvedModelId = ((Number) value).longValue();
      }
      else if (value instanceof String str) {
        Assert.isTrue(NumberUtils.isCreatable(str), () -> "模型 ID 解析结果不合法: " + str);
        resolvedModelId = Long.parseLong(str);
      }
      else {
        throw new IllegalArgumentException("模型 ID 解析结果不合法，应返回字符串或整数，实际为 " + value.getClass().getName() + ": " + value);
      }
    }
    else if (NumberUtils.isCreatable(modelIdOrExpr)) {
      resolvedModelId = Long.parseLong(modelIdOrExpr);
    }
    else {
      throw new IllegalArgumentException("模型 ID 不合法: " + modelIdOrExpr);
    }
    return getEffectiveModelId(context.getTenantId(), resolvedModelId);
  }

  /**
   * 获取有效的模型 ID
   */
  protected final Long getEffectiveModelId(Long tenantId, @Nullable String modelIdOrExpr) {
    if (StringUtils.isEmpty(modelIdOrExpr) || ModelConsts.DEFAULT_MODEL.toString().equals(modelIdOrExpr)) {
      return tenantSettingInfoCache.getModelId(tenantId);
    }
    // 本方法用于自主规划模式智能体调用技能，这种情况不支持表达式
    Assert.isTrue(NumberUtils.isCreatable(modelIdOrExpr), () -> "modelId 不合法: " + modelIdOrExpr);
    return getEffectiveModelId(tenantId, Long.parseLong(modelIdOrExpr));
  }

  /**
   * 解析提示词
   *
   * @param modelId 大模型 ID
   * @param promptId 提示词 ID
   * @param promptParameters 提示词参数
   * @param promptContent 自定义提示词内容
   * @return 提示词
   */
  @Nullable
  protected final String resolvePrompt(Long tenantId, Long modelId, @Nullable Long promptId, @Nullable List<ParameterSpec> promptParameters,
                                       @Nullable String promptContent) {
    if (promptId != null) {
      return ScenePromptUtil.resolvePrompt(tenantId, modelId, promptId, name -> {
        ParameterSpec spec = IterableUtils.find(promptParameters, p -> p.getName().equals(name));
        return spec != null ? SceneParamUtil.getParamValue(spec.getValue()) : null;
      });
    }
    return resolveTemplate(promptContent);
  }

  /**
   * 解析模板字符串，替换里面的变量引用
   */
  @Nullable
  protected final String resolveTemplate(@Nullable String template) {
    if (StringUtils.isEmpty(template)) {
      return null;
    }
    return TemplateUtil.resolveTemplate(template, this::resolveTemplateParam);
  }

  /**
   * 解析模板字符串中的参数
   *
   * @param expression 参数表达式
   * @return 参数的值
   */
  @Nullable
  protected final Object resolveTemplateParam(String expression) {
    String convertedExpression = "$." + expression;
    Object value = SceneParamUtil.getParamValue(convertedExpression);
    // 不支持的表达式原样返回，避免把 ${expression} 转为 $.expression
    if (convertedExpression.equals(value)) {
      return "${" + expression + "}";
    }
    return value;
  }

  /**
   * 构造请求参数为 Map
   */
  protected final Map<String, Object> buildRequestParametersToMap(@Nullable ParameterSpec root) {
    Assert.isTrue(root == null || root.isObject(), "根节点必须是对象");
    if (root == null) {
      return Collections.emptyMap();
    }
    Map<String, Object> map = resolveObjectParameterValue("", root);
    return map != null ? map : Collections.emptyMap();
  }

  /**
   * 解析参数的值
   *
   * <p>通过递归处理嵌套结构</p>
   */
  @Nullable
  protected final Object resolveParameterValue(String propertyPath, ParameterSpec spec) {
    if (spec.isObject()) {
      // 特殊参数格式内容，进行值处理
      if (StringUtils.isNotEmpty(spec.getFormat())) {
        return PluginParameterHelper.resolveParameterValue(spec, SceneContextUtil.getContext().getTenantId());
      }
      return resolveObjectParameterValue(propertyPath, spec);
    }
    else if (spec.isList()) {
      return resolveListParameterValue(propertyPath, spec);
    }
    Object value = SceneParamUtil.getParamValue(spec.getValue());
    // 支持默认值
    if (value == null && StringUtils.isNotEmpty(spec.getDefaultValue())) {
      value = spec.getDefaultValue();
    }
    value = AttrDataType.convert(propertyPath, spec.getType(), value);
    return value;
  }

  /**
   * 解析对象类型参数的值
   */
  @Nullable
  protected final Map<String, Object> resolveObjectParameterValue(String propertyPath, ParameterSpec spec) {
    // 支持将另一个对象直接赋值给当前对象
    if (StringUtils.isNotEmpty(spec.getValue())) {
      return resolveObjectParameterValueByValue(propertyPath, spec);
    }
    // 如果未配置子节点，或者子节点都未赋值，直接返回 null
    if (!spec.hasChildrenAssignment()) {
      return null;
    }
    Map<String, Object> map = new LinkedHashMap<>(spec.getChildren().size());
    for (ParameterSpec child : spec.getChildren()) {
      Object value = resolveParameterValue(propertyPath + "." + child.getName(), child);
      map.put(child.getName(), value);
    }
    return map;
  }

  /**
   * 根据对象的 value 解析对象类型参数的值
   *
   * <p>用于将取值表达式表示的另一个对象直接赋值给当前对象</p>
   */
  @Nullable
  protected final Map<String, Object> resolveObjectParameterValueByValue(String propertyPath, ParameterSpec spec) {
    Object value = SceneParamUtil.getParamValue(spec.getValue());
    if (ObjectUtils.isEmpty(value) && !spec.hasChildrenAssignment()) {
      return null;
    }
    Map<String, Object> object = ParamConverterUtil.convertObject(propertyPath, spec, value);
    // 保证 map 可写，且修改时不会影响原对象
    Map<String, Object> result = MapUtils.isNotEmpty(object) ? new LinkedHashMap<>(object) : new LinkedHashMap<>();
    // 支持按对象赋值后再单独给部分属性赋值
    // 暂时只支持第一层属性，不支持单独给嵌套的第二层属性赋值
    if (spec.hasChildren()) {
      for (ParameterSpec child : spec.getChildren()) {
        // 只处理配置了取值表达式的属性
        if (StringUtils.isNotEmpty(child.getValue())) {
          result.put(child.getName(), resolveParameterValue(propertyPath + "." + child.getName(), child));
        }
      }
    }
    return result;
  }

  /**
   * 解析列表类型参数的值
   */
  @Nullable
  protected final List<Object> resolveListParameterValue(String propertyPath, ParameterSpec spec) {
    String value = StringUtils.trimToNull(spec.getValue());
    // 未赋值时直接返回 null
    if (StringUtils.isEmpty(value)) {
      return null;
    }
    Object list;
    // 如果是赋值表达式，只支持将另一个列表直接赋值给当前列表，不支持给列表的元素赋值
    if (value.startsWith("$.")) {
      list = SceneParamUtil.getParamValue(value);
    }
    // 支持 JSON 格式的常量值
    else if (value.startsWith("[") && value.endsWith("]")) {
      list = JsonUtil.parseJsonRequired(value, List.class);
    }
    // 如果数组元素是属性，支持逗号分隔形式的常量值
    else if (!spec.hasChildren() || spec.getArrayElement().isProperty()) {
      list = Arrays.asList(value.split("\\s*,\\s*"));
    }
    else {
      throw new BssException("非法的数组赋值: property=" + propertyPath + ", value=" + value);
    }
    return ParamConverterUtil.convertList(propertyPath, spec, list);
  }

  /**
   * 解析文件
   *
   * @param expression 文件地址或文件 ID(取值表达式), 常量值只能是一个文件，引用变量时可以是多个文件
   * @return 文件列表，元素是字符串（文件地址)或 FileInfoVO 对象
   */
  protected final List<AbstractFile> resolveFiles(@Nullable String expression) {
    Object values = SceneParamUtil.getParamValue(expression);
    if (ObjectUtils.isEmpty(values)) {
      return List.of();
    }
    // 多个文件
    if (values instanceof Collection) {
      return ((Collection<?>) values).stream().map(this::resolveSingleFile).collect(Collectors.toList());
    }
    // 单个文件
    return Collections.singletonList(resolveSingleFile(values));
  }

  /**
   * 构造文件消息内容
   */
  private AbstractFile resolveSingleFile(Object value) {
    // 字符串，可以是文件地址或文件 ID
    if (value instanceof String str) {
      // 文件地址
      //noinspection HttpUrlsUsage
      if (str.startsWith("http://") || str.startsWith("https://")) {
        try {
          URL url = new URI(str).toURL();
          return new UrlFile(url);
        }
        catch (URISyntaxException | MalformedURLException e) {
          throw new BssException("非法的文件地址: " + str, e);
        }
      }
      // data URL
      else if (str.startsWith("data:")) {
        return new DataUrlFile(str);
      }
      // 纯数字当作文件 ID
      if (StringUtils.isNumeric(str)) {
        Long fileId = Long.parseLong(str);
        return resolveFileByFileId(fileId);
      }
      throw new BssException("非法的文件地址: " + value);
    }
    // 数值类型当作文件 ID
    else if (value instanceof Number) {
      Long fileId = ((Number) value).longValue();
      return resolveFileByFileId(fileId);
    }
    throw new BssException("非法的文件赋值: type=" + value.getClass().getCanonicalName() + ", value=" + value);
  }

  /**
   * 根据文件 ID 构造文件消息内容
   */
  private FileServerFile resolveFileByFileId(Long fileId) {
    FileInfoVO fileInfo = fileStoreService.getFileInfoById(fileId);
    Assert.notNull(fileInfo, () -> "文件不存在: " + fileId);
    return new FileServerFile(fileInfo);
  }

  /**
   * 添加记忆内容
   */
  protected final void addMemoryContent(Map<String, Object> output, Object parameters, @Nullable Boolean customMemorized, @Nullable String memoryContent) {
    output.put("memorized", true);
    // 开启自定义时，使用自定义的记忆内容
    if (Boolean.TRUE.equals(customMemorized)) {
      output.put("memoryContent", SceneParamUtil.getParamValue(memoryContent));
    }
    else {
      // 默认使用入参
      output.put("memoryContent", parameters);
    }
  }

  /**
   * 退出循环异常
   */
  public static class BreakLoopException extends RuntimeException {
    private static final long serialVersionUID = 1L;
  }

  /**
   * 继续循环异常
   */
  public static class ContinueLoopException extends RuntimeException {
    private static final long serialVersionUID = 1L;
  }

  /**
   * 退出并行异常
   */
  public static class BreakParallelException extends RuntimeException {
    private static final long serialVersionUID = 1L;
  }

  /**
   * 执行异常分支异常，流式输出失败时抛出，用于中止回复节点的执行，转而执行流式输出节点的异常分支
   */
  public static class ExecuteExceptionBranchException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    /** 异常分支的步骤编码 */
    @Getter
    private final String exceptionBranch;

    public ExecuteExceptionBranchException(String exceptionBranch) {
      this.exceptionBranch = exceptionBranch;
    }
  }

  /**
   * 返回默认值的异常
   */
  public static class FallbackOutputException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    /** 默认值 */
    @Getter
    private final String fallbackText;

    public FallbackOutputException(@Nullable String fallbackText) {
      this.fallbackText = StringUtils.defaultString(fallbackText);
    }
  }

}
