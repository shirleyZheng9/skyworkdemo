package com.iwhalecloud.bote.config;

import com.iwhalecloud.bassc.basiccenter.config.CookiesProperties;
import com.iwhalecloud.bassc.basiccenter.config.SecurityProperties;
import com.iwhalecloud.bote.service.security.BoteCookieSerializer;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.session.web.http.CookieSerializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * EncryptionConfig 中 cookieSerializer Bean 的 cookieCleanupPaths CSV 解析单元测试。
 *
 * @author zhaolei2
 * @since 2026-07-08
 */
class EncryptionConfigTest {

  @SuppressWarnings("unchecked")
  private static List<String> cleanupPathsOf(CookieSerializer serializer) throws Exception {
    Field field = BoteCookieSerializer.class.getDeclaredField("cleanupPaths");
    field.setAccessible(true);
    return (List<String>) field.get(serializer);
  }

  /** 直接调用 @Bean 方法（不走 Spring 代理）；构造器 4 个依赖该 Bean 未使用，传 null 安全。 */
  private static CookieSerializer buildSerializer(String cleanupPathsCsv) {
    SecurityProperties sp = new SecurityProperties();
    CookiesProperties cp = new CookiesProperties();
    cp.getCookie().setName("BOTE_SESSION");
    EncryptionConfig config = new EncryptionConfig(null, null, null, null);
    return config.cookieSerializer(sp, cp, cleanupPathsCsv);
  }

  @Test
  void parsesSinglePath() throws Exception {
    assertEquals(List.of("/bote"), cleanupPathsOf(buildSerializer("/bote")));
  }

  @Test
  void parsesMultiplePaths() throws Exception {
    assertEquals(List.of("/bote", "/app"), cleanupPathsOf(buildSerializer("/bote,/app")));
  }

  @Test
  void trimsWhitespace() throws Exception {
    assertEquals(List.of("/bote", "/app"), cleanupPathsOf(buildSerializer(" /bote , /app ")));
  }

  @Test
  void filtersEmptySegments() throws Exception {
    assertEquals(List.of("/bote", "/app"), cleanupPathsOf(buildSerializer("/bote,,/app,")));
  }

  @Test
  void deduplicates() throws Exception {
    assertEquals(List.of("/bote", "/app"), cleanupPathsOf(buildSerializer("/bote,/bote,/app")));
  }
}
