package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.enums.ParamSourceType;
import com.iwhalecloud.bote.dto.base.EnvVariableValDTO;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.service.base.IEnvVariableManageService;
import com.iwhalecloud.bote.service.orchestration.reply.ReplyHandler;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;

/**
 * 场景参数工具类
 *
 * @author bianjp
 * @since 2024-08-29
 */
@SuppressWarnings("PMD.UnusedFormalParameter")
public abstract class SceneParamUtil {
  private static final Logger logger = LoggerFactory.getLogger(SceneParamUtil.class);
  /** 变量值解析器映射 */
  private static final Map<ParamSourceType, ParamResolver> paramResolvers = new LinkedHashMap<>();
  /** 系统变量生成器，key 为变量名称，比如 now, uuid */
  private static final Map<String, Supplier<Object>> systemVariableGenerators = new LinkedHashMap<>();
  /** 登录信息变量提取器，key 为变量名称，比如 userId, userName */
  private static final Map<String, Function<LoginInfo, Object>> sessionVariableExtractors = new LinkedHashMap<>();
  /** 项目环境变量管理服务 */
  private static final IEnvVariableManageService envVarService = SpringUtil.getBean(IEnvVariableManageService.class);

  static {
    // 入参，形式为 $.input[.PROPERTY_PATH], PROPERTY_PATH 是变量的访问路径（嵌套属性使用 . 表示层级），未指定时返回所有入参
    // 示例: $.input.custId, $.input.customer.custId
    paramResolvers.put(ParamSourceType.INPUT, SceneParamUtil::getInputParamValue);
    // 步骤输出，形式为 $.step.STEP_CODE[.PROPERTY_PATH], STEP_CODE 是步骤编码，PROPERTY_PATH 是变量的访问路径（嵌套属性使用 . 表示层级）
    paramResolvers.put(ParamSourceType.STEP_OUTPUT, SceneParamUtil::getStepOutputValue);
    // 循环变量，形式为 $.loop.STEP_CODE[.PROPERTY_PATH], STEP_CODE 为循环步骤的编码, PROPERTY_PATH 是变量的访问路径（嵌套属性使用 . 表示层级）
    // PROPERTY_PATH 为空时返回循环变量自身
    paramResolvers.put(ParamSourceType.LOOP_VARIABLE, SceneParamUtil::getLoopVariableValue);
    // 全局变量，形式为 $.variable.VARIABLE_NAME[.PROPERTY_PATH], VARIABLE_NAME 为变量名称
    paramResolvers.put(ParamSourceType.GLOBAL_VARIABLE, SceneParamUtil::getLocalVariable);
    // 系统变量，形式为 $.system.VARIABLE_NAME, VARIABLE_NAME 为变量名称
    paramResolvers.put(ParamSourceType.SYSTEM_VARIABLE, SceneParamUtil::getSystemVariable);
    // 上下文变量，形式为 $.context.VARIABLE_NAME, VARIABLE_NAME 为变量名称
    paramResolvers.put(ParamSourceType.CONTEXT_VARIABLE, SceneParamUtil::getContextVariable);
    // 登录信息，形式为 $.session[.PROPERTY_PATH], PROPERTY_PATH 是变量的访问路径（嵌套属性使用 . 表示层级）
    paramResolvers.put(ParamSourceType.SESSION, SceneParamUtil::getSessionVariable);
    // 项目环境变量，形式为 $.envVar.VARIABLE_NAME, VARIABLE_NAME 为变量名称
    paramResolvers.put(ParamSourceType.ENV_VARIABLE, SceneParamUtil::getEnvVariable);

    // 系统变量生成器
    systemVariableGenerators.put("now", Date::new);
    systemVariableGenerators.put("today", LocalDate::now);
    systemVariableGenerators.put("uuid", () -> UUID.randomUUID().toString());
    systemVariableGenerators.put("query", () -> SceneContextUtil.getContext().getRequest().getMessageContent());
    systemVariableGenerators.put("fileIds", () -> SceneContextUtil.getContext().getRequest().getFileIds());
    systemVariableGenerators.put("cookie", () -> {
      HttpServletRequest request = ServletUtil.getRequest();
      return request != null ? request.getHeader(HttpHeaders.COOKIE) : null;
    });
    systemVariableGenerators.put("replied", () -> {
      ReplyHandler replyHandler = SceneContextUtil.getRootContext().getReplyHandler();
      return replyHandler != null && CollectionUtils.isNotEmpty(replyHandler.getReplies());
    });
    systemVariableGenerators.put("chatSessionId",
    () -> SceneContextUtil.getContext().getRequest().getConversationId());

    // 登录信息变量提取器
    // 优先取外系统用户 ID
    sessionVariableExtractors.put("userId", loginInfo -> ObjectUtils.getIfNull(loginInfo.getExtUserId(), loginInfo.getUserId()));
    sessionVariableExtractors.put("userName", LoginInfo::getUserName);
    sessionVariableExtractors.put("realName", LoginInfo::getRealName);
    sessionVariableExtractors.put("attributes", LoginInfo::getAttributes);
    sessionVariableExtractors.put("sessionId", LoginInfo::getToken);
  }

