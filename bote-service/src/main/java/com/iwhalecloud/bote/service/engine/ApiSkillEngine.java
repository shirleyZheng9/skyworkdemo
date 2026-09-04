package com.iwhalecloud.bote.service.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.cache.ApiSkillCache;
import com.iwhalecloud.bote.cache.GatewayCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.operator.ConditionEvaluator;
import com.iwhalecloud.bote.common.sse.SseInvoker;
import com.iwhalecloud.bote.common.sse.SseUtil;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.common.util.AesUtil;
import com.iwhalecloud.bote.common.util.EnvUtil;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.GroovyUtil;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.ParamConverterUtil;
import com.iwhalecloud.bote.common.util.ParamUtil;
import com.iwhalecloud.bote.common.util.ServletUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.skill.ApiServiceParams;
import com.iwhalecloud.bote.dto.skill.SimpleServiceDTO;
import com.iwhalecloud.bote.dto.skill.SimpleServiceGatewayDTO;
import com.iwhalecloud.bote.dto.skill.SimpleServiceMockDTO;
import com.iwhalecloud.bote.llm.client.adapter.ChatCompletionEventSourceListener;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bote.portal.IAuthProvider;
import com.iwhalecloud.bote.service.plugin.impl.helper.PluginCallHelper;
import com.iwhalecloud.bote.service.plugin.impl.helper.PluginParameterHelper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.LogUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.RequestEntity.BodyBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 * API 技能执行引擎
 *
 * @author bianjp
 * @since 2024-12-13
 */
@Service
@RequiredArgsConstructor
public class ApiSkillEngine {
  private static final Logger logger = LoggerFactory.getLogger(ApiSkillEngine.class);

  private final ApiSkillCache apiSkillCache;
  private final GatewayCache gatewayCache;
  private final IAuthProvider authProvider;

  /**
   * 调用 API
   *
   * @param tenantId 租户 ID
   * @param serviceId API 服务 ID
   * @param params 请求参数
   * @return API 出参
   */
  @Nullable
  public Object execute(Long tenantId, Long serviceId, ApiServiceParams params) {
    Assert.notNull(serviceId, "serviceId 不能为空");
    // 查询服务配置
    SimpleServiceDTO service = apiSkillCache.get(tenantId, serviceId);
    Assert.notNull(service, () -> "服务不存在: serviceId=" + serviceId);
    return execute(service, params);
  }

  /**
   * 测试 API
   *
   * @param service API 服务
   * @param params 测试参数
   * @return API 出参
   */
  @Nullable
  public Object executeForTest(SimpleServiceDTO service, ApiServiceParams params) {
    // api表单测试时支持常量值数组
    if (MediaType.MULTIPART_FORM_DATA_VALUE.equals(service.getBodyType())) {
      ParameterSpec parameterSpec = IterableUtils.find(CollectionUtils.emptyIfNull(service.getRequest().getChildren()),
        child -> "body".equals(child.getName()));
      params.setBody(processListParameters(parameterSpec, params.getBody()));
    }
    return execute(service, params);
  }

  /**
   * api测试时需要支持常量值数组
   * <p>只处理第一层级的列表参数，不做深度递归</p>
   */
  @Nullable
  @SuppressWarnings("unchecked")
  private Object processListParameters(ParameterSpec spec, @Nullable Object value) {
    if (value == null || !spec.isObject() || !spec.hasChildren()) {
      return value;
    }
    // 只处理对象类型的第一层级属性
    if (!(value instanceof Map)) {
      return value;
    }
    Map<String, Object> originalMap = (Map<String, Object>) value;
    Map<String, Object> processedMap = new LinkedHashMap<>(originalMap);
    // 遍历对象的直接属性，只处理列表类型
    for (ParameterSpec property : spec.getChildren()) {
      if (property.isList()) {
        Object propertyValue = originalMap.get(property.getName());
        if (propertyValue instanceof String) {
          List<Object> convertedList = parseStringToList(property, propertyValue);
          if (convertedList != null) {
            processedMap.put(property.getName(), convertedList);
          }
        }
      }
    }
    return processedMap;
  }

