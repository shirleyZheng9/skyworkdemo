package com.iwhalecloud.bote.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwhalecloud.bote.dto.base.FileDownloadToken;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.redis.core.ValueOperations;

/**
 * {@link FileDownloadCache} 单元测试。
 *
 * <p>构造时由 CacheFactory 取得 ICacheClient，故以 mock CacheFactory 注入 mock ICacheClient。
 * generateFileDownloadToken/validateAndGetToken/getTokenInfo 均依赖 JsonUtil 静态序列化，
 * 故 @BeforeAll 以 mockStatic(SpringUtil) 注入真实 ObjectMapper 供 JsonUtil 加载。</p>
 */
class FileDownloadCacheTest {

  private static MockedStatic<SpringUtil> spring;

  private CacheFactory cacheFactory;
  private ICacheClient cacheClient;
  private ValueOperations<String, String> valueOps;
  private FileDownloadCache cache;

  @BeforeAll
  static void setUpSpring() {
    spring = mockStatic(SpringUtil.class);
    spring.when(() -> SpringUtil.getBean(eq(ObjectMapper.class), any()))
      .thenReturn(CacheTestSupport.jsonObjectMapper());
    // 在 mockStatic 生效且无未完成 stubbing 时触发 JsonUtil 静态初始化，
    // 避免后续在 when().thenReturn() 表达式内首次加载导致 UnfinishedStubbing
    assertThat(JsonUtil.toJsonString("init")).isNotNull();
  }

  @AfterAll
  static void tearDownSpring() {
    spring.close();
  }

  @BeforeEach
  void setUp() {
    cacheFactory = mock(CacheFactory.class);
    cacheClient = mock(ICacheClient.class);
    valueOps = mock(ValueOperations.class);
    when(cacheFactory.getCacheClient(org.mockito.ArgumentMatchers.anyString(),
      org.mockito.ArgumentMatchers.anyString())).thenReturn(cacheClient);
    when(cacheClient.opsForValue()).thenReturn(valueOps);
    cache = new FileDownloadCache(cacheFactory);
  }

  private String tokenJson(LocalDateTime createTime, LocalDateTime expireTime) {
    FileDownloadToken token = new FileDownloadToken();
    token.setToken("t1");
    token.setUserId(1L);
    token.setDocumentId("doc-1");
    token.setFileId(2L);
    token.setFileName("f.txt");
    token.setFileSize(100L);
    token.setCreateTime(createTime);
    token.setExpireTime(expireTime);
    token.setDownloadUrl("http://x/d/t1");
    return JsonUtil.toJsonString(token);
  }

  // ==================== generateFileDownloadToken ====================

  @Test
  void generate_nullUserId_throws() {
    Function<String, String> urlBuilder = token -> "url:" + token;
    assertThatThrownBy(() -> cache.generateFileDownloadToken(null, "doc", 2L, "f", 10L, urlBuilder))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void generate_blankDocumentId_throws() {
    assertThatThrownBy(() -> cache.generateFileDownloadToken(1L, "  ", 2L, "f", 10L, t -> "u"))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void generate_valid_storesToCacheAndAppliesUrlBuilder() {
    FileDownloadToken token = cache.generateFileDownloadToken(1L, "doc-1", 2L, "f.txt", 100L,
      t -> "http://dl/" + t);

    assertThat(token).isNotNull();
    assertThat(token.getUserId()).isEqualTo(1L);
    assertThat(token.getDocumentId()).isEqualTo("doc-1");
    assertThat(token.getFileId()).isEqualTo(2L);
    assertThat(token.getFileName()).isEqualTo("f.txt");
    assertThat(token.getFileSize()).isEqualTo(100L);
    assertThat(token.getDownloadUrl()).isEqualTo("http://dl/" + token.getToken());
    assertThat(token.getExpireTime()).isNotNull();

    verify(valueOps).set(eq("file_download_token:" + token.getToken()),
      org.mockito.ArgumentMatchers.anyString(), eq(5L), eq(TimeUnit.MINUTES));
  }

  // ==================== validateAndGetToken ====================

  @Test
  void validate_blankToken_returnsNull() {
    assertThat(cache.validateAndGetToken("  ")).isNull();
    verifyNoInteractions(valueOps);
  }

  @Test
  void validate_missingInCache_returnsNull() {
    when(valueOps.get("file_download_token:t1")).thenReturn(null);
    assertThat(cache.validateAndGetToken("t1")).isNull();
  }

  @Test
  void validate_emptyStringInCache_returnsNull() {
    when(valueOps.get("file_download_token:t1")).thenReturn("");
    assertThat(cache.validateAndGetToken("t1")).isNull();
  }

  @Test
  void validate_validFutureToken_returnsToken() {
    when(valueOps.get("file_download_token:t1"))
      .thenReturn(tokenJson(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(4)));
    FileDownloadToken token = cache.validateAndGetToken("t1");
    assertThat(token).isNotNull();
    assertThat(token.getUserId()).isEqualTo(1L);
    assertThat(token.getDocumentId()).isEqualTo("doc-1");
  }

  @Test
  void validate_expiredToken_returnsNull() {
    when(valueOps.get("file_download_token:t1"))
      .thenReturn(tokenJson(LocalDateTime.now().minusMinutes(10), LocalDateTime.now().minusMinutes(1)));
    assertThat(cache.validateAndGetToken("t1")).isNull();
  }

  @Test
  void validate_malformedJson_returnsNull() {
    when(valueOps.get("file_download_token:t1")).thenReturn("not-a-json");
    assertThat(cache.validateAndGetToken("t1")).isNull();
  }

  // ==================== getTokenInfo ====================

  @Test
  void getTokenInfo_blankToken_returnsNull() {
    assertThat(cache.getTokenInfo("")).isNull();
  }

  @Test
  void getTokenInfo_missingInCache_returnsNull() {
    when(valueOps.get("file_download_token:t1")).thenReturn(null);
    assertThat(cache.getTokenInfo("t1")).isNull();
  }

  @Test
  void getTokenInfo_valid_returnsTokenWithoutValidityCheck() {
    // 过期 token 也会被返回（getTokenInfo 不校验有效性）
    when(valueOps.get("file_download_token:t1"))
      .thenReturn(tokenJson(LocalDateTime.now().minusMinutes(10), LocalDateTime.now().minusMinutes(1)));
    FileDownloadToken token = cache.getTokenInfo("t1");
    assertThat(token).isNotNull();
    assertThat(token.getToken()).isEqualTo("t1");
  }

  @Test
  void getTokenInfo_malformedJson_returnsNull() {
    when(valueOps.get("file_download_token:t1")).thenReturn("not-a-json");
    assertThat(cache.getTokenInfo("t1")).isNull();
  }
}
