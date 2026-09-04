package com.iwhalecloud.bote.common.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * {@link BeyondContentType} 单元测试
 *
 * <p>纯常量枚举，仅含 getCode；覆盖 values/valueOf 契约与 getCode 取值。</p>
 */
class BeyondContentTypeTest {

  @Test
  void values_containsAllSixTypes() {
    assertThat(BeyondContentType.values())
        .containsExactly(
            BeyondContentType.FORM,
            BeyondContentType.REASONING,
            BeyondContentType.TEXT,
            BeyondContentType.CHART,
            BeyondContentType.BOTE,
            BeyondContentType.BOTE_PAGE_FUNC);
  }

  @Test
  void getCode_returnsExpectedCode() {
    assertThat(BeyondContentType.FORM.getCode()).isEqualTo("2010");
    assertThat(BeyondContentType.REASONING.getCode()).isEqualTo("1001");
    assertThat(BeyondContentType.TEXT.getCode()).isEqualTo("1002");
    assertThat(BeyondContentType.CHART.getCode()).isEqualTo("2001");
    assertThat(BeyondContentType.BOTE.getCode()).isEqualTo("2011");
    assertThat(BeyondContentType.BOTE_PAGE_FUNC.getCode()).isEqualTo("2014");
  }

  @Test
  void valueOf_roundtripsForEachConstant() {
    for (BeyondContentType e : BeyondContentType.values()) {
      assertThat(BeyondContentType.valueOf(e.name())).isSameAs(e);
    }
  }
}