  /**
   * 字符串转列表
   */
  @Nullable
  @SuppressWarnings("unchecked")
  private List<Object> parseStringToList(ParameterSpec spec, @Nullable Object value) {
    String listValue = StringUtils.trimToNull((String) value);
    if (StringUtils.isEmpty(listValue)) {
      return null;
    }
    // 支持 JSON 格式的常量值
    if (listValue.startsWith("[") && listValue.endsWith("]")) {
      return JsonUtil.parseJsonRequired(listValue, List.class);
    }
    // 如果数组元素是属性，支持逗号分隔形式的常量值
    if (!spec.hasChildren() || spec.getArrayElement().isProperty()) {
      Object[] pieces = listValue.split("\\s*,\\s*");
      return Arrays.asList(pieces);
    }
    return null;
  }

  /**
   * 调用 API
   *
   * @param service API 服务
   * @param params 请求参数
   * @return API 出参
   */
  @Nullable
  @SuppressWarnings("PMD.GuardLogStatement")
  public Object execute(SimpleServiceDTO service, ApiServiceParams params) {
    // 转换入参结构
    ApiServiceParams convertedParams = convertRequestParams(service, params);

    // 检查是否开启接口模拟
    if (Boolean.TRUE.equals(service.getMockEnabled())) {
      return buildMockResponse(service, convertedParams);
    }

    // 构造请求
    RequestEntity<?> requestEntity = buildRequest(service, convertedParams);

    // 特殊处理 SSE 接口
    if (Boolean.TRUE.equals(service.getSse())) {
      HttpUrl url = HttpUrl.get(requestEntity.getUrl());
      HttpMethod method = requestEntity.getMethod();
      assert url != null;
      assert method != null;
      HttpHeaders headers = requestEntity.getHeaders();
      Headers okHttpHeaders = !headers.isEmpty() ? Headers.of(headers.toSingleValueMap()) : null;
      return new SseInvoker((partialHandler) -> {
        ChatCompletionEventSourceListener listener = ChatCompletionEventSourceListener.builder()
          .url(url)
          .partialHandler(response -> {
            String content = response.getDeltaContent();
            if (StringUtils.isNotEmpty(content)) {
              partialHandler.accept(SseEvent.ofText(content));
            }
          })
          .build();
        ModelHttpClient.sseBlocking(method.name(), url, okHttpHeaders, requestEntity.getBody(), SseUtil.requestListener, listener::onEvent);
      });
    }

    // 调用普通 HTTP 接口
    return executeHttpCall(service, requestEntity, params);
  }

  /**
   * 执行 HTTP 调用（加密处理 + 异常处理 + 后置处理）
   */
  @Nullable
  @SuppressWarnings("PMD.GuardLogStatement")
  private Object executeHttpCall(SimpleServiceDTO service, RequestEntity<?> requestEntity, ApiServiceParams params) {
    HttpMethod method = requestEntity.getMethod();
    URI uri = requestEntity.getUrl();
    logger.debug("Request start: {} {} data={}", method, uri, LogUtil.limitLength(requestEntity.getBody()));
    // 调用接口
    Object data;
    ResponseEntity<?> responseEntity;
    String encryptedResponse = null;
    try {
      // 根据服务配置的超时时间创建 RestTemplate
      RestTemplate restTemplate = HttpUtil.createRestTemplateWithTimeout(service.getConnectTimeout(), service.getReadTimeout());
      if (Boolean.TRUE.equals(service.getEncrypt())) {
        // 如果启用了加密，响应体是加密字符串，需要用 String 接收，避免 JSON 解析失败
        responseEntity = restTemplate.exchange(requestEntity, String.class);
        encryptedResponse = Objects.toString(responseEntity.getBody(), "");
        data = decryptResponse(responseEntity.getBody());
      }
      else {
        responseEntity = restTemplate.exchange(requestEntity, Object.class);
        data = responseEntity.getBody();
      }
      logger.debug("Request end: {} url={}, status={}, response={}", method, uri, responseEntity.getStatusCode().value(),
        LogUtil.limitLength(Boolean.TRUE.equals(service.getEncrypt()) ? encryptedResponse : data));
    }
    catch (HttpStatusCodeException e) {
      logger.error("Request failed: {} url={}, status={}", method, uri, e.getStatusCode());
      throw BaseErrorConstant.REQUEST_FAIL.toException(e, "url=" + uri + ", error=" + e.getStatusCode());
    }
    catch (Exception e) {
      logger.error("Request failed: {} url={}", method, uri, e);
      throw BaseErrorConstant.REQUEST_FAIL.toException(e, "url=" + uri + ", error=" + ExpUtil.getMsg(e));
    }

    // 执行API 后置处理器
    data = executePostScript(service, responseEntity.getStatusCode().value(), responseEntity.getHeaders(), data, params);
    // 转换出参结构
    if (data != null && service.getResponse() != null) {
      data = ParamConverterUtil.convert("", service.getResponse(), data);
    }
    return data;
  }

