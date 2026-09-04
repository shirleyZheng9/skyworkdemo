package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.common.base.CaseFormat;
import com.iwhalecloud.bote.cache.ModelClientCache;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.ParamConverterUtil;
import com.iwhalecloud.bote.common.util.PluginContextUtil;
import com.iwhalecloud.bote.common.util.SignCreateUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.PluginExecuteParams;
import com.iwhalecloud.bote.dto.plugin.PluginExecutionContext;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.service.plugin.IPlugin;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 插件执行器
 *
 * @author qian.sisheng
 * @since 2025-04-09
 */
@SuppressWarnings("PMD.GuardLogStatement")
public abstract class AbstractPlugin<T> implements IPlugin {

  protected final Logger logger = LoggerFactory.getLogger(getClass());
  /** 插件参数类型 */
  private final Class<T> clazz;
  /** 请求参数 */
  private ParameterSpec requestParameter;
  /** 响应参数 */
  private ParameterSpec responseParameter;
  /** 驼峰转下划线 */
  protected static final ObjectMapper snakeCaseMapper = createObjectMapper();
  /** 模型客户端 */
  private static final ModelClientCache modelClientCache = SpringUtil.getBean(ModelClientCache.class);
  /** 租户设置信息缓存 */
  private static final TenantSettingInfoCache tenantSettingInfoCache = SpringUtil.getBean(TenantSettingInfoCache.class);
  /** 博特 API */
  private static final String BOTE_API_URL = SystemParameter.BOTE_API_URL.getValueFromEnv();

  public AbstractPlugin(Class<T> clazz) {
    this.clazz = clazz;
  }

  /**
   * 获取请求参数
   *
   * @return 请求参数
   */
  public ParameterSpec getRequestParameter() {
    if (requestParameter == null) {
      requestParameter = createRequestParameter();
    }
    return requestParameter;
  }

  /**
   * 获取响应参数
   *
   * @return 响应参数
   */
  public ParameterSpec getResponseParameter() {
    if (responseParameter == null) {
      responseParameter = createResponseParameter();
    }
    return responseParameter;
  }

  /**
   * 获取插件编码
   *
   * @return 插件编码
   */
  @Override
  public abstract String getPluginCode();

  /**
   * 创建请求参数
   *
   * @return 请求参数
   */
  public abstract ParameterSpec createRequestParameter();

  /**
   * 创建响应参数
   *
   * @return 响应参数
   */
  public abstract ParameterSpec createResponseParameter();

  @Override
  public Object execute(PluginExecuteParams params) {
    try {
      // 创建并设置执行上下文
      PluginExecutionContext context = new PluginExecutionContext(params);
      PluginContextUtil.setContext(context);

      T pluginParams = convertParams(context.getPluginParams());
      // 参数校验
      validateParams(pluginParams);
      Object data = doRun(pluginParams);
      return convertOutputParams(data);
    }
    catch (Exception e) {
      logger.error("Execute plugin failed: pluginCode={}", getPluginCode(), e);
      throw new BssException("插件执行失败：" + e.getMessage(), e);
    }
    finally {
      // 清理执行上下文
      PluginContextUtil.removeContext();
    }
  }

  /**
   * 校验参数
   *
   * @param params 插件特定参数
   */
  public abstract void validateParams(T params);

  /**
   * 执行插件
   *
   * @param pluginParams 插件参数
   * @return 结果
   */
  public abstract Object doRun(T pluginParams);

  /**
   * 转换参数
   *
   * @param params 执行参数
   * @return 插件特定参数
   */
  public T convertParams(Map<String, Object> params) {
    if (params == null) {
      return null;
    }
    Object data = ParamConverterUtil.convert("", getRequestParameter(), params);
    if (data == null) {
      return null;
    }
    return JsonUtil.convert(data, clazz);
  }

  /**
   * 转换插件输出参数
   */
  public final Object convertOutputParams(Object value) {
    if (value == null) {
      return null;
    }
    return ParamConverterUtil.convert("",  getResponseParameter(), JsonUtil.convert(value, Object.class));
  }

  /**
   * 将驼峰转成下划线
   *
   * @param camel 驼峰字符串
   * @return 下划线字符串
   */
  public String camelToUnderScore(String camel) {
    if (StringUtils.isBlank(camel)) {
      return camel;
    }
    return StringUtils.uncapitalize(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, camel));
  }

  /**
   * 创建驼峰转下划线 ObjectMapper
   *
   * @return ObjectMapper
   */
  public static ObjectMapper createObjectMapper() {
    ObjectMapper snakeCaseMapper = JsonUtil.getObjectMapper().copy();
    snakeCaseMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    return snakeCaseMapper;
  }

  /**
   * 获取当前执行上下文
   *
   * @return 当前执行上下文
   */
  protected PluginExecutionContext getCurrentContext() {
    return PluginContextUtil.getContext();
  }

  /**
   * 获取LLM客户端
   *
   * @return LLM客户端，如果modelId为空则返回null
   */
  @Nullable
  protected LlmClient getLlmClient() {
    PluginExecutionContext context = getCurrentContext();
    if (context == null || !context.supportsLlm()) {
      return null;
    }
    return getLlmClient(context.getTenantId(), context.getModelId());
  }


  /**
   * 获取LLM客户端
   *
   * @return LLM客户端，如果modelId为空则返回null
   */
  private LlmClient getLlmClient(Long tenantId, Long modelId) {
    Assert.notNull(tenantId, "租户ID 不能为空");
    Assert.notNull(modelId, "模型ID 不能为空");
    Long actualModelId = ModelConsts.DEFAULT_MODEL.equals(modelId) ? tenantSettingInfoCache.getModelId(tenantId) : modelId;
    return modelClientCache.getLlmClient(tenantId, actualModelId);
  }

  /**
   * 获取文件访问URL，并生成签名参数
   */
  protected String getFileUrl(Long fileId) {
    String path = "/bote/file/file/id/" + fileId;
    String fileUrl = StringUtils.stripEnd(BOTE_API_URL, "/") + path;
    String sign = SignCreateUtil.createGetSign(path, null);
    return fileUrl + "?X-SIGN=" + sign + "&XA-TYPE=1.0";
  }
}
