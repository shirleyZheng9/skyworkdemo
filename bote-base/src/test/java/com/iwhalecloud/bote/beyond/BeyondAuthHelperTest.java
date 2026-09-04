package com.iwhalecloud.bote.beyond;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.config.properties.BeyondProperties;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import okhttp3.Headers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequest;

/**
 * {@link BeyondAuthHelper} 单元测试。
 *
 * <p>构造时需解析 RSA 私钥，故在 @BeforeAll 生成真实 RSA 密钥对，以其 PKCS8 编码作为
 * BeyondProperties.jwtKey 注入。createToken 依赖 SessionUtil.getLoginInfo()，以 mockStatic 注入
 * LoginInfo。覆盖三种请求头构造方式与 token 签名。</p>
 */
class BeyondAuthHelperTest {

  private static MockedStatic<SessionUtil> sessionUtil;
  private static String jwtKey;

  private BeyondProperties properties;
  private BeyondAuthHelper helper;

  @BeforeAll
  static void setUpKey() throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    KeyPair keyPair = generator.generateKeyPair();
    jwtKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
    sessionUtil = mockStatic(SessionUtil.class);
    sessionUtil.when(SessionUtil::getLoginInfo).thenReturn(buildLoginInfo());
  }

  @AfterAll
  static void tearDown() {
    sessionUtil.close();
  }

  @BeforeEach
  void setUp() throws Exception {
    properties = mock(BeyondProperties.class);
    when(properties.getJwtKey()).thenReturn(jwtKey);
    helper = new BeyondAuthHelper(properties);
  }

  @Test
  void createToken_returnsSignedJwt() {
    String token = helper.createToken();
    assertThat(token).isNotNull();
    // JWT 由 header.payload.signature 三段组成
    assertThat(token.split("\\.")).hasSize(3);
  }

  @Test
  void buildHttpHeaders_containsTokenAndSystemCode() {
    HttpHeaders headers = helper.buildHttpHeaders();
    assertThat(headers.getFirst("Beyond-Token")).isNotBlank();
    assertThat(headers.getFirst("System-Code")).isEqualTo("BOT");
  }

  @Test
  void buildOkHttpHeaders_containsTokenAndSystemCode() {
    Headers headers = helper.buildOkHttpHeaders();
    assertThat(headers.get("Beyond-Token")).isNotBlank();
    assertThat(headers.get("System-Code")).isEqualTo("BOT");
  }

  @Test
  void addTokenHeader_addsToRequest() {
    ClientHttpRequest request = mock(ClientHttpRequest.class);
    HttpHeaders headers = new HttpHeaders();
    when(request.getHeaders()).thenReturn(headers);
    helper.addTokenHeader(request);
    assertThat(headers.getFirst("Beyond-Token")).isNotBlank();
    assertThat(headers.getFirst("System-Code")).isEqualTo("BOT");
  }

  @Test
  void createToken_differentCallsProduceValidTokens() {
    String token1 = helper.createToken();
    String token2 = helper.createToken();
    assertThat(token1.split("\\.")).hasSize(3);
    assertThat(token2.split("\\.")).hasSize(3);
  }

  private static LoginInfo buildLoginInfo() {
    LoginInfo info = new LoginInfo();
    info.setUserId(1L);
    info.setUserName("admin");
    info.setRealName("管理员");
    return info;
  }
}
