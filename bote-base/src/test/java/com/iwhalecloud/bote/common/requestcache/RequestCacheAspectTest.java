package com.iwhalecloud.bote.common.requestcache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.common.annotation.RequestCacheable;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * {@link RequestCacheAspect} 单元测试。
 *
 * <p>切面依赖 RequestContextHolder/LocaleContextHolder 静态方法与 Servlet 请求/响应对象，故以
 * mockStatic 注入 ServletRequestAttributes 与 Locale，并以 mock ProceedingJoinPoint、JdbcTemplate
 * 驱动 @RequestCacheable 的 sql/method 两条 ETag 计算路径及缓存命中（304）分支。</p>
 */
@MockitoSettings(strictness = Strictness.LENIENT)
class RequestCacheAspectTest {

  private MockedStatic<RequestContextHolder> requestContextHolder;
  private MockedStatic<LocaleContextHolder> localeContextHolder;

  private HttpServletRequest request;
  private HttpServletResponse response;
  private ProceedingJoinPoint joinPoint;
  private RequestCacheable requestCacheable;
  private JdbcTemplate jdbcTemplate;
  private Signature signature;
  private RequestCacheAspect aspect;

  @BeforeEach
  void setUp() throws Throwable {
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    joinPoint = mock(ProceedingJoinPoint.class);
    requestCacheable = mock(RequestCacheable.class);
    jdbcTemplate = mock(JdbcTemplate.class);
    signature = mock(Signature.class);

    when(signature.getDeclaringType()).thenReturn(TestController.class);
    when(signature.getName()).thenReturn("list");
    when(joinPoint.getSignature()).thenReturn(signature);
    when(joinPoint.getArgs()).thenReturn(new Object[]{});
    when(joinPoint.getTarget()).thenReturn(new TestController());
    when(joinPoint.proceed()).thenReturn("ok");

    requestContextHolder = mockStatic(RequestContextHolder.class);
    localeContextHolder = mockStatic(LocaleContextHolder.class);
    localeContextHolder.when(LocaleContextHolder::getLocale).thenReturn(Locale.ENGLISH);

    @SuppressWarnings("unchecked")
    ObjectProvider<List<RequestCacheableParamResolver>> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(null);
    aspect = new RequestCacheAspect(jdbcTemplate, provider);
  }

  @AfterEach
  void tearDown() {
    requestContextHolder.close();
    localeContextHolder.close();
  }

  private void setupServletAttributes() {
    requestContextHolder.when(RequestContextHolder::getRequestAttributes)
      .thenReturn(new ServletRequestAttributes(request, response));
  }

  private List<Map<String, Object>> singleRow() {
    return List.of(Map.of("v", 1));
  }

  // ==================== around: 前置分支 ====================

  @Test
  void around_noRequestAttributes_proceeds() throws Throwable {
    requestContextHolder.when(RequestContextHolder::getRequestAttributes).thenReturn(null);
    assertThat(aspect.aroundRequestCacheableMethod(joinPoint, requestCacheable)).isEqualTo("ok");
    verify(response, never()).setHeader(any(), any());
  }

  @Test
  void around_nonServletAttributes_proceeds() throws Throwable {
    requestContextHolder.when(RequestContextHolder::getRequestAttributes)
      .thenReturn(mock(RequestAttributes.class));
    assertThat(aspect.aroundRequestCacheableMethod(joinPoint, requestCacheable)).isEqualTo("ok");
    verify(response, never()).setHeader(any(), any());
  }

  @Test
  void around_responseNull_proceeds() throws Throwable {
    // 单参构造的 ServletRequestAttributes#getResponse 返回 null
    requestContextHolder.when(RequestContextHolder::getRequestAttributes)
      .thenReturn(new ServletRequestAttributes(request));
    assertThat(aspect.aroundRequestCacheableMethod(joinPoint, requestCacheable)).isEqualTo("ok");
    verify(response, never()).setHeader(any(), any());
  }

  // ==================== around: sql 路径 ====================

  @Test
  void around_sqlPath_noMatch_proceedsAndSetsHeaders() throws Throwable {
    setupServletAttributes();
    when(requestCacheable.sql()).thenReturn("SELECT 1");
    when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(singleRow());

    Object result = aspect.aroundRequestCacheableMethod(joinPoint, requestCacheable);

    assertThat(result).isEqualTo("ok");
    verify(response).setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    verify(response).setHeader(eq(HttpHeaders.ETAG), anyString());
  }

