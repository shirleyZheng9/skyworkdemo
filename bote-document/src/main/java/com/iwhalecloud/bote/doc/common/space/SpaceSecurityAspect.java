package com.iwhalecloud.bote.doc.common.space;

import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.model.SpaceBaseRO;
import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import com.iwhalecloud.bote.doc.common.space.annotation.IgnoreSpace;
import com.iwhalecloud.bote.doc.common.utils.SecurityAspectUtils;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 企业空间安全切面
 * 处理企业空间ID的提取和安全检查
 *
 * @author lizuyin
 * @since 2025-10-24
 */
@Aspect
@Component
@Order(2)
@SuppressWarnings("PMD.GuardLogStatement")
public class SpaceSecurityAspect {

  private static final Logger logger = LoggerFactory.getLogger(SpaceSecurityAspect.class);

  private static final List<String> LOCAL_IGNORE_URLS = new ArrayList<>();
  private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

  static {
    LOCAL_IGNORE_URLS.add("/**/doc.html");
    LOCAL_IGNORE_URLS.add("/error");
    LOCAL_IGNORE_URLS.add("/favicon.ico");
    LOCAL_IGNORE_URLS.add("/**/swagger-resources");
    LOCAL_IGNORE_URLS.add("/**/swagger-resources/**");
    LOCAL_IGNORE_URLS.add("/v3/api-docs/swagger-config");
    LOCAL_IGNORE_URLS.add("/**/v2/api-docs");
    LOCAL_IGNORE_URLS.add("/**/v3/api-docs");
    LOCAL_IGNORE_URLS.add("/**/webjars/**");
    LOCAL_IGNORE_URLS.add("/**/favicon.ico");
  }

  private final SpaceProperties spaceProperties;

  @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
  public SpaceSecurityAspect(SpaceProperties spaceProperties) {
    this.spaceProperties = spaceProperties;
  }

  /**
   * 判断是否是可忽略URL
   */
  private static boolean isIgnoreUrl(List<String> ignoreUrls, String uri) {
    if (CollectionUtils.isEmpty(ignoreUrls)) {
      return false;
    }
    // 快速匹配，保证性能
    if (ignoreUrls.contains(uri)) {
      return true;
    }
    // 逐个 Ant 路径匹配
    for (String url : ignoreUrls) {
      if (PATH_MATCHER.match(url, uri)) {
        return true;
      }
    }
    return false;
  }




  /**
   * 拦截所有Controller方法
   */
  @Around("execution(* com.iwhalecloud.bote.doc..controller..*(..))")
  // -@cs[IllegalThrowsCheck] joinPoint.proceed may throw anything
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    HttpServletRequest request = getCurrentRequest();
    if (request == null) {
      return joinPoint.proceed();
    }

    Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

    // 检查是否忽略企业空间检查
    if (checkIfIgnore(request, method)) {
      SpaceContextHolder.setIgnore(true);
      return joinPoint.proceed();
    }

    // 提取企业空间ID
    Long spaceId = extractSpaceId(request, joinPoint);

    // 企业空间ID可以为空，不强制要求
    if (spaceId != null) {
      // 设置企业空间上下文
      SpaceContextHolder.setSpaceId(spaceId);
    }

