package com.iwhalecloud.bote.portal.adapter;

import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.portal.config.properties.BasicCenterProperties;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * basiccenter 鉴权提供者实现
 *
 * @author chen.linfa
 * @since 2024-11-16
 */
public class BasicCenterAuthProvider extends AbstractCachingAuthProvider<BasicCenterProperties> {
  public BasicCenterAuthProvider(BasicCenterProperties properties) {
    super(properties);
  }

  @SuppressWarnings({"rawtypes", "PMD.GuardLogStatement"})
  @Override
  @Nullable
  protected LoginInfo loadLoginInfo(String sessionId) {
    HttpEntity<?> requestEntity = buildRequest(sessionId);
    String url = properties.getLoggedUrl();
    // 调用登录状态检查接口
    LoginInfo loginInfo = null;
    try {
      logger.debug("Request basiccenter logged api start: url={}, sessionId={}", url, sessionId);
      ResponseEntity<ResultVO> responseEntity = HttpUtil.getRestTemplate().exchange(url, HttpMethod.GET, requestEntity, ResultVO.class);
      logger.debug("Request basiccenter logged api end: sessionId={}", sessionId);
      ResultVO result = responseEntity.getBody();
      if (result != null && result.isSuccess()) {
        loginInfo = JsonUtil.parseJson(JsonUtil.toJsonString(result.getResultObject()), LoginInfo.class);
      }
    }
    catch (HttpStatusCodeException e) {
      // 未登录时有些系统可能为返回 401
      if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
        logger.debug("User not logged: sessionId={}, status={}, response={}", sessionId, e.getStatusCode().value(), e.getResponseBodyAsString());
      }
      else {
        logger.error("Request basiccenter logged api failed: url={}, sessionId={}, status={}, response={}", url, sessionId, e.getStatusCode().value(),
          e.getResponseBodyAsString(), e);
        throw BaseErrorConstant.REQUEST_BASIC_CENTER_LOGGED_API_FAIL.toException(e);
      }
    }
    catch (RuntimeException e) {
      logger.error("Request basiccenter logged api failed: url={}, sessionId={}", url, sessionId, e);
      throw BaseErrorConstant.REQUEST_BASIC_CENTER_LOGGED_API_FAIL.toException(e);
    }
    if (loginInfo != null && loginInfo.getUserId() != null) {
      loginInfo.setDefaultTenantId(properties.getDefaultTenantId());
      return loginInfo;
    }
    else {
      return null;
    }
  }
}