  /**
   * 解密响应数据，并将解密后的数据转换成 Map 对象
   */
  @Nullable
  private Object decryptResponse(@Nullable Object respBody) {
    if (respBody == null) {
      return null;
    }
    // 响应体是字符串时才进行解密
    if (respBody instanceof String) {
      String key = SystemParameter.ENCRYPTION_AES.getValueFromDb();
      String decrypt = AesUtil.aesDecrypt((String) respBody, key);
      return decrypt == null ? null : JsonUtil.parseJson(decrypt, new TypeReference<Map<String, Object>>() { });
    }
    // 其他类型的响应体（如 Map、List 等）说明已经被 RestTemplate 解析，不需要解密
    return respBody;
  }

  /**
   * 执行后置脚本，用于替换大模型返回的消息内容
   */
  @Nullable
  private Object executePostScript(SimpleServiceDTO service, int httpStatusCode, HttpHeaders headers, @Nullable Object respBody, ApiServiceParams params) {
    if (StringUtils.isEmpty(service.getPostScript())) {
      return respBody;
    }
    // 把入参、响应头、响应体、状态码都放进去
    Map<String, Object> postScriptParams = new HashMap<>();
    postScriptParams.put("reqBody", params.getBody());
    postScriptParams.put("reqQuery", params.getQuery());
    postScriptParams.put("respHeader", headers.asSingleValueMap());
    postScriptParams.put("respHttpStatusCode", httpStatusCode);
    postScriptParams.put("respBody", respBody);
    return GroovyUtil.invoke(service.getPostScript(), postScriptParams);
  }

  /**
   * 构造模拟响应
   */
  @Nullable
  private Object buildMockResponse(SimpleServiceDTO service, ApiServiceParams convertedParams) {
    // 响应数据
    Object result = null;
    // 是否有条件匹配的用例（允许用例返回 null）
    boolean hasMatchingCase = false;

    // 匹配用例
    if (CollectionUtils.isNotEmpty(service.getMocks())) {
      MockConditionParamResolver paramResolver = new MockConditionParamResolver(convertedParams);
      for (SimpleServiceMockDTO mock : service.getMocks()) {
        if (ConditionEvaluator.evaluate(mock.getCondition(), paramResolver)) {
          result = mock.getResponse();
          hasMatchingCase = true;
          break;
        }
      }
    }

    // 没有条件匹配的用例时，使用默认模拟报文
    if (!hasMatchingCase) {
      result = service.getDefaultMockResponse();
    }
    // 转换出参结构
    if (result != null && service.getResponse() != null) {
      return ParamConverterUtil.convert("", service.getResponse(), result);
    }
    return result;
  }