  @Test
  void around_sqlPath_etagMatch_returns304() throws Throwable {
    setupServletAttributes();
    when(requestCacheable.sql()).thenReturn("SELECT 1");
    List<Map<String, Object>> rows = singleRow();
    when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(rows);
    // 复刻切面的 etag 计算：etagKeys = Arrays.asList(sqlArgs, list)，locale="en"
    Object etagKeys = Arrays.asList(new ArrayList<>(), rows);
    String expectedEtag = "W/\"" + DigestUtils.md5DigestAsHex(
      (etagKeys.toString() + "en").getBytes(StandardCharsets.UTF_8)) + "\"";
    when(request.getHeader(HttpHeaders.IF_NONE_MATCH)).thenReturn(expectedEtag);

    Object result = aspect.aroundRequestCacheableMethod(joinPoint, requestCacheable);

    assertThat(result).isNull();
    verify(response).setStatus(HttpStatus.NOT_MODIFIED.value());
    verify(joinPoint, never()).proceed();
  }

  @Test
  void around_sqlPath_emptyResult_noCacheOnNotFound_proceedsNoHeaders() throws Throwable {
    setupServletAttributes();
    when(requestCacheable.sql()).thenReturn("SELECT 1");
    when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(Collections.emptyList());

    Object result = aspect.aroundRequestCacheableMethod(joinPoint, requestCacheable);

    assertThat(result).isEqualTo("ok");
    verify(response, never()).setHeader(eq(HttpHeaders.ETAG), any());
    verify(response, never()).setHeader(eq(HttpHeaders.CACHE_CONTROL), any());
  }

  @Test
  void around_sqlPath_emptyResult_cacheOnNotFound_usesRequestUrl() throws Throwable {
    setupServletAttributes();
    when(requestCacheable.sql()).thenReturn("SELECT 1");
    when(requestCacheable.cacheOnNotFound()).thenReturn(true);
    when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(Collections.emptyList());
    when(request.getRequestURL()).thenReturn(new StringBuffer("http://host/api/x"));

    Object result = aspect.aroundRequestCacheableMethod(joinPoint, requestCacheable);

    assertThat(result).isEqualTo("ok");
    verify(response).setHeader(eq(HttpHeaders.ETAG), anyString());
    verify(response).setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
  }

  @Test
  void around_sqlPath_singleRowAllNull_treatedAsNotFound() throws Throwable {
    setupServletAttributes();
    when(requestCacheable.sql()).thenReturn("SELECT 1");
    // 单行且所有值为 null，视为查不到数据（HashMap 允许 null 值）
    java.util.HashMap<String, Object> nullRow = new java.util.HashMap<>();
    nullRow.put("v", null);
    when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of(nullRow));

    Object result = aspect.aroundRequestCacheableMethod(joinPoint, requestCacheable);

