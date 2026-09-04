package com.iwhalecloud.bote.doc.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * {@link AntiBotUtil} 单元测试。
 *
 * <p>HTML 分支经 Jsoup 解析、按关键字命中判定；JSON 分支经 {@code JsonUtil.getObjectMapper()}
 * 解析。JsonUtil 静态初始化用 {@code SpringUtil.getBean(Class, Supplier)} 的 fallback
 * 供应者（context 为 null 时返回新建 ObjectMapper），故无 Spring 容器亦能正常解析 JSON。</p>
 */
class AntiBotUtilTest {

  // ============ isLikelyAntiBotPage - blank / 基础 ============

  @Test
  void isLikelyAntiBotPage_blank_returnsFalse() {
    assertThat(AntiBotUtil.isLikelyAntiBotPage(null)).isFalse();
    assertThat(AntiBotUtil.isLikelyAntiBotPage("")).isFalse();
    assertThat(AntiBotUtil.isLikelyAntiBotPage("   ")).isFalse();
  }

  // ============ isLikelyAntiBotPage - JSON 错误响应 ============

  @Test
  void isLikelyAntiBotPage_jsonErrorObjectWithAntiBotCode_returnsTrue() {
    // 知乎式：error.code 为反爬错误码 40362
    String zhihu = "{\"error\":{\"message\":\"您当前请求存在异常\",\"code\":40362}}";
    assertThat(AntiBotUtil.isLikelyAntiBotPage(zhihu)).isTrue();
  }

  @Test
  void isLikelyAntiBotPage_jsonErrorObjectWith4xxCode_returnsTrue() {
    // isAntiBotErrorCode: 429 / 403 / 4xx 区间
    assertThat(AntiBotUtil.isLikelyAntiBotPage("{\"error\":{\"code\":429}}")).isTrue();
    assertThat(AntiBotUtil.isLikelyAntiBotPage("{\"error\":{\"code\":403}}")).isTrue();
    assertThat(AntiBotUtil.isLikelyAntiBotPage("{\"error\":{\"code\":418}}")).isTrue();
  }

  @Test
  void isLikelyAntiBotPage_jsonErrorObjectWith5xxCode_returnsFalse() {
    // 500 不属于反爬错误码区间（>=400 && <500 不含 500），且无 message 关键字
    assertThat(AntiBotUtil.isLikelyAntiBotPage("{\"error\":{\"code\":500}}")).isFalse();
    assertThat(AntiBotUtil.isLikelyAntiBotPage("{\"error\":{\"code\":399}}")).isFalse();
  }

  @Test
  void isLikelyAntiBotPage_jsonErrorObjectWithKeywordMessage_returnsTrue() {
    // checkErrorObject -> message 含反爬关键字
    assertThat(AntiBotUtil.isLikelyAntiBotPage("{\"error\":{\"message\":\"访问频率过高\"}}"))
      .isTrue();
    assertThat(AntiBotUtil.isLikelyAntiBotPage("{\"error\":{\"message\":\"access denied\"}}"))
      .isTrue();
  }

  @Test
  void isLikelyAntiBotPage_jsonErrorTextual_returnsTrue() {
    // error 为文本：微博式 {"error":"访问受限","code":403}
    String weibo = "{\"error\":\"访问受限\",\"code\":403}";
    assertThat(AntiBotUtil.isLikelyAntiBotPage(weibo)).isTrue();
    assertThat(AntiBotUtil.isLikelyAntiBotPage("{\"error\":\"请求异常\"}")).isTrue();
  }

  @Test
  void isLikelyAntiBotPage_jsonOtherErrorFields_returnsTrue() {
    // checkOtherErrorFields：顶层 status / code / message 命中
    assertThat(AntiBotUtil.isLikelyAntiBotPage("{\"error\":\"access denied\",\"status\":403}"))
      .isTrue();
    assertThat(AntiBotUtil.isLikelyAntiBotPage("{\"status\":403}")).isTrue();
    assertThat(AntiBotUtil.isLikelyAntiBotPage("{\"code\":429}")).isTrue();
    assertThat(AntiBotUtil.isLikelyAntiBotPage("{\"message\":\"forbidden\"}")).isTrue();
  }

  @Test
  void isLikelyAntiBotPage_jsonNotError_returnsFalse() {
    // 非 JSON 错误响应：isJsonErrorResponse=false，Jsoup 解析后无关键字 -> false
    assertThat(AntiBotUtil.isLikelyAntiBotPage("{\"name\":\"document\"}")).isFalse();
  }

  @Test
  void isLikelyAntiBotPage_jsonArray_returnsFalse() {
    // 以 [ 开头的合法 JSON 数组，无 error 字段 -> false
    assertThat(AntiBotUtil.isLikelyAntiBotPage("[1,2,3]")).isFalse();
  }

  @Test
  void isLikelyAntiBotPage_invalidJson_returnsFalse() {
    // 以 { 开头但非合法 JSON：readTree 抛 JsonProcessingException -> 走 HTML 解析无关键字 -> false
    assertThat(AntiBotUtil.isLikelyAntiBotPage("{not a json")).isFalse();
  }

  // ============ isLikelyAntiBotPage - HTML ============

  @Test
  void isLikelyAntiBotPage_htmlEnglishKeyword_returnsTrue() {
    String html = "<html><head><title>Just a moment</title></head>"
      + "<body>checking your browser</body></html>";
    assertThat(AntiBotUtil.isLikelyAntiBotPage(html)).isTrue();
    assertThat(AntiBotUtil.isLikelyAntiBotPage("<html><body>Access Denied</body></html>"))
      .isTrue();
  }

  @Test
  void isLikelyAntiBotPage_htmlChineseKeyword_returnsTrue() {
    String html = "<html><head><title>安全验证</title></head><body>请先完成验证</body></html>";
    assertThat(AntiBotUtil.isLikelyAntiBotPage(html)).isTrue();
  }

  @Test
  void isLikelyAntiBotPage_htmlTechnicalMarker_returnsTrue() {
    String html = "<html><head><title>ok</title></head><body>cf-ray 48f0a1</body></html>";
    assertThat(AntiBotUtil.isLikelyAntiBotPage(html)).isTrue();
  }

  @Test
  void isLikelyAntiBotPage_htmlNormalContent_returnsFalse() {
    String html = "<html><head><title>我的文档</title></head><body>这是正文内容</body></html>";
    assertThat(AntiBotUtil.isLikelyAntiBotPage(html)).isFalse();
  }

  // ============ buildAntiBotHintMessage ============

  @Test
  void buildAntiBotHintMessage_withUrl_containsUrl() {
    String msg = AntiBotUtil.buildAntiBotHintMessage("https://example.com/x");
    assertThat(msg).contains("https://example.com/x");
    assertThat(msg).contains("反爬虫");
  }

  @Test
  void buildAntiBotHintMessage_blankUrl_usesDefault() {
    String msg = AntiBotUtil.buildAntiBotHintMessage("");
    assertThat(msg).contains("目标链接");
    assertThat(msg).doesNotContain("url=null");
  }
}
