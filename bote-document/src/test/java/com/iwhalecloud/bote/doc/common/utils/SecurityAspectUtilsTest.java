package com.iwhalecloud.bote.doc.common.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.util.AntPathMatcher;

/**
 * {@link SecurityAspectUtils} 单元测试。
 *
 * <p>纯逻辑 + 反射 + mock HttpServletRequest，不启动 Spring 容器。
 * isIgnoreUrl 使用真实 AntPathMatcher；extractIdFromParameter 通过反射获取真实
 * Parameter（其 getName() 受 -parameters 编译开关影响，故断言时复用同一参数名）。</p>
 */
class SecurityAspectUtilsTest {

  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  // 用于 extractIdFromParameter：通过反射拿到真实 Parameter
  @SuppressWarnings("unused")
  void sampleMethod(Long docId, String name) {
  }

  // ---- holder 类，供反射提取 ID ----

  @SuppressWarnings("unused")
  static class WithIdField {
    private final Long id = 42L;
  }

  @SuppressWarnings("unused")
  static class WithStringIdField {
    private final String id = "99";
  }

  @SuppressWarnings("unused")
  static class WithBadTypeField {
    private final Object id = new Object();
  }

  static class WithIdGetter {
    @SuppressWarnings("unused")
    private Long notId = 5L;

    public Long getId() {
      return 77L;
    }
  }

  @SuppressWarnings("unused")
  static class NoId {
    private final String name = "x";
  }

  // ============ isIgnoreUrl ============

  @Test
  void isIgnoreUrl_emptyList_returnsFalse() {
    assertThat(SecurityAspectUtils.isIgnoreUrl(Collections.emptyList(), "/api/x", pathMatcher))
      .isFalse();
    assertThat(SecurityAspectUtils.isIgnoreUrl(null, "/api/x", pathMatcher)).isFalse();
  }

  @Test
  void isIgnoreUrl_exactContains_returnsTrue() {
    List<String> ignores = Arrays.asList("/health", "/api/x");
    assertThat(SecurityAspectUtils.isIgnoreUrl(ignores, "/api/x", pathMatcher)).isTrue();
  }

  @Test
  void isIgnoreUrl_antPatternMatch_returnsTrue() {
    List<String> ignores = Arrays.asList("/doc/**", "/health");
    assertThat(SecurityAspectUtils.isIgnoreUrl(ignores, "/doc/123/children", pathMatcher))
      .isTrue();
  }

  @Test
  void isIgnoreUrl_noMatch_returnsFalse() {
    List<String> ignores = Arrays.asList("/doc/**", "/health");
    assertThat(SecurityAspectUtils.isIgnoreUrl(ignores, "/api/library/list", pathMatcher))
      .isFalse();
  }

  // ============ extractIdFromParameter ============

  @Test
  void extractIdFromParameter_nameMatches_returnsConverted() throws Exception {
    Method method = SecurityAspectUtilsTest.class.getDeclaredMethod(
      "sampleMethod", Long.class, String.class);
    Parameter docIdParam = method.getParameters()[0];
    // 复用真实参数名（兼容是否启用 -parameters）
    assertThat(SecurityAspectUtils.extractIdFromParameter(docIdParam, 123L, docIdParam.getName()))
      .isEqualTo(123L);
  }

  @Test
  void extractIdFromParameter_nameMismatch_returnsNull() throws Exception {
    Method method = SecurityAspectUtilsTest.class.getDeclaredMethod(
      "sampleMethod", Long.class, String.class);
    Parameter nameParam = method.getParameters()[1];
    assertThat(SecurityAspectUtils.extractIdFromParameter(nameParam, "abc", "docId")).isNull();
  }

  // ============ convertToLong ============

  @Test
  void convertToLong_null_returnsNull() {
    assertThat(SecurityAspectUtils.convertToLong(null)).isNull();
  }

  @Test
  void convertToLong_longValue_returnsSame() {
    assertThat(SecurityAspectUtils.convertToLong(123L)).isEqualTo(123L);
  }

