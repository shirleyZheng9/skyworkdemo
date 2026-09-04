package com.iwhalecloud.bote.doc.module.collaboration.socket.auth;

import com.iwhalecloud.bote.doc.module.collaboration.cache.SocketServerCache;
import com.iwhalecloud.bote.doc.module.collaboration.constant.ConnectTypeEnum;
import com.iwhalecloud.bote.doc.module.collaboration.socket.config.WebSocketProperties;
import io.netty.handler.codec.http.FullHttpRequest;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 默认Token校验器实现
 *
 * @author Aiqing
 * @since 2025/08/29
 */
@Component
@RequiredArgsConstructor
public class DefaultTokenValidator implements TokenValidator {
  private static final Logger logger = LoggerFactory.getLogger(DefaultTokenValidator.class);

  private final WebSocketProperties webSocketProperties;
  private final SocketServerCache socketServerCache;

  @Override
  public UserAuthInfo validateToken(String token, String connectType) {
    if (token == null || token.trim().isEmpty()) {
      logger.debug("Token为空");
      return null;
    }
    // 区分内部token检验，和外部链接校验
    ConnectTypeEnum connectTypeEnum = ConnectTypeEnum.fromCode(connectType);
    if (connectTypeEnum == null) {
      return null;
    }
    if (Objects.equals(connectTypeEnum, ConnectTypeEnum.INTERNAL)) {
      // 比对内部的固定token
      String internalAuthKey = webSocketProperties.getInternalAuthKey();
      if (Objects.equals(internalAuthKey, token)) {
        return createInternalAuthInfo();
      }
    }
    return validateUserToken(token);
  }

  @Override
  public UserAuthInfo validateFromRequest(FullHttpRequest request) {
    return null;
  }

  public UserAuthInfo validateUserToken(String token) {
    UserAuthInfo authInfo = socketServerCache.getUserSocketAuth(token);
    if (authInfo == null) {
      return null;
    }
    // 校验token是否过期
    if (authInfo.isExpired()) {
      return null;
    }
    return authInfo;
  }

  private UserAuthInfo createInternalAuthInfo() {
    UserAuthInfo authInfo = new UserAuthInfo();
    authInfo.setUserId(-1L);
    authInfo.setUserName("internal");
    authInfo.setUserCode("internal");
    return authInfo;
  }
}
