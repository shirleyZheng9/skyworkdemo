package com.iwhalecloud.bote.portal.adapter;

import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.portal.config.properties.UportalProperties;
import com.iwhalecloud.bote.portal.dto.UportalLoggedResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * uportal 鉴权提供者实现
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class UportalAuthProvider extends AbstractCachingAuthProvider<UportalProperties> {
  public UportalAuthProvider(UportalProperties properties) {
    super(properties);
  }

  @Override
  @Nullable
  protected LoginInfo loadLoginInfo(String sessionId) {
    HttpEntity<?> requestEntity = buildRequest(sessionId);
    String url = properties.getLoggedUrl();
    // 调用登录状态检查接口
    LoginInfo loginInfo;
    try {
      logger.debug("Request uportal logged api start: url={}, sessionId={}", url, sessionId);
      ResponseEntity<UportalLoggedResponse> responseEntity = HttpUtil.getRestTemplate().exchange(url, HttpMethod.GET, requestEntity, UportalLoggedResponse.class);
      logger.debug("Request uportal logged api end: sessionId={}", sessionId);
      loginInfo = parseLoggedResponse(sessionId, responseEntity.getBody());
    }
    catch (HttpStatusCodeException e) {
      // 未登录时有些系统可能为返回 401
      if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
        logger.debug("User not logged: sessionId={}, status={}, response={}", sessionId, e.getStatusCode().value(), e.getResponseBodyAsString());
        loginInfo = null;
      }
      else {
        logger.error("Request uportal logged api failed: url={}, sessionId={}, status={}, response={}", url, sessionId, e.getStatusCode().value(),
          e.getResponseBodyAsString(), e);
        throw BaseErrorConstant.REQUEST_UPORTAL_LOGGED_API_FAIL.toException(e);
      }
    }
    catch (RuntimeException e) {
      logger.error("Request uportal logged api failed: url={}, sessionId={}", url, sessionId, e);
      throw BaseErrorConstant.REQUEST_UPORTAL_LOGGED_API_FAIL.toException(e);
    }
    return loginInfo;
  }

  /**
   * 解析检查登录状态的响应结果
   */
  @Nullable
  private LoginInfo parseLoggedResponse(String sessionId, @Nullable UportalLoggedResponse response) {
    LoginInfo loginInfo = null;
    if (response == null) {
      logger.warn("Empty uportal logged api response: sessionId={}", sessionId);
    }
    else if (!response.isSuccess()) {
      logger.debug("Invalid uportal session: sessionId={}, message={}", sessionId, response.getResultMsg());
    }
    else if (response.getUserInfo() == null || response.getUserInfo().getUserId() == null) {
      logger.warn("Invalid uportal logged api response, empty userInfo: sessionId={}, response={}", sessionId, response);
    }
    else {
      loginInfo = response.toLoginInfo(sessionId);
      loginInfo.setDefaultTenantId(properties.getDefaultTenantId());
    }
    return loginInfo;
  }
}
