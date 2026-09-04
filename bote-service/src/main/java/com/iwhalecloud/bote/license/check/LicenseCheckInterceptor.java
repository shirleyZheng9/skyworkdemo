package com.iwhalecloud.bote.license.check;

import com.iwhalecloud.bote.license.cache.LicenseCache;
import com.iwhalecloud.bote.license.entity.LicenseExtInfoDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * license 检查请求拦截器
 *
 * @author cheng.xu
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class LicenseCheckInterceptor implements HandlerInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(LicenseCheckInterceptor.class);

  private final List<LicenseExtInfoChecker> licenseExtInfoCheckers;
  private final LicenseCache licenseCache;
  private final LicenseChecker licenseChecker;

  public LicenseCheckInterceptor(ObjectProvider<List<LicenseExtInfoChecker>> licenseExtInfoCheckersProvider, LicenseCache licenseCache,
                                 LicenseChecker licenseChecker) {
    this.licenseExtInfoCheckers = ListUtils.emptyIfNull(licenseExtInfoCheckersProvider.getIfAvailable());
    this.licenseCache = licenseCache;
    this.licenseChecker = licenseChecker;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
    // 忽略静态资源请求
    if (!(handler instanceof HandlerMethod)) {
      return true;
    }

    // 检查 license 基本信息
    CheckResult checkResult = licenseChecker.checkLicense(false);
    if (!checkResult.isSuccess()) {
      return errorResponse(request, response, checkResult.getErrorCode(), checkResult.getErrorMsg());
    }

    // 检查 license 扩展信息
    LicenseExtInfoDTO licenseExtInfo = licenseCache.getLicense();
    for (LicenseExtInfoChecker checker : licenseExtInfoCheckers) {
      if (checker.skip(licenseExtInfo, request)) {
        continue;
      }
      checkResult = checker.check(licenseExtInfo, request);
      if (!checkResult.isSuccess()) {
        return errorResponse(request, response, checkResult.getErrorCode(), checkResult.getErrorMsg());
      }
    }
    return true;
  }

  /**
   * 返回检查失败响应
   */
  @SuppressFBWarnings({"CRLF_INJECTION_LOGS", "XSS_SERVLET"})
  private boolean errorResponse(HttpServletRequest request, HttpServletResponse response, String errorCode, String errorMsg) throws IOException {
    logger.error("License check failed: request={}, errorCode={}, errorMsg={}", request.getRequestURI(), errorCode, errorMsg);
    ResultVO<Void> result = new ResultVO<>(errorCode, errorMsg);
    response.setStatus(200);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(JsonUtil.toJsonString(result));
    return false;
  }
}
