package com.iwhalecloud.bote.portal.adapter;

import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.portal.config.properties.NgportalProperties;
import com.iwhalecloud.bote.portal.dto.NgportalLoginResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * ngportal 鉴权提供者实现
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
public class NgportalAuthProvider extends AbstractCachingAuthProvider<NgportalProperties> {
  public NgportalAuthProvider(NgportalProperties properties) {
    super(properties);
  }

  @Override
  @Nullable
  @SuppressWarnings("PMD.GuardLogStatement")
  protected LoginInfo loadLoginInfo(String sessionId) {
    HttpEntity<?> requestEntity = buildRequest(sessionId);
    String url = properties.getLoggedUrl();
    // 调用登录状态检查接口
    LoginInfo loginInfo;
    try {
      logger.debug("Request ngportal logged api start: url={}, sessionId={}", url, sessionId);
      ResponseEntity<NgportalLoginResponse> responseEntity = HttpUtil.getRestTemplate().exchange(url, HttpMethod.GET, requestEntity, NgportalLoginResponse.class);
      logger.debug("Request ngportal logged api end: sessionId={}, response={}", sessionId, responseEntity.getBody());
      loginInfo = parseLoggedResponse(sessionId, responseEntity.getBody());
    }
    catch (HttpStatusCodeException e) {
      // 未登录时有些系统可能会返回 401
      if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
        logger.debug("User not logged: sessionId={}, status={}, response={}", sessionId, e.getStatusCode().value(), e.getResponseBodyAsString());
        loginInfo = null;
      }
      else {
        logger.error("Request ngportal logged api failed: url={}, sessionId={}, status={}, response={}", url, sessionId, e.getStatusCode().value(),
          e.getResponseBodyAsString(), e);
        throw BaseErrorConstant.REQUEST_NGPORTAL_LOGGED_API_FAIL.toException(e);
      }
    }
    catch (RuntimeException e) {
      logger.error("Request ngportal logged api failed: url={}, sessionId={}", url, sessionId, e);
      throw BaseErrorConstant.REQUEST_NGPORTAL_LOGGED_API_FAIL.toException(e);
    }
    return loginInfo;
  }

  /**
   * 解析检查登录状态的响应结果
   */
  @Nullable
  private LoginInfo parseLoggedResponse(String sessionId, @Nullable NgportalLoginResponse response) {
    LoginInfo loginInfo = null;
    if (response == null) {
      logger.warn("Empty ngportal logged api response: sessionId={}", sessionId);
    }
    else if (response.getUserId() == null) {
      logger.debug("Invalid ngportal session: sessionId={}", sessionId);
    }
    else {
      loginInfo = response.toLoginInfo(sessionId);
      loginInfo.setDefaultTenantId(properties.getDefaultTenantId());
    }
    return loginInfo;
  }
}
