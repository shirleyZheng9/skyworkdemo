package com.iwhalecloud.bote.llm.client.util;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * {@link ModelValidationUtil} 单元测试
 *
 * <p>覆盖 validateUrl 的合法/空/非法 scheme/格式错误等分支</p>
 */
class ModelValidationUtilTest {

  private static final String TITLE = "接口地址";

  // ==================== 合法 URL ====================

  @Test
  void validateUrl_validHttp() {
    assertThatCode(() -> ModelValidationUtil.validateUrl("http://localhost:8080/x", TITLE))
        .doesNotThrowAnyException();
  }

  @Test
  void validateUrl_validHttps() {
    assertThatCode(() -> ModelValidationUtil.validateUrl("https://example.com/x", TITLE))
        .doesNotThrowAnyException();
  }

  // ==================== 空 URL ====================

  @Test
  void validateUrl_null_throws() {
    assertThatThrownBy(() -> ModelValidationUtil.validateUrl(null, TITLE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(TITLE)
        .hasMessageContaining("不能为空");
  }

  @Test
  void validateUrl_empty_throws() {
    assertThatThrownBy(() -> ModelValidationUtil.validateUrl("", TITLE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(TITLE)
        .hasMessageContaining("不能为空");
  }

  // ==================== 非法 scheme ====================

  @Test
  void validateUrl_wrongScheme_throws() {
    assertThatThrownBy(() -> ModelValidationUtil.validateUrl("ftp://x", TITLE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(TITLE)
        .hasMessageContaining("http:// 或 https://");
  }

  // ==================== 格式错误 ====================

  @Test
  void validateUrl_malformed_throws() {
    assertThatThrownBy(() -> ModelValidationUtil.validateUrl("http://[invalid", TITLE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(TITLE)
        .hasMessageContaining("不合法");
  }

  // ==================== title 出现在各错误分支 ====================

  @Test
  void validateUrl_titleInMessage_wrongScheme() {
    String customTitle = "模型地址";
    assertThatThrownBy(() -> ModelValidationUtil.validateUrl("ftp://x", customTitle))
        .hasMessageContaining(customTitle);
  }

  @Test
  void validateUrl_titleInMessage_malformed() {
    String customTitle = "模型地址";
    assertThatThrownBy(() -> ModelValidationUtil.validateUrl("http://[invalid", customTitle))
        .hasMessageContaining(customTitle);
  }
}