    assertThat(result).isEqualTo("ok");
    verify(response, never()).setHeader(eq(HttpHeaders.ETAG), any());
  }

  // ==================== buildEtagKeys: 参数校验 ====================

  @Test
  void around_neitherSqlNorMethod_throws() {
    setupServletAttributes();
    when(requestCacheable.sql()).thenReturn("");
    when(requestCacheable.method()).thenReturn("");
    assertThatThrownBy(() -> aspect.aroundRequestCacheableMethod(joinPoint, requestCacheable))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("必须指定");
  }

  @Test
  void around_bothSqlAndMethod_throws() {
    setupServletAttributes();
    when(requestCacheable.sql()).thenReturn("SELECT 1");
    when(requestCacheable.method()).thenReturn("buildEtag");
    assertThatThrownBy(() -> aspect.aroundRequestCacheableMethod(joinPoint, requestCacheable))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("不能同时");
  }

  // ==================== buildEtagKeysBySql: 参数解析 ====================

  @Test
  void sql_param1_resolvesFirstArg() throws Throwable {
    setupServletAttributes();
    when(requestCacheable.sql()).thenReturn("SELECT #{param1}");
    when(joinPoint.getArgs()).thenReturn(new Object[]{"v1"});
    when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(singleRow());

    aspect.aroundRequestCacheableMethod(joinPoint, requestCacheable);

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(jdbcTemplate).queryForList(sqlCaptor.capture(), any(Object[].class));
    assertThat(sqlCaptor.getValue()).isEqualTo("SELECT ?");
  }

  @Test
  void sql_param1NestedProperty_resolvesNested() throws Throwable {
    setupServletAttributes();
    when(requestCacheable.sql()).thenReturn("SELECT #{param1.id}");
    when(joinPoint.getArgs()).thenReturn(new Object[]{new Bean(5L)});
    when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(singleRow());

    aspect.aroundRequestCacheableMethod(joinPoint, requestCacheable);

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(jdbcTemplate).queryForList(sqlCaptor.capture(), any(Object[].class));
    assertThat(sqlCaptor.getValue()).isEqualTo("SELECT ?");
  }

  @Test
  void sql_collectionParam_expandsToMultiplePlaceholders() throws Throwable {
    setupServletAttributes();
    when(requestCacheable.sql()).thenReturn("SELECT #{param1}");
    when(joinPoint.getArgs()).thenReturn(new Object[]{List.of(1, 2, 3)});
    when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(singleRow());

    aspect.aroundRequestCacheableMethod(joinPoint, requestCacheable);

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(jdbcTemplate).queryForList(sqlCaptor.capture(), any(Object[].class));
    assertThat(sqlCaptor.getValue()).isEqualTo("SELECT ?,?,?");
  }

  @Test
  void sql_resolverParam_usesResolver() throws Throwable {
    setupServletAttributes();
    when(requestCacheable.sql()).thenReturn("SELECT #{userId}");
    RequestCacheableParamResolver resolver = mock(RequestCacheableParamResolver.class);
    when(resolver.supports("userId")).thenReturn(true);
    when(resolver.resolve("userId")).thenReturn(42);
    rebuildAspectWith(resolver);
    when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(singleRow());

    aspect.aroundRequestCacheableMethod(joinPoint, requestCacheable);

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(jdbcTemplate).queryForList(sqlCaptor.capture(), any(Object[].class));
    assertThat(sqlCaptor.getValue()).isEqualTo("SELECT ?");
    verify(resolver).resolve("userId");
  }

  @Test
  void sql_unresolvableParam_throws() {
    setupServletAttributes();
    when(requestCacheable.sql()).thenReturn("SELECT #{unknown}");
    assertThatThrownBy(() -> aspect.aroundRequestCacheableMethod(joinPoint, requestCacheable))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("参数不存在");
  }

  @Test
  void sql_paramNumOutOfRange_throws() {
    setupServletAttributes();
    when(requestCacheable.sql()).thenReturn("SELECT #{param9}");
    when(joinPoint.getArgs()).thenReturn(new Object[]{"only-one"});
    assertThatThrownBy(() -> aspect.aroundRequestCacheableMethod(joinPoint, requestCacheable))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("参数不存在");
  }

  // ==================== buildEtagKeysByMethod ====================

  @Test
  void around_methodPath_invokesControllerMethod() throws Throwable {
    setupServletAttributes();
    when(requestCacheable.method()).thenReturn("buildEtag");

    Object result = aspect.aroundRequestCacheableMethod(joinPoint, requestCacheable);

    assertThat(result).isEqualTo("ok");
    verify(response).setHeader(eq(HttpHeaders.ETAG), anyString());
  }

  @Test
  void methodPath_notFound_throwsBssException() {
    setupServletAttributes();
    when(requestCacheable.method()).thenReturn("noSuchMethod");
    assertThatThrownBy(() -> aspect.aroundRequestCacheableMethod(joinPoint, requestCacheable))
      .isInstanceOf(BssException.class);
  }

  @Test
  void methodPath_invocationTargetBssException_rethrows() {
    setupServletAttributes();
    when(requestCacheable.method()).thenReturn("throwBss");
    assertThatThrownBy(() -> aspect.aroundRequestCacheableMethod(joinPoint, requestCacheable))
      .isInstanceOf(BssException.class)
      .hasMessageContaining("bss-fail");
  }

  @Test
  void methodPath_invocationTargetOther_throwsBssException() {
    setupServletAttributes();
    when(requestCacheable.method()).thenReturn("throwOther");
    assertThatThrownBy(() -> aspect.aroundRequestCacheableMethod(joinPoint, requestCacheable))
      .isInstanceOf(BssException.class);
  }

  // ==================== helpers ====================

  @SuppressWarnings("unchecked")
  private void rebuildAspectWith(RequestCacheableParamResolver resolver) {
    ObjectProvider<List<RequestCacheableParamResolver>> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(List.of(resolver));
    aspect = new RequestCacheAspect(jdbcTemplate, provider);
  }

  /** 带可嵌套属性的测试 Bean。 */
  public static class Bean {
    private final Long id;
    Bean(Long id) { this.id = id; }
    public Long getId() { return id; }
  }

  /** 提供 method 路径所需的 buildEtag / 抛异常方法。 */
  public static class TestController {
    public String list() { return "ok"; }
    public String buildEtag() { return "etag-keys"; }
    public void throwBss() { throw new BssException("bss-fail"); }
    public void throwOther() { throw new RuntimeException("boom"); }
  }
}