  @Test
  void convertToLong_numberSubclass_returnsLongValue() {
    assertThat(SecurityAspectUtils.convertToLong(7)).isEqualTo(7L);
    assertThat(SecurityAspectUtils.convertToLong(3.14d)).isEqualTo(3L);
  }

  @Test
  void convertToLong_numericString_returnsLong() {
    assertThat(SecurityAspectUtils.convertToLong("456")).isEqualTo(456L);
  }

  @Test
  void convertToLong_invalidString_returnsNull() {
    assertThat(SecurityAspectUtils.convertToLong("abc")).isNull();
  }

  @Test
  void convertToLong_unsupportedType_returnsNull() {
    assertThat(SecurityAspectUtils.convertToLong(new Object())).isNull();
  }

  // ============ extractIdFromHeader ============

  @Test
  void extractIdFromHeader_validNumber_returnsLong() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("X-Doc-Id")).thenReturn("889");
    assertThat(SecurityAspectUtils.extractIdFromHeader(request, "X-Doc-Id")).isEqualTo(889L);
  }

  @Test
  void extractIdFromHeader_blankHeader_returnsNull() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("X-Doc-Id")).thenReturn("");
    assertThat(SecurityAspectUtils.extractIdFromHeader(request, "X-Doc-Id")).isNull();
    when(request.getHeader("X-Doc-Id")).thenReturn(null);
    assertThat(SecurityAspectUtils.extractIdFromHeader(request, "X-Doc-Id")).isNull();
  }

  @Test
  void extractIdFromHeader_invalidNumber_returnsNull() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("X-Doc-Id")).thenReturn("not-a-number");
    assertThat(SecurityAspectUtils.extractIdFromHeader(request, "X-Doc-Id")).isNull();
  }

  // ============ extractIdByReflection ============

  @Test
  void extractIdByReflection_nullObj_returnsNull() {
    assertThat(SecurityAspectUtils.extractIdByReflection(null, "id", "getId")).isNull();
  }

  @Test
  void extractIdByReflection_longField_returnsValue() {
    assertThat(SecurityAspectUtils.extractIdByReflection(new WithIdField(), "id", "getId"))
      .isEqualTo(42L);
  }

  @Test
  void extractIdByReflection_stringField_returnsConverted() {
    assertThat(SecurityAspectUtils.extractIdByReflection(new WithStringIdField(), "id", "getId"))
      .isEqualTo(99L);
  }

  @Test
  void extractIdByReflection_badTypeField_returnsNull() {
    assertThat(SecurityAspectUtils.extractIdByReflection(new WithBadTypeField(), "id", "getId"))
      .isNull();
  }

  @Test
  void extractIdByReflection_noField_fallsBackToGetter() {
    // 字段 "id" 不存在 -> 走 getter "getId"
    assertThat(SecurityAspectUtils.extractIdByReflection(new WithIdGetter(), "id", "getId"))
      .isEqualTo(77L);
  }

  @Test
  void extractIdByReflection_neitherFieldNorGetter_returnsNull() {
    assertThat(SecurityAspectUtils.extractIdByReflection(new NoId(), "id", "getId")).isNull();
  }

  // ============ attemptExtractIdFromObjects ============

  @Test
  void attemptExtractIdFromObjects_nullArgs_returnsNull() {
    assertThat(SecurityAspectUtils.attemptExtractIdFromObjects(null, "id", "getId")).isNull();
  }

  @Test
  void attemptExtractIdFromObjects_skipsNullAndReturnsFirstMatch() {
    Object[] args = {null, new NoId(), new WithIdField()};
    assertThat(SecurityAspectUtils.attemptExtractIdFromObjects(args, "id", "getId"))
      .isEqualTo(42L);
  }

  @Test
  void attemptExtractIdFromObjects_noMatch_returnsNull() {
    Object[] args = {new NoId(), null};
    assertThat(SecurityAspectUtils.attemptExtractIdFromObjects(args, "id", "getId")).isNull();
  }
}
