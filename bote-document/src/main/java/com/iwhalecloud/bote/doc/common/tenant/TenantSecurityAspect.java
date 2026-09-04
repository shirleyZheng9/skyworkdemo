package com.iwhalecloud.bote.doc.common.tenant;

import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import com.iwhalecloud.bote.doc.common.tenant.annotation.IgnoreTenant;
import com.iwhalecloud.bote.doc.common.utils.SecurityAspectUtils;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 租户安全切面
 * 处理租户ID的提取和安全检查
 *
 * @author Aiqing
 * @since 2025-01-06
 */
@Aspect
@Component
@Order(1)
@SuppressWarnings("PMD.GuardLogStatement")
public class TenantSecurityAspect {

  private static final Logger logger = LoggerFactory.getLogger(TenantSecurityAspect.class);

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

  private final TenantProperties tenantProperties;
  private final List<TenantSecurityCustomizer> securityCustomizers;

  @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
  public TenantSecurityAspect(TenantProperties tenantProperties,
                              ObjectProvider<List<TenantSecurityCustomizer>> securityCustomizerProvider) {
    this.tenantProperties = tenantProperties;
    this.securityCustomizers = securityCustomizerProvider.getIfAvailable();
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

    // 检查是否忽略租户检查
    if (checkIfIgnore(request, method)) {
      TenantContextHolder.setIgnore(true);
      return joinPoint.proceed();
    }

    // 提取租户ID
    Long tenantId = extractTenantId(request, joinPoint);

    if (tenantId == null) {
      return ResultVO.fail("400", "未传递租户ID", null, null);
    }
    // 权限检查
    if (CollectionUtils.isNotEmpty(securityCustomizers)) {
      for (TenantSecurityCustomizer customizer : securityCustomizers) {
        boolean checked = customizer.checkAccess(tenantId);
        if (!checked) {
          return handleAccessDenied();
        }
      }
    }
    // 设置租户上下文
    TenantContextHolder.setTenantId(tenantId);
    try {
      return joinPoint.proceed();
    }
    finally {
      // 清理租户上下文
      TenantContextHolder.clear();
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
   * 检查是否忽略租户检查
   */
  private boolean checkIfIgnore(HttpServletRequest request, Method method) {
    if (tenantProperties == null || !Objects.equals(Boolean.TRUE, tenantProperties.getEnable())) {
      return true;
    }
    if (isIgnoreUrl(request, tenantProperties)) {
      return true;
    }

    IgnoreTenant ignoreTenant = method.getAnnotation(IgnoreTenant.class);
    if (ignoreTenant != null) {
      return true;
    }
    // 获取控制器类的类型
    Class<?> controllerClass = method.getDeclaringClass();
    // 获取类上标识
    IgnoreTenant classLevelIgnore = controllerClass.getAnnotation(IgnoreTenant.class);
    return classLevelIgnore != null;
  }

  /**
   * 检查是否为忽略的URL
   */
  private boolean isIgnoreUrl(HttpServletRequest request, TenantProperties tenantProperties) {
    String requestUri = request.getRequestURI();
    boolean ignoreUrl = SecurityAspectUtils.isIgnoreUrl(new ArrayList<>(tenantProperties.getIgnoreUrls()), requestUri, PATH_MATCHER);
    if (ignoreUrl) {
      return true;
    }
    return SecurityAspectUtils.isIgnoreUrl(LOCAL_IGNORE_URLS, requestUri, PATH_MATCHER);
  }

  /**
   * 提取租户ID
   */
  private Long extractTenantId(HttpServletRequest request, ProceedingJoinPoint joinPoint) {
    // 1. 从URL参数获取
    String tenantParam = request.getParameter(DocBaseConsts.PARAM_TENANT_ID);
    if (StringUtils.isNotBlank(tenantParam)) {
      try {
        return Long.valueOf(tenantParam);
      }
      catch (NumberFormatException e) {
        logger.debug("URL参数中的租户ID格式错误: {}", tenantParam);
      }
    }

    // 2. 从方法参数中提取
    Long tenantIdFromArgs = extractTenantIdFromMethodArgs(joinPoint);
    if (tenantIdFromArgs != null) {
      return tenantIdFromArgs;
    }

    // 3. 从Header获取
    return SecurityAspectUtils.extractIdFromHeader(request, DocBaseConsts.TENANT_HEADER);
  }

  /**
   * 从方法参数中提取租户ID
   */
  private Long extractTenantIdFromMethodArgs(ProceedingJoinPoint joinPoint) {
    Object[] args = joinPoint.getArgs();
    Parameter[] parameters = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameters();

    for (int i = 0; i < parameters.length; i++) {
      Parameter parameter = parameters[i];
      Object arg = args[i];

      if (arg == null) {
        continue;
      }
      Long tenantId = SecurityAspectUtils.extractIdFromParameter(parameter, arg, DocBaseConsts.PARAM_TENANT_ID);
      if (tenantId != null) {
        return tenantId;
      }
      // 检查是否为TenantBaseRO类型
      if (arg instanceof TenantBaseRO) {
        TenantBaseRO tenantBaseRO = (TenantBaseRO) arg;
        if (tenantBaseRO.getTenantId() != null) {
          return tenantBaseRO.getTenantId();
        }
      }
    }
    // 如果未解析出，尝试从BaseEntity的子类中获取
    Long tenantId = attemptExtractFromObject(parameters, args);
    if (tenantId != null) {
      return tenantId;
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
        // 尝试通过反射查找tenantId字段
        Long tenantId = SecurityAspectUtils.extractIdByReflection(arg, "tenantId", "getTenantId");
        if (tenantId != null) {
          logger.debug("通过反射从对象中提取到租户ID: tenantId={}, className={}",
            tenantId, arg.getClass().getSimpleName());
          return tenantId;
        }
      }
    }
    return null;
  }

  /**
   * 处理访问被拒绝的情况
   */
  private Object handleAccessDenied() {
    return ResultVO.fail("403", "无权访问此租户", null, null);
  }
}