  /**
   * 获取参数值
   *
   * @param spec 参数描述
   * @return 参数值
   */
  @Nullable
  public static Object getParamValue(@Nullable String spec) {
    if (StringUtils.isEmpty(spec)) {
      return null;
    }
    Object result = doGetParamValue(spec);
    if (result == null) {
      logger.warn("No param found: spec={}", spec);
    }
    return result;
  }

  /**
   * 获取参数值底层逻辑
   */
  @Nullable
  private static Object doGetParamValue(String spec) {
    // 字面量
    if (!spec.startsWith("$.")) {
      return spec;
    }
    // 使用第一个匹配的解析器
    for (Entry<ParamSourceType, ParamResolver> entry : paramResolvers.entrySet()) {
      if (spec.startsWith(entry.getKey().getPrefix())) {
        return entry.getValue().resolve(entry.getKey().removePrefix(spec));
      }
    }
    // 没有匹配的解析器时当作字面量，字符串形式
    return spec;
  }

  /**
   * 从循环变量获取属性值
   */
  @Nullable
  private static Object getLoopVariableValue(String propertySpec) {
    if (StringUtils.isEmpty(propertySpec)) {
      throw new BssException("非法的循环变量引用: " + ParamSourceType.LOOP_VARIABLE.getPrefix() + propertySpec);
    }
    String[] parts = StringUtils.split(propertySpec, ".", 2);
    String variableName = parts[0];
    String propertyPath = parts.length > 1 ? parts[1] : null;
    Object value = SceneContextUtil.getContext().getLoopVariable(variableName);
    if (value == null) {
      return null;
    }
    // 未指定属性路径时返回变量自身
    if (StringUtils.isEmpty(propertyPath)) {
      return value;
    }

    return ParamUtil.getNestedProperty(value, propertyPath);
  }

  /**
   * 从入参获取属性值
   *
   * @param propertyPath 入参名称
   * @return 属性值
   */
  @Nullable
  private static Object getInputParamValue(String propertyPath) {
    SceneOrchestrationContext context = SceneContextUtil.getContext();
    if ("USER_INPUT".equals(propertyPath)) {
      // 约定的入参变量，标识用户消息内容
      return context.getRequest().getMessageContent();
    }
    Map<String, Object> inputParameters = context.getParsedInputParameters();
    if (MapUtils.isEmpty(inputParameters)) {
      return null;
    }

    return ParamUtil.getNestedProperty(inputParameters, propertyPath);
  }