  /**
   * 转换请求参数
   */
  private ApiServiceParams convertRequestParams(SimpleServiceDTO service, ApiServiceParams params) {
    if (service.getRequest() == null || !service.getRequest().hasChildren()) {
      return params;
    }
    ApiServiceParams convertedParams = new ApiServiceParams();
    for (ParameterSpec child : service.getRequest().getChildren()) {
      // path, header, query 必须是对象，body 可以是任意类型
      if ("path".equals(child.getName())) {
        convertedParams.setPath(ParamConverterUtil.convertObject(child.getName(), child, params.getPath()));
      }
      else if ("header".equals(child.getName())) {
        convertedParams.setHeader(ParamConverterUtil.convertObject(child.getName(), child, params.getHeader()));
      }
      else if ("query".equals(child.getName())) {
        convertedParams.setQuery(ParamConverterUtil.convertObject(child.getName(), child, params.getQuery()));
      }
      else if ("body".equals(child.getName())) {
        convertedParams.setBody(ParamConverterUtil.convert(child.getName(), child, params.getBody()));
      }
    }
    return convertedParams;
  }

  /**
   * 构造请求
   */
  private RequestEntity<?> buildRequest(SimpleServiceDTO service, ApiServiceParams params) {
    String serviceId = Objects.toString(service.getServiceId(), "");
    // 查询网关地址
    Assert.notNull(service.getPlatformId(), () -> "服务未关联平台定义: serviceId=" + serviceId);
    SimpleServiceGatewayDTO gateway = gatewayCache.get(service.getTenantId(), service.getPlatformId());
    Assert.notNull(gateway, () -> "服务关联的平台未定义网关环境: serviceId=" + serviceId + ", env=" + EnvUtil.getEnvCode());
    Assert.hasLength(gateway.getUrl(), () -> "服务关联的平台未配置环境地址: serviceId=" + serviceId + ", env=" + EnvUtil.getEnvCode());

    // 处理请求参数
    processRequestParams(service, params);
    // 请求方法
    HttpMethod method = HttpMethod.valueOf(service.getReqMethod().toUpperCase());
    // 请求地址
    URI uri = PluginCallHelper.buildUrl(gateway.getUrl(), service.getRelativePath(), params.getPath(), params.getQuery());
    // 请求头
    HttpHeaders headers = buildHeaders(gateway.getHeaders(), params.getHeader());
    if (service.getEncrypt()) {
      // 接口加密时，需要添加 xa-type 加密请求头
      headers.add(BaseConsts.HEADER_KEY_SIGN_SECURITY_MODE, BaseConsts.AES);
    }
    // 请求体
    Object body = method == HttpMethod.GET ? null : params.getBody();
    body = processRequestBody(service, headers, body);

    // 非 GET 请求且有请求体时默认设置为 JSON 格式
    if (body != null && headers.getContentType() == null) {
      headers.setContentType(MediaType.APPLICATION_JSON);
    }

    BodyBuilder builder = RequestEntity.method(method, uri).headers(headers);
    return body == null ? builder.build() : builder.body(body);
  }

  /**
   * 处理请求参数，比如参数加密
   */
  private void processRequestParams(SimpleServiceDTO service, ApiServiceParams params) {
    if (service.getEncrypt() && MapUtils.isNotEmpty(params.getQuery())) {
      String key = SystemParameter.ENCRYPTION_AES.getValueFromDb();
      for (Entry<String, Object> entry : params.getQuery().entrySet()) {
        entry.setValue(AesUtil.aesEncrypt(Objects.toString(entry.getValue(), ""), key));
      }
    }
  }

  /**
   * 处理请求体，比如参数加密
   */
  @Nullable
  private Object processRequestBody(SimpleServiceDTO service, HttpHeaders headers, @Nullable Object body) {
    if (MediaType.MULTIPART_FORM_DATA_VALUE.equals(service.getBodyType()) && body != null) {
      body = PluginParameterHelper.buildMultipartBody(body);
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      // multipart 类型请求体加密：只加密非文件资源
      if (service.getEncrypt()) {
        body = encryptMultipartBody(body);
      }
    }
    else if (body != null && service.getEncrypt()) {
      // 其它类型的请求体加密：加密整个请求体
      String key = SystemParameter.ENCRYPTION_AES.getValueFromDb();
      body = AesUtil.aesEncrypt(JsonUtil.toJsonString(body), key);
    }
    return body;
  }

