package com.iwhalecloud.bote.common.requestcache;

import com.iwhalecloud.bote.common.annotation.RequestCacheable;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 请求缓存切面
 *
 * <p>使用切面机制拦截 Controller 方法的执行，设置 Cache-Control + ETag 响应头指示客户端可以缓存响应但需要校验缓存是否有效，缓存有效时返回 304 响应，以减少处理请求、传输数据的开销。</p>
 *
 * <p>通过定制 {@link org.springframework.web.method.support.InvocableHandlerMethod#invokeForRequest} 也能实现需求，但需要定制 {@link org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter},
 * 对应用的配置侵入性较大，而且会对每个请求增加一次检查是否存在注解的开销，因此采用切面方式，虽然存在扫描注解、执行切面的开销，但只会在应用启动时扫描一次注解，
 * 也只对启用了缓存功能的 Controller 存在一点运行时开销（启用缓存功能的是极少数接口）。</p>
 *
 * @author bianjp
 * @since 2023-06-25
 */
@Aspect
@SuppressWarnings("PMD.GuardLogStatement")
public final class RequestCacheAspect {
  private static final Logger logger = LoggerFactory.getLogger(RequestCacheAspect.class);
  /** SQL 参数匹配模式(#{paramName}) */
  private static final Pattern sqlParamPattern = Pattern.compile("#\\{([\\w.]+)}");

  /** JDBC 模板，执行查询 SQL 使用 */
  private final JdbcTemplate jdbcTemplate;
  /** 请求缓存参数解析器列表 */
  private final List<RequestCacheableParamResolver> paramResolvers;

  public RequestCacheAspect(JdbcTemplate jdbcTemplate, ObjectProvider<List<RequestCacheableParamResolver>> paramResolvers) {
    this.jdbcTemplate = jdbcTemplate;
    this.paramResolvers = ListUtils.emptyIfNull(paramResolvers.getIfAvailable());
  }

  /**
   * 拦截添加了 {@link RequestCacheable} 注解的 Controller 方法
   *
   * @param joinPoint 连接点实例
   * @param requestCacheable 注解实例
   * @return Controller 方法返回值。客户端缓存有效时不调用 Controller 方法，直接返回 null
   */
  @Around("bean(*Controller) && @annotation(requestCacheable)")
  @Nullable
  // -@cs[IllegalThrowsCheck] joinPoint.proceed may throw anything
  public Object aroundRequestCacheableMethod(ProceedingJoinPoint joinPoint, RequestCacheable requestCacheable) throws Throwable {
    // 获取 request, response 对象，获取不到时不处理
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    if (!(requestAttributes instanceof ServletRequestAttributes)) {
      return joinPoint.proceed();
    }
    HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
    HttpServletResponse response = ((ServletRequestAttributes) requestAttributes).getResponse();
    if (response == null) {
      return joinPoint.proceed();
    }

    // 构造计算 etag 的键
    Object etagKeys = buildEtagKeys(joinPoint, requestCacheable);
    if (etagKeys == null) {
      // 无法计算出 etag 时不处理（可能请求的数据不存在，业务逻辑一般会主动报错）
      if (!requestCacheable.cacheOnNotFound()) {
        return joinPoint.proceed();
      }
      // 查不到数据时使用请求地址计算 ETag
      etagKeys = request.getRequestURL().toString();
    }

    // 计算 etag, 兼容国际化语言切换功能，追加语言变量，注意 etag 的值必须用双引号包裹
    // 使用 weak etag 以避免 byte range requests 被缓存，同时避免 Nginx 启用 gzip 时 etag 丢失或被篡改
    // Nginx 修改响应体时会删除不合法的 etag (不以 W/ 或双引号开头), strong etag 会被转为 weak etag, weak etag 会原样保留
    // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/ETag#directives
    // https://mailman.nginx.org/pipermail/nginx/2020-January/058856.html
    String locale = LocaleContextHolder.getLocale().toLanguageTag();
    String etag = "W/\"" + DigestUtils.md5DigestAsHex((etagKeys + locale).getBytes(StandardCharsets.UTF_8)) + "\"";

    // 无论缓存是否有效都需要返回缓存响应头
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.setHeader(HttpHeaders.ETAG, etag);

    // 检查请求头中的 etag 与最新 etag 是否相同，如果相同则表示客户端缓存有效，直接返回 304 状态码，不再调用 Controller 方法
    String oldEtag = request.getHeader(HttpHeaders.IF_NONE_MATCH);
    if (etag.equals(oldEtag)) {
      response.setStatus(HttpStatus.NOT_MODIFIED.value());
      return null;
    }

    // 调用 Controller 方法生成响应内容
    return joinPoint.proceed();
  }

  /**
   * 构造用于计算 ETag 的键
   *
   * <p>可以是任意数据类型，但需要有合理的 toString 实现，计算前会调用 Object#toString 转为字符串。</p>
   */
  @Nullable
  private Object buildEtagKeys(ProceedingJoinPoint joinPoint, RequestCacheable requestCacheable) {
    String sql = requestCacheable.sql();
    String method = requestCacheable.method();
    if (StringUtils.isEmpty(sql) && StringUtils.isEmpty(method)) {
      throw new IllegalArgumentException(getControllerMethod(joinPoint) + " 使用 @RequestCacheable 注解时必须指定 sql 或 method 参数");
    }
    else if (StringUtils.isNotEmpty(sql) && StringUtils.isNotEmpty(method)) {
      throw new IllegalArgumentException(getControllerMethod(joinPoint) + " 使用 @RequestCacheable 注解时不能同时指定 sql 和 method 参数");
    }
    if (StringUtils.isNotEmpty(sql)) {
      return buildEtagKeysBySql(joinPoint, sql);
    }
    return buildEtagKeysByMethod(joinPoint, method);
  }

  /**
   * 根据 SQL 构造计算 ETag 的键
   *
   * <p>执行 SQL, 根据 SQL 参数和查询结果计算 ETag。</p>
   *
   * <p>之所以将 SQL 参数用于计算 ETag, 是为了支持 SQL 参数中使用了 session 中的 userId 等信息的情况，此时参数不在 URL 中，如果 SQL 查询结果相同但实际上却不应该对不同用户共用缓存，</p>
   */
  @Nullable
  @SuppressFBWarnings("SQL_INJECTION_SPRING_JDBC")
  private Object buildEtagKeysBySql(ProceedingJoinPoint joinPoint, String sql) {
    StringBuilder sb = new StringBuilder(sql.length());
    Matcher matcher = sqlParamPattern.matcher(sql);
    // 缓存 SQL 参数解析结果，SQL 中重复使用同一参数时只解析一次
    Map<String, Object> resolvedParams = new HashMap<>();
    Object[] methodArgs = joinPoint.getArgs();
    List<Object> sqlArgs = new ArrayList<>();
    Object value;

    // 解析 SQL 参数
    while (matcher.find()) {
      value = resolvedParams.computeIfAbsent(matcher.group(1), (k) -> resolveSqlParam(k, joinPoint, methodArgs));
      // 如果参数值是集合（用于 IN 条件），替换为多个参数
      if (value instanceof Collection) {
        matcher.appendReplacement(sb, StringUtils.repeat("?", ",", ((Collection<?>) value).size()));
        sqlArgs.addAll((Collection<?>) value);
      }
      else {
        matcher.appendReplacement(sb, "?");
        sqlArgs.add(value);
      }
    }
    matcher.appendTail(sb);
    String processedSql = sb.toString();

    // 执行 SQL
    List<Map<String, Object>> list = jdbcTemplate.queryForList(processedSql, sqlArgs.toArray(new Object[0]));
    // 查不到数据时不缓存（通常 Controller 方法在查不到数据时会报错；即使不报错，查不到数据也难以确定合适的 ETag）
    // 使用 SQL 聚合函数时（如 MAX），查询结果固定会返回一条记录，但没有数据时值为 null, 要兼容这种情况
    if (list.isEmpty() || (list.size() == 1 && list.get(0).values().stream().allMatch(Objects::isNull))) {
      return null;
    }
    // SQL 参数 + 返回结果共同用于计算 ETag
    return Arrays.asList(sqlArgs, list);
  }

  /**
   * 解析 SQL 参数
   *
   * <p>支持嵌套属性，使用 {@link PropertyUtils#getNestedProperty(Object, String)} 解析。</p>
   */
  @Nullable
  private Object resolveSqlParam(String paramName, ProceedingJoinPoint joinPoint, Object[] methodArgs) {
    // 检查属性名称是否是 paramN (N 为数字，从 1 开始），如果是则表示引用的是 Controller 方法的参数
    if (paramName.startsWith("param")) {
      int dotPos = paramName.indexOf('.');
      // 找出第一层属性名称
      String paramNumStr = paramName.substring("param".length(), dotPos == -1 ? paramName.length() : dotPos);
      if (StringUtils.isNumeric(paramNumStr)) {
        String rootPropName = dotPos == -1 ? paramName : paramName.substring(0, dotPos);
        return resolveSqlParamByMethodArgs(paramName, rootPropName, Integer.parseInt(paramNumStr), joinPoint, methodArgs);
      }
    }

    // 如果引用的不是 Controller 方法参数，则由 RequestCacheableParamResolver 负责解析。嵌套属性也由 resolver 处理，这
    for (RequestCacheableParamResolver resolver : paramResolvers) {
      if (resolver.supports(paramName)) {
        return resolver.resolve(paramName);
      }
    }
    logger.error("Unresolvable SQL param: controller={}, param={}", getControllerMethod(joinPoint), paramName);
    throw new IllegalArgumentException(getControllerMethod(joinPoint) + " @RequestCacheable#sql 引用的 " + paramName + " 参数不存在");
  }

  @Nullable
  @SuppressWarnings("java:S2139")
  private Object resolveSqlParamByMethodArgs(String paramName, String rootPropName, int paramNum, ProceedingJoinPoint joinPoint, Object[] methodArgs) {
    if (paramNum < 1 || paramNum > methodArgs.length) {
      logger.error("Invalid SQL param number: controller={}, param={}", getControllerMethod(joinPoint), paramName);
      throw new IllegalArgumentException(getControllerMethod(joinPoint) + " @RequestCacheable#sql 引用的 " + rootPropName + " 参数不存在");
    }
    Object rootPropValue = methodArgs[paramNum - 1];
    Object value;
    if (rootPropName.length() == paramName.length()) {
      value = rootPropValue;
    }
    else {
      try {
        value = PropertyUtils.getNestedProperty(rootPropValue, paramName.substring(rootPropName.length() + 1));
      }
      catch (NestedNullException e) {
        // 中间值为空时返回 null
        value = null;
      }
      catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
        logger.error("Failed to resolve sql param: controller={}, paramName={}", getControllerMethod(joinPoint), paramName, e);
        throw new BssException("解析 RequestCacheable 的 SQL 参数失败: " + e.getMessage(), e);
      }
    }
    return value;
  }

  /**
   * 根据方法构造计算 ETag 的键
   *
   * <p>调用方法，使用方法的返回值计算 ETag。</p>
   */
  @Nullable
  @SuppressWarnings({"PMD.PreserveStackTrace", "java:S2139"})
  private Object buildEtagKeysByMethod(ProceedingJoinPoint joinPoint, String method) {
    Object controller = joinPoint.getTarget();
    Object value;
    try {
      value = MethodUtils.invokeMethod(controller, true, method, joinPoint.getArgs());
    }
    catch (NoSuchMethodException | IllegalAccessException e) {
      logger.error("Failed to build etag keys by method: controller={}, method={}", getControllerMethod(joinPoint), method, e);
      throw new BssException("调用计算 ETag 的自定义方法失败: " + e.getMessage(), e);
    }
    catch (InvocationTargetException e) {
      logger.error("Failed to build etag keys by method: controller={}, method={}", getControllerMethod(joinPoint), method, e);
      Throwable cause = e.getCause();
      if (cause instanceof BssException) {
        throw (BssException) cause;
      }
      throw new BssException("调用计算 ETag 的自定义方法失败: " + cause.getMessage(), cause);
    }
    return value;
  }

  /**
   * 获取 Controller 方法的简洁表示，用于日志、错误信息
   */
  private String getControllerMethod(ProceedingJoinPoint joinPoint) {
    Signature signature = joinPoint.getSignature();
    return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
  }

}