  /**
   * 从步骤输出获取属性值
   */
  @Nullable
  private static Object getStepOutputValue(String propertyPath) {
    SceneOrchestrationContext context = SceneContextUtil.getContext();
    String[] pieces = StringUtils.split(propertyPath, ".", 2);
    if (pieces == null || pieces.length < 1) {
      logger.warn("Illegal property path for step output: {}", propertyPath);
      throw new BssException("非法的步骤出参引用: " + propertyPath);
    }

    String stepCode = pieces[0];
    String path = pieces.length > 1 ? pieces[1] : null;
    if (path == null) {
      return context.getStepOutput(stepCode);
    }

    // 特殊处理对节点异常信息的引用
    if (SceneConsts.EXCEPTION_INFO_KEY.equals(path) || path.startsWith(SceneConsts.EXCEPTION_INFO_PROPERTY_PATH_PREFIX)) {
      Map<String, Object> exceptionInfo = context.getExceptionInfo(stepCode);
      // 只处理有异常的情况，没异常当作普通的出参
      if (exceptionInfo != null) {
        if (SceneConsts.EXCEPTION_INFO_KEY.equals(path)) {
          return exceptionInfo;
        }
        return ParamUtil.getNestedProperty(exceptionInfo, path.substring(SceneConsts.EXCEPTION_INFO_PROPERTY_PATH_PREFIX.length()));
      }
    }

    return ParamUtil.getNestedProperty(context.getStepOutput(stepCode), path);
  }

  /**
   * 从本地变量获取属性值
   */
  @Nullable
  private static Object getLocalVariable(String propertyPath) {
    SceneOrchestrationContext context = SceneContextUtil.getContext();
    return ParamUtil.getNestedProperty(context.getGlobalVariables(), propertyPath);
  }

  /**
   * 从系统变量获取属性值
   */
  @Nullable
  private static Object getSystemVariable(String propertyPath) {
    Supplier<Object> supplier = systemVariableGenerators.get(propertyPath);
    if (supplier == null) {
      throw new BssException("未知的系统变量: " + propertyPath);
    }
    return supplier.get();
  }

  /**
   * 从上下文变量获取属性值
   */
  @Nullable
  private static Object getContextVariable(String propertyPath) {
    SceneOrchestrationContext context = SceneContextUtil.getContext();
    Map<String, Object> contextParams = context.getRequest().getContextParams();
    if (MapUtils.isEmpty(contextParams)) {
      return null;
    }
    return ParamUtil.getNestedProperty(contextParams, propertyPath);
  }

  /**
   * 从登录信息获取属性值
   */
  @Nullable
  private static Object getSessionVariable(String propertyPath) {
    // 获取登录信息
    LoginInfo loginInfo = SessionUtil.getOptionalLoginInfo();
    if (loginInfo == null) {
      return null;
    }

    // 未指定具体属性时返回登录信息 JSON 字符串
    if (StringUtils.isEmpty(propertyPath)) {
      return JsonUtil.toJsonString(loginInfo);
    }
    Function<LoginInfo, Object> extractor = sessionVariableExtractors.get(propertyPath);
    if (extractor != null) {
      return extractor.apply(loginInfo);
    }
    return ParamUtil.getNestedProperty(loginInfo, propertyPath);
  }

  /**
   * 从项目环境变量中解析变量值
   */
  @Nullable
  private static Object getEnvVariable(String propertyPath) {
    SceneOrchestrationContext context = SceneContextUtil.getContext();
    Long tenantId = context.getRequest().getTenantId();
    if (tenantId == null) {
      throw new BssException("解析项目环境变量失败，租户ID不能为空");
    }
    // 查询项目环境变量值
    EnvVariableValDTO valueDTO = envVarService.getEnvVariableValueByCode(propertyPath, tenantId);
    if (valueDTO == null) {
      logger.error("No env variable value found: tenantId={}, variableCode={}", tenantId, propertyPath);
      return null;
    }
    // 根据变量类型转换值
    AttrDataType dataType = AttrDataType.ofCode(valueDTO.getDataType());
    if (dataType == null) {
      String message = String.format("未知的项目环境变量属性数据类型：attr=%s，type=%s", Objects.toString(propertyPath, ""), valueDTO.getDataType());
      throw new BssException(message);
    }
    return dataType.convert(propertyPath, valueDTO.getVariableVal());
  }

  /**
   * 参数值解析器
   */
  @FunctionalInterface
  interface ParamResolver {
    /**
     * 解析参数值
     *
     * @param propertyPath 属性路径
     * @return 参数值
     */
    @Nullable
    Object resolve(String propertyPath);
  }
}
