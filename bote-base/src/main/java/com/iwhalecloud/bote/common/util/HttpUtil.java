package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.LogUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import javax.net.ssl.SSLContext;
import lombok.Getter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.HostnameVerificationPolicy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

/**
 * HTTP 请求工具类
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
@SuppressWarnings({"java:S2139", "java:S1192", "java:S5998", "PMD.GuardLogStatement"})
@SuppressFBWarnings("CRLF_INJECTION_LOGS")
public final class HttpUtil {
  private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

  @Getter
  private static final RestTemplate restTemplate = createRestTemplate();

  private HttpUtil() {
  }

  /**
   * 检查 URL 是否合法
   *
   * @param url URL
   * @return 是否合法
   */
  public static boolean isValid(String url) {
    if (StringUtils.isEmpty(url)) {
      return false;
    }
    //noinspection HttpUrlsUsage
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
      return false;
    }

    try {
      //noinspection ResultOfMethodCallIgnored
      new URI(url).toURL();
    }
    catch (URISyntaxException | MalformedURLException e) {
      return false;
    }
    return true;
  }

  /**
   * 提取 URL 中的 query 参数，自动解码参数值
   *
   * @param url URL
   * @return 查询参数，没有时返回空 map。参数值做了 URL 解码
   */
  public static MultiValueMap<String, String> extractQueryParams(String url) {
    MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUriString(url).build().getQueryParams();
    if (queryParams.isEmpty()) {
      return queryParams;
    }
    MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
    for (Entry<String, List<String>> entry : queryParams.entrySet()) {
      String name = entry.getKey();
      List<String> values = entry.getValue();
      if (values != null && !values.isEmpty()) {
        result.put(name, values.stream().map(v -> UriUtils.decode(v, StandardCharsets.UTF_8)).toList());
      }
      else {
        result.put(name, values);
      }
    }
    return result;
  }

  /**
   * 解码存量的 query 参数
   */
  public static void decodeQueryParams(String url, UriComponentsBuilder builder) {
    MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUriString(url).build().getQueryParams();
    if (queryParams.isEmpty()) {
      return;
    }
    // 先清空 query 参数
    builder.replaceQuery(null);
    for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
      List<String> decodedValues = entry.getValue().stream().map(p -> URLDecoder.decode(p, StandardCharsets.UTF_8)).toList();
      builder.queryParam(entry.getKey(), decodedValues);
    }
  }

  /**
   * 解码存量的 query 参数
   */
  private static MultiValueMap<String, String> decodeQueryParams(MultiValueMap<String, String> queryParams) {
    MultiValueMap<String, String> decodedParams = new LinkedMultiValueMap<>();
    for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
      List<String> decodedValues = entry.getValue().stream().map(p -> URLDecoder.decode(p, StandardCharsets.UTF_8)).toList();
      decodedParams.put(entry.getKey(), decodedValues);
    }
    return decodedParams;
  }

  /**
   * GET 请求
   * <p>响应体使用 JSON 格式</p>
   *
   * @param url 请求路径
   * @param typeReference 响应类型描述
   * @param <T> 响应对象类型
   * @return 响应对象。可能为 null
   */
  @Nullable
  public static <T> T get(String url, ParameterizedTypeReference<T> typeReference) {
    return get(url, (MultiValueMap<String, String>) null, typeReference);
  }

  /**
   * GET 请求
   * <p>响应体使用 JSON 格式</p>
   *
   * @param url 请求路径
   * @param params 请求参数，可选
   * @param typeReference 响应类型描述
   * @param <T> 响应对象类型
   * @return 响应对象。可能为 null
   */
  @Nullable
  public static <T> T get(String url, @Nullable Map<String, String> params, ParameterizedTypeReference<T> typeReference) {
    MultiValueMap<String, String> multiValueMap = null;
    if (MapUtils.isNotEmpty(params)) {
      multiValueMap = new LinkedMultiValueMap<>();
      multiValueMap.setAll(params);
    }
    return get(url, multiValueMap, typeReference);
  }

  /**
   * GET 请求
   * <p>响应体使用 JSON 格式</p>
   *
   * @param url 请求路径
   * @param params 请求参数，可选
   * @param typeReference 响应类型描述
   * @param <T> 响应对象类型
   * @return 响应对象。可能为 null
   */
  @Nullable
  public static <T> T get(String url, @Nullable MultiValueMap<String, String> params, ParameterizedTypeReference<T> typeReference) {
    return get(url, params, typeReference, null);
  }

  /**
   * GET 请求
   * <p>响应体使用 JSON 格式</p>
   *
   * @param url 请求路径
   * @param params 请求参数，可选
   * @param typeReference 响应类型描述
   * @param <T> 响应对象类型
   * @param headers 请求头信息
   * @return 响应对象。可能为 null
   */
  @Nullable
  public static <T> T get(String url, @Nullable MultiValueMap<String, String> params, ParameterizedTypeReference<T> typeReference,
                          @Nullable HttpHeaders headers) {
    Assert.hasText(url, "请求地址不能为空");

    // 构造链接，参数作为查询字符传递
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);
    // 解码存量的 query 参数，避免重复转义
    decodeQueryParams(url, uriComponentsBuilder);
    if (MapUtils.isNotEmpty(params)) {
      // 解码 query 参数，避免重复转义
      uriComponentsBuilder.queryParams(decodeQueryParams(params));
    }
    URI uri = uriComponentsBuilder.encode().build().toUri();

    // 请求头
    if (headers == null) {
      headers = new HttpHeaders();
      headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    // 请求体
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);

    // 执行请求
    logger.debug("Request start: GET url={}", uri);
    ResponseEntity<T> responseEntity;
    try {
      responseEntity = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, typeReference);
      logger.debug("Request end: GET url={}, status={}, response={}", uri, responseEntity.getStatusCode().value(),
        LogUtil.limitLength(responseEntity.getBody()));
    }
    catch (HttpStatusCodeException e) {
      logger.error("Request failed: GET url={}, status={}", uri, e.getStatusCode());
      throw BaseErrorConstant.REQUEST_FAIL.toException(e, "url=" + url + ", error=" + e.getStatusCode());
    }
    catch (Exception e) {
      logger.error("Request failed: GET url={}", uri, e);
      throw BaseErrorConstant.REQUEST_FAIL.toException(e, "url=" + url + ", error=" + ExpUtil.getMsg(e));
    }

    return responseEntity.getBody();
  }

  /**
   * GET 请求
   * <p>响应体是字符串</p>
   *
   * @param url 请求路径
   * @return 响应对象
   */
  @Nullable
  public static String get(String url, Map<String, Object> params) {
    return get(url, params, null);
  }

  /**
   * GET 请求
   * <p>响应体是字符串</p>
   *
   * @param url 请求路径
   * @param params 请求参数
   * @param headers 请求头
   * @return 响应对象
   */
  @Nullable
  public static String get(String url, Map<String, Object> params, @Nullable HttpHeaders headers) {
    Assert.hasText(url, "请求地址不能为空");
    if (headers == null) {
      headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
    }
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    logger.debug("Request start: GET url={}", url);
    ResponseEntity<String> responseEntity;
    try {
      responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class, params);
      logger.debug("Request end: GET url={}, status={}, response={}", url, responseEntity.getStatusCode().value(),
        LogUtil.limitLength(responseEntity.getBody()));
    }
    catch (HttpStatusCodeException e) {
      logger.error("Request failed: GET url={}, status={}", url, e.getStatusCode());
      throw BaseErrorConstant.REQUEST_FAIL.toException(e, "url=" + url + ", error=" + e.getStatusCode());
    }
    catch (Exception e) {
      logger.error("Request failed: GET url={}", url, e);
      throw BaseErrorConstant.REQUEST_FAIL.toException(e, "url=" + url + ", error=" + ExpUtil.getMsg(e));
    }
    return responseEntity.getBody();
  }

  /**
   * GET 请求，响应体为字节数组（适用于图片等二进制资源）
   *
   * @param url 请求路径
   * @param params 查询参数
   * @return 字节数组，可能为 null
   */
  @Nullable
  public static byte[] getForBytes(String url, Map<String, Object> params) {
    return getForBytes(url, params, null);
  }

  /**
   * GET 请求，响应体为字节数组（适用于图片等二进制资源）
   *
   * @param url 请求路径
   * @param params 查询参数
   * @param headers 请求头
   * @return 字节数组，可能为 null
   */
  @Nullable
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public static byte[] getForBytes(String url, Map<String, Object> params, @Nullable HttpHeaders headers) {
    Assert.hasText(url, "请求地址不能为空");
    if (headers == null) {
      headers = new HttpHeaders();
    }
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    logger.debug("Request start: GET url={} (bytes)", url);
    try {
      ResponseEntity<byte[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, byte[].class, params);
      logger.debug("Request end: GET url={}, status={}, length={}", url, responseEntity.getStatusCode().value(),
        responseEntity.getBody() != null ? responseEntity.getBody().length : 0);
      return responseEntity.getBody();
    }
    catch (HttpStatusCodeException e) {
      logger.error("Request failed: GET url={}, status={}", url, e.getStatusCode());
      throw BaseErrorConstant.REQUEST_FAIL.toException(e, "url=" + url + ", error=" + e.getStatusCode());
    }
    catch (Exception e) {
      logger.error("Request failed: GET url={}", url, e);
      throw BaseErrorConstant.REQUEST_FAIL.toException(e, "url=" + url + ", error=" + ExpUtil.getMsg(e));
    }
  }

  /**
   * POST 请求
   *
   * @param url 请求路径
   * @param params 请求参数，可选
   * @param responseType 响应类型
   * @param <T> 响应对象类型
   * @return 响应对象。可能为 null
   */
  @Nullable
  public static <T> T post(String url, @Nullable Object params, Class<T> responseType) {
    return post(url, params, ParameterizedTypeReference.forType(responseType));
  }

  /**
   * POST 请求
   * <p>请求参数为 MultiValueMap 类型时使用表单形式，其它情况使用 JSON 形式</p>
   * <p>响应体使用 JSON 格式</p>
   *
   * @param url 请求路径
   * @param params 请求参数，可选
   * @param typeReference 响应类型描述
   * @param <T> 响应对象类型
   * @return 响应对象。可能为 null
   */
  @Nullable
  public static <T> T post(String url, @Nullable Object params, ParameterizedTypeReference<T> typeReference) {
    Assert.hasText(url, "请求地址不能为空");
    // 请求头
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    return post(url, params, typeReference, headers);
  }

  /**
   * POST 请求
   * <p>请求参数为 MultiValueMap 类型时使用表单形式，其它情况使用 JSON 形式</p>
   * <p>响应体使用 JSON 格式</p>
   *
   * @param url 请求路径
   * @param params 请求参数，可选
   * @param typeReference 响应类型描述
   * @param <T> 响应对象类型
   * @param headers 提取出请求头，方便其他接口携带cookie
   * @return 响应对象。可能为 null
   */
  @Nullable
  public static <T> T post(String url, @Nullable Object params, ParameterizedTypeReference<T> typeReference, HttpHeaders headers) {
    // 请求体
    HttpEntity<?> requestEntity = getHttpEntity(params, headers);

    // 执行请求
    logger.debug("Request start: POST url={}, data={}", url, LogUtil.limitLength(params));
    ResponseEntity<T> responseEntity;
    try {
      responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, typeReference);
      logger.debug("Request end: POST url={}, status={}, response={}", url, responseEntity.getStatusCode().value(),
        LogUtil.limitLength(responseEntity.getBody()));
    }
    catch (HttpStatusCodeException e) {
      logger.error("Request failed: POST url={}, status={}", url, e.getStatusCode());
      throw BaseErrorConstant.REQUEST_FAIL.toException(e, "url=" + url + ", error=" + e.getStatusCode());
    }
    catch (Exception e) {
      logger.error("Request failed: POST url={}", url, e);
      throw BaseErrorConstant.REQUEST_FAIL.toException(e, "url=" + url + ", error=" + ExpUtil.getMsg(e));
    }

    return responseEntity.getBody();
  }

  @Nullable
  public static <T> T put(String url, @Nullable Object params, ParameterizedTypeReference<T> typeReference, HttpHeaders headers) {
    // 请求体
    HttpEntity<?> requestEntity = getHttpEntity(params, headers);

    // 执行请求
    logger.debug("Request start: PUT url={}", url);
    ResponseEntity<T> responseEntity;
    try {
      responseEntity = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, typeReference);
      logger.debug("Request end: PUT url={}, status={}, response={}", url, responseEntity.getStatusCode().value(),
        LogUtil.limitLength(responseEntity.getBody()));
    }
    catch (HttpStatusCodeException e) {
      logger.error("Request failed: PUT url={}, status={}", url, e.getStatusCode());
      throw BaseErrorConstant.REQUEST_FAIL.toException(e, "url=" + url + ", error=" + e.getStatusCode());
    }
    catch (Exception e) {
      logger.error("Request failed: PUT url={}", url, e);
      throw BaseErrorConstant.REQUEST_FAIL.toException(e, "url=" + url + ", error=" + ExpUtil.getMsg(e));
    }
    return responseEntity.getBody();
  }

  /**
   * POST 请求（支持查询参数和请求体）
   * <p>查询参数会附加到URL中，请求体使用JSON格式</p>
   * <p>响应体使用 JSON 格式</p>
   *
   * @param url 请求路径
   * @param queryParams 查询参数，可选
   * @param body 请求体，可选
   * @param typeReference 响应类型描述
   * @param <T> 响应对象类型
   * @param headers 请求头信息
   * @return 响应对象。可能为 null
   */
  @Nullable
  public static <T> T post(String url, @Nullable Map<String, String> queryParams, @Nullable Object body, ParameterizedTypeReference<T> typeReference, HttpHeaders headers) {
    // 构造链接，查询参数作为查询字符串传递
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);
    if (MapUtils.isNotEmpty(queryParams)) {
      uriComponentsBuilder.queryParams(convertToMultiValueMap(queryParams));
    }
    URI uri = uriComponentsBuilder.build().toUri();

    // 请求体
    HttpEntity<?> requestEntity = getHttpEntity(body, headers);

    // 执行请求
    logger.debug("Request start: POST url={}, queryParams={}, body={}", uri, queryParams, LogUtil.limitLength(body));
    ResponseEntity<T> responseEntity;
    try {
      responseEntity = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, typeReference);
      logger.debug("Request end: POST url={}, status={}, response={}", uri, responseEntity.getStatusCode().value(),
        LogUtil.limitLength(responseEntity.getBody()));
    }
    catch (HttpStatusCodeException e) {
      logger.error("Request failed: POST url={}, status={}", uri, e.getStatusCode());
      throw BaseErrorConstant.REQUEST_FAIL.toException(e, "url=" + url + ", error=" + e.getStatusCode());
    }
    catch (Exception e) {
      logger.error("Request failed: POST url={}", uri, e);
      throw BaseErrorConstant.REQUEST_FAIL.toException(e, "url=" + url + ", error=" + ExpUtil.getMsg(e));
    }

    return responseEntity.getBody();
  }

  private static HttpEntity<?> getHttpEntity(@Nullable Object params, HttpHeaders headers) {
    HttpEntity<?> requestEntity;
    if (params == null) {
      requestEntity = new HttpEntity<>(headers);
    }
    else if (params instanceof MultiValueMap) {
      if (headers.getContentType() == null) {
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
      }
      requestEntity = new HttpEntity<>(params, headers);
    }
    else if (params instanceof String) {
      requestEntity = new HttpEntity<>(params, headers);
    }
    else {
      headers.setContentType(MediaType.APPLICATION_JSON);
      requestEntity = new HttpEntity<>(JsonUtil.toJsonString(params), headers);
    }
    return requestEntity;
  }

  /**
   * 将Map<String, String>转换为MultiValueMap<String, String>
   *
   * @param params 参数Map
   * @return MultiValueMap
   */
  private static MultiValueMap<String, String> convertToMultiValueMap(@Nullable Map<String, String> params) {
    if (MapUtils.isEmpty(params)) {
      return new LinkedMultiValueMap<>();
    }
    MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
    params.forEach(multiValueMap::add);
    return multiValueMap;
  }

  /**
   * 构造 RestTemplate 实例
   */
  private static RestTemplate createRestTemplate() {
    RestTemplate restTemplate = new CustomRestTemplate(HttpUtil::getClientHttpRequestFactory);
    restTemplate.setMessageConverters(getHttpMessageConverters());
    return restTemplate;
  }

  /**
   * 创建带自定义超时时间的 RestTemplate 实例
   *
   * @param connectTimeout 请求超时时间（毫秒），null 表示使用默认值 10000ms
   * @param readTimeout 读取超时时间（毫秒），null 表示使用默认值 300000ms
   * @return RestTemplate 实例
   */
  public static RestTemplate createRestTemplateWithTimeout(@Nullable Integer connectTimeout, @Nullable Integer readTimeout) {
    ClientHttpRequestFactory factory = getClientHttpRequestFactory(connectTimeout, readTimeout);
    RestTemplate restTemplate = new CustomRestTemplate(() -> factory);
    restTemplate.setMessageConverters(getHttpMessageConverters());
    return restTemplate;
  }

  /**
   * 自定义 HTTP 请求工厂实例
   */
  private static ClientHttpRequestFactory getClientHttpRequestFactory() {
    return getClientHttpRequestFactory(null, null);
  }

  /**
   * 自定义 HTTP 请求工厂实例（支持自定义超时时间）
   *
   * @param connectTimeout 请求超时时间（毫秒），null 表示使用默认值 10000ms
   * @param readTimeout 读取超时时间（毫秒），null 表示使用默认值 300000ms
   * @return ClientHttpRequestFactory 实例
   */
  @SuppressWarnings("PMD.CloseResource")
  private static ClientHttpRequestFactory getClientHttpRequestFactory(@Nullable Integer connectTimeout, @Nullable Integer readTimeout) {
    // 忽略SSL证书验证
    SSLContext sslContext;
    try {
      sslContext = SSLContexts.custom().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build();
    }
    catch (GeneralSecurityException e) {
      throw BaseErrorConstant.SSL_CONTEXT_BUILD_ERROR.toException(e);
    }
    TlsSocketStrategy tlsSocketStrategy = ClientTlsStrategyBuilder.create()
      .setHostVerificationPolicy(HostnameVerificationPolicy.CLIENT)
      .setSslContext(sslContext)
      .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
      .buildClassic();

    // connectionManager 的生命周期由 HttpClient 管理，HttpClient 关闭时会自动关闭 connectionManager
    PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
      .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.LAX)
      .setConnPoolPolicy(PoolReusePolicy.FIFO)
      .setTlsSocketStrategy(tlsSocketStrategy)
      .setDefaultConnectionConfig(ConnectionConfig.custom()
        // 连接超时时间，默认 180s
        .setConnectTimeout(Timeout.ofSeconds(60))
        // 连接最大存活时间，无论是否被复用。过长的时间会导致客户端无法及时感知到 DNS、负载均衡变化
        .setTimeToLive(TimeValue.ofMinutes(3))
        .build()
      )
      // 最大连接数
      .setMaxConnTotal(200)
      // 每个路由的最大连接数
      .setMaxConnPerRoute(200)
      .build();

    HttpClient httpClient = HttpClients.custom()
      .setConnectionManager(connectionManager)
      // 重试次数，默认重试 1 次，间隔 1s
      .setRetryStrategy(new DefaultHttpRequestRetryStrategy(3, TimeValue.ofSeconds(1)))
      // 避免管理 cookie
      .disableCookieManagement()
      // 禁用重定向
      .disableRedirectHandling()
      .setDefaultRequestConfig(RequestConfig.custom()
        // 从连接池获取连接的超时时间，默认 10 秒，如果指定了 connectTimeout 则使用指定值
        .setConnectionRequestTimeout(connectTimeout != null ? Timeout.ofMilliseconds(connectTimeout) : Timeout.ofSeconds(10))
        // 连接读取超时时间，默认 300 秒，如果指定了 readTimeout 则使用指定值
        .setResponseTimeout(readTimeout != null ? Timeout.ofMilliseconds(readTimeout) : Timeout.ofSeconds(300))
        // 连接的默认最大空闲时间（超出时间未被复用则关闭），仅在服务器未指定 Keep-Alive 超时时间时使用
        .setConnectionKeepAlive(Timeout.ofSeconds(60))
        .build())
      .build();

    // https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-6.1-Release-Notes#web-applications
    // RestTemplate 默认不再 buffer 请求体，发送 json 请求体时会使用 Transfer-Encoding: chunked, 然而某些业务系统不支持
    return new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
  }

  /**
   * 构造 http 转换器列表
   * <p>参考 {@link org.springframework.web.client.RestTemplate#RestTemplate()}, 去掉一些没用的转换器</p>
   */
  private static List<HttpMessageConverter<?>> getHttpMessageConverters() {
    List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
    messageConverters.add(new ByteArrayHttpMessageConverter());
    // 默认编码是 ISO-8859-1, 会导致乱码
    messageConverters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
    messageConverters.add(new FormHttpMessageConverter());
    // 复用系统默认的 ObjectMapper 以便将日期序列化为字符串而非时间戳
    MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter(JsonUtil.getObjectMapper());
    // 兼容不标准的 text/json 响应类型，部分现场环境调用 API 技能需要
    List<MediaType> mediaTypes = new ArrayList<>(jackson2HttpMessageConverter.getSupportedMediaTypes());
    mediaTypes.add(MediaType.parseMediaType("text/json"));
    jackson2HttpMessageConverter.setSupportedMediaTypes(mediaTypes);
    messageConverters.add(jackson2HttpMessageConverter);
    return messageConverters;
  }

  /**
   * 自定义 RestTemplate, 解决 HttpClient 在遇到 Error 会关闭连接池导致 RestTemplate 不可用的问题
   *
   * <p>Apache HttpClient 在捕获到 Error 时会关闭连接池: {@link org.apache.http.impl.execchain.MainClientExec#execute},
   * 导致后续所有请求都会报错: {@link org.apache.http.impl.conn.PoolingHttpClientConnectionManager#requestConnection},
   * 虽然 HttpClient 的行为有一定的合理性（遇到 Error 无法确保正确释放连接），但实际上有些 Error 是可恢复的，比如 OutOfMemoryError, StackOverflowError,
   * 不能出现一次 Error 就导致 RestTemplate 无法使用。</p>
   *
   * @see <a href="https://issues.apache.org/jira/browse/HTTPCLIENT-2133">"Connection pool shut down" Exceptions occur after Error such as OOM is thrown while making an HTTP request</a>
   */
  private static final class CustomRestTemplate extends RestTemplate {
    /** 请求工厂生成器 */
    private final Supplier<ClientHttpRequestFactory> requestFactoryGenerator;

    private CustomRestTemplate(Supplier<ClientHttpRequestFactory> requestFactoryGenerator) {
      this.requestFactoryGenerator = requestFactoryGenerator;
      setRequestFactory(requestFactoryGenerator.get());
    }

    @Override
    @Nullable
    protected <T> T doExecute(URI url, @Nullable String uriTemplate, @Nullable HttpMethod method, @Nullable RequestCallback requestCallback,
                              @Nullable ResponseExtractor<T> responseExtractor) throws RestClientException {
      try {
        return super.doExecute(url, uriTemplate, method, requestCallback, responseExtractor);
      }
      catch (IllegalStateException e) {
        // 没有办法在请求开始前检查连接池状态，只能捕获异常
        if ("Connection pool shut down".equals(e.getMessage())) {
          // 使用新的请求工程实例，以替换连接池
          setRequestFactory(requestFactoryGenerator.get());
          // 重试请求
          return super.doExecute(url, uriTemplate, method, requestCallback, responseExtractor);
        }
        throw e;
      }
    }
  }
}