  /**
   * 加密 multipart 请求体中的非文件资源字段
   */
  @SuppressWarnings("unchecked")
  private MultiValueMap<String, Object> encryptMultipartBody(Object body) {
    Assert.isTrue(body instanceof Map, "表单数据必须是 Map");
    String key = SystemParameter.ENCRYPTION_AES.getValueFromDb();
    MultiValueMap<String, Object> encryptedBody = new LinkedMultiValueMap<>();

    for (Entry<String, List<Object>> entry : ((Map<String, List<Object>>) body).entrySet()) {
      String fieldName = entry.getKey();
      List<Object> values = entry.getValue();

      for (Object value : values) {
        // 文件资源不加密
        if (value instanceof Resource) {
          encryptedBody.add(fieldName, value);
        }
        else {
          // 非文件资源字段加密
          String stringValue = value instanceof String ? (String) value : JsonUtil.toJsonString(value);
          String encrypted = AesUtil.aesEncrypt(stringValue, key);
          encryptedBody.add(fieldName, encrypted);
        }
      }
    }
    return encryptedBody;
  }

  /**
   * 构造请求头
   */
  private HttpHeaders buildHeaders(@Nullable Map<String, String> gatewayHeaders, @Nullable Map<String, Object> headerParams) {
    HttpServletRequest request = ServletUtil.getRequest();
    HttpHeaders headers = new HttpHeaders();
    // 添加平台网关配置的请求头
    if (MapUtils.isNotEmpty(gatewayHeaders)) {
      for (Entry<String, String> entry : gatewayHeaders.entrySet()) {
        headers.set(entry.getKey(), resolveHeaderValue(request, entry.getValue()));
      }
    }
    // 添加 API 技能配置的请求头
    if (MapUtils.isNotEmpty(headerParams)) {
      for (Entry<String, Object> entry : headerParams.entrySet()) {
        headers.set(entry.getKey(), Objects.toString(entry.getValue(), ""));
      }
    }

    // 优先接收 JSON 格式
    if (headers.getAccept().isEmpty()) {
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.ALL));
    }
    return headers;
  }

  /**
   * 解析请求头的值，替换里面的变量
   */
  private String resolveHeaderValue(@Nullable HttpServletRequest request, String value) {
    String result;
    if ("${cookie}".equals(value)) {
      result = request != null ? request.getHeader(HttpHeaders.COOKIE) : null;
    }
    else if ("${sessionId}".equals(value)) {
      result = request != null ? authProvider.getSessionId(request) : null;
    }
    else {
      result = value;
    }
    // 避免返回 null, 否则发送请求时可能会变成字符串 "null"
    return result != null ? result : "";
  }

  /**
   * 模拟用例条件的参数解析器
   */
  private static final class MockConditionParamResolver implements Function<String, Object> {
    /** 接口参数 */
    private final ApiServiceParams params;

    private MockConditionParamResolver(ApiServiceParams params) {
      this.params = params;
    }

    @Override
    @Nullable
    public Object apply(String spec) {
      if (!Strings.CS.startsWith(spec, "$.")) {
        return spec;
      }
      Object result;
      if (spec.startsWith("$.path")) {
        result = ParamUtil.getNestedProperty(params.getPath(), StringUtils.substring(spec, "$.path".length() + 1));
      }
      else if (spec.startsWith("$.header")) {
        result = ParamUtil.getNestedProperty(params.getHeader(), StringUtils.substring(spec, "$.header".length() + 1));
      }
      else if (spec.startsWith("$.query")) {
        result = ParamUtil.getNestedProperty(params.getQuery(), StringUtils.substring(spec, "$.query".length() + 1));
      }
      else if (spec.startsWith("$.body")) {
        result = ParamUtil.getNestedProperty(params.getBody(), StringUtils.substring(spec, "$.body".length() + 1));
      }
      else {
        result = spec;
      }
      return result;
    }
  }
}
