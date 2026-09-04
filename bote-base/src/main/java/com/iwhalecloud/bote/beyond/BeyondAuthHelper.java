package com.iwhalecloud.bote.beyond;

import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.config.properties.BeyondProperties;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import okhttp3.Headers;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.stereotype.Component;

/**
 * 百应鉴权工具类
 *
 * @author bianjp
 * @since 2025-07-18
 */
@Component
@ConditionalOnBooleanProperty("beyond.enabled")
public final class BeyondAuthHelper {
  /** 鉴权 token 失效时间(秒) */
  private static final int TOKEN_EXPIRE_SECONDS = 60 * 10;
  /** 鉴权 token 请求头名称 */
  @SuppressWarnings("UastIncorrectHttpHeaderInspection")
  private static final String TOKEN_HEADER = "Beyond-Token";
  /** 系统编码请求头 */
  @SuppressWarnings("UastIncorrectHttpHeaderInspection")
  private static final String SYSTEM_CODE_HEADER = "System-Code";

  /** jwt 签名密钥 */
  private final PrivateKey privateKey;

  public BeyondAuthHelper(BeyondProperties properties) throws NoSuchAlgorithmException, InvalidKeySpecException {
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(properties.getJwtKey()));
    this.privateKey = keyFactory.generatePrivate(keySpec);
  }

  /**
   * 在请求头中添加 token
   */
  public void addTokenHeader(ClientHttpRequest request) {
    String token = createToken();
    HttpHeaders headers = request.getHeaders();
    headers.add(TOKEN_HEADER, token);
    headers.add(SYSTEM_CODE_HEADER, "BOT");
  }

  /**
   * 构造 Http 请求头
   */
  public HttpHeaders buildHttpHeaders() {
    String token = createToken();
    HttpHeaders headers = new HttpHeaders();
    headers.set(TOKEN_HEADER, token);
    headers.set(SYSTEM_CODE_HEADER, "BOT");
    return headers;
  }
  /**
   * 构造 OkHttp 请求头
   */
  public Headers buildOkHttpHeaders() {
    String token = createToken();
    return Headers.of(TOKEN_HEADER, token, SYSTEM_CODE_HEADER, "BOT");
  }

  /**
   * 创建 token
   */
  public String createToken() {
    LoginInfo loginInfo = SessionUtil.getLoginInfo();
    Map<String, Object> body = new HashMap<>();
    body.put("userId", loginInfo.getUserId());
    body.put("userCode", loginInfo.getUserName());
    body.put("userName", loginInfo.getRealName());
    return Jwts.builder()
      .header()
      .add("alg", SIG.RS256.getId())
      .add("typ", "JWT")
      .and()
      .claims(body)
      .expiration(DateUtils.addSeconds(new Date(), TOKEN_EXPIRE_SECONDS))
      .signWith(privateKey)
      .compact();
  }
}