    try {
      return joinPoint.proceed();
    }
    finally {
      // 清理企业空间上下文
      SpaceContextHolder.clear();
    }
  }

  /**
   * 获取当前HTTP请求
   */
  private HttpServletRequest getCurrentRequest() {
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return attributes != null ? attributes.getRequest() : null;
  }

  /**
   * 检查是否忽略企业空间检查
   */
  private boolean checkIfIgnore(HttpServletRequest request, Method method) {
    if (spaceProperties == null || !Objects.equals(Boolean.TRUE, spaceProperties.getEnable())) {
      return true;
    }
    if (isIgnoreUrl(request, spaceProperties)) {
      return true;
    }

    IgnoreSpace ignoreSpace = method.getAnnotation(IgnoreSpace.class);
    if (ignoreSpace != null) {
      return true;
    }
    // 获取控制器类的类型
    Class<?> controllerClass = method.getDeclaringClass();
    // 获取类上标识
    IgnoreSpace classLevelIgnore = controllerClass.getAnnotation(IgnoreSpace.class);
    return classLevelIgnore != null;
  }

  /**
   * 检查是否为忽略的URL
   */
  private boolean isIgnoreUrl(HttpServletRequest request, SpaceProperties spaceProperties) {
    String requestUri = request.getRequestURI();
    boolean ignoreUrl = isIgnoreUrl(new ArrayList<>(spaceProperties.getIgnoreUrls()), requestUri);
    if (ignoreUrl) {
      return true;
    }
    return isIgnoreUrl(LOCAL_IGNORE_URLS, requestUri);
  }

  /**
   * 提取企业空间ID
   */
  private Long extractSpaceId(HttpServletRequest request, ProceedingJoinPoint joinPoint) {
    // 1. 从URL参数获取
    String spaceParam = request.getParameter(DocBaseConsts.PARAM_SPACE_ID);
    if (StringUtils.isNotBlank(spaceParam)) {
      try {
        return Long.valueOf(spaceParam);
      }
      catch (NumberFormatException e) {
        logger.debug("URL参数中的企业空间ID格式错误: {}", spaceParam);
      }
    }

    // 2. 从Header获取
    Long spaceInHeader = SecurityAspectUtils.extractIdFromHeader(request, DocBaseConsts.SPACE_HEADER);
    if (spaceInHeader != null) {
      return spaceInHeader;
    }

    // 3. 从方法参数中提取
    return extractSpaceIdFromMethodArgs(joinPoint);
  }

  /**
   * 从方法参数中提取企业空间ID
   */
  private Long extractSpaceIdFromMethodArgs(ProceedingJoinPoint joinPoint) {
    Object[] args = joinPoint.getArgs();
    Parameter[] parameters = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameters();

    for (int i = 0; i < parameters.length; i++) {
      Parameter parameter = parameters[i];
      Object arg = args[i];

      if (arg == null) {
        continue;
      }
      Long spaceId = SecurityAspectUtils.extractIdFromParameter(parameter, arg, DocBaseConsts.PARAM_SPACE_ID);
      if (spaceId != null) {
        return spaceId;
      }
      // 检查是否为SpaceBaseRO类型
      if (arg instanceof SpaceBaseRO) {
        SpaceBaseRO spaceBaseRO = (SpaceBaseRO) arg;
        if (spaceBaseRO.getSpaceId() != null) {
          return spaceBaseRO.getSpaceId();
        }
      }
      // 检查是否为TenantBaseRO类型
      if (arg instanceof TenantBaseRO) {
        TenantBaseRO tenantBaseRO = (TenantBaseRO) arg;
        if (tenantBaseRO.getSpaceId() != null) {
          return tenantBaseRO.getSpaceId();
        }
      }
    }
    // 如果未解析出，尝试从BaseEntity的子类中获取
    Long spaceId = attemptExtractFromObject(parameters, args);
    if (spaceId != null) {
      return spaceId;
    }
    return null;
  }

  private Long attemptExtractFromObject(Parameter[] parameters, Object[] args) {
    for (int i = 0; i < parameters.length; i++) {
      Object arg = args[i];

      if (arg == null) {
        continue;
      }
      // 检查是否为BaseEntity或其子类
      if (arg instanceof BaseEntity) {
        // 尝试通过反射查找spaceId字段
        Long spaceId = SecurityAspectUtils.extractIdByReflection(arg, "spaceId", "getSpaceId");
        if (spaceId != null) {
          logger.debug("通过反射从对象中提取到企业空间ID: spaceId={}, className={}",
            spaceId, arg.getClass().getSimpleName());
          return spaceId;
        }
      }
    }
    return null;
  }

}
