package com.iwhalecloud.bote.doc.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * 反爬虫检测与辅助工具类。
 *
 * <p>该类不会真正"突破"网站的反爬虫，而是帮助识别常见的人机验证 / 拦截页面，
 * 让上层逻辑可以给出更清晰的错误信息或在需要时扩展更多策略。</p>
 *
 * <p>思路来源于 firecrawl 对 anti‑bot 的处理：优先检测"被挡住"的情况，
 * 然后再决定是否升级代理、切换引擎等。本项目当前仅实现检测与错误提示能力。</p>
 *
 * @author auto
 * @since 2026-02-28
 */
public final class AntiBotUtil {

  private AntiBotUtil() {
  }

  /**
   * 判断页面内容是否<strong>很可能</strong>是反爬 / 人机验证页面。
   *
   * @param content 页面内容（可能是 HTML 或 JSON）
   * @return true 表示疑似被反爬拦截，而不是正常正文内容
   */
  public static boolean isLikelyAntiBotPage(String content) {
    if (StringUtils.isBlank(content)) {
      return false;
    }

    // 首先检查是否是 JSON 格式的错误响应（如知乎、微博等 API 返回的错误）
    if (isJsonErrorResponse(content)) {
      return true;
    }

    // 然后检查 HTML 内容
    Document doc;
    try {
      doc = Jsoup.parse(content);
    }
    catch (Exception e) {
      return false;
    }

    String title = StringUtils.defaultString(doc.title()).toLowerCase();
    String bodyText = StringUtils.defaultString(doc.text()).toLowerCase();

    // 一些常见的英文拦截 / 人机验证文案
    String[] englishKeywords = {
      "are you a robot",
      "are you human",
      "verify you are human",
      "verify that you are human",
      "access denied",
      "access denied |",
      "access to this site is blocked",
      "unusual traffic from your computer network",
      "sorry, you have been blocked",
      "checking your browser before accessing",
      "just a moment",
      "bot detection",
      "bot protection",
      "captcha",
      "cloudflare",
      "attention required",
      "one more step",
      "please enable javascript and cookies"
    };

    // 一些常见的中文拦截 / 人机验证文案
    String[] chineseKeywords = {
      "安全验证",
      "行为验证",
      "人机验证",
      "验证您是否是机器人",
      "访问受限",
      "访问受限，请稍后再试",
      "您的访问频率过高",
      "当前访问存在风险",
      "请完成安全验证后继续访问",
      "请先完成验证",
      "系统检测到异常访问",
      "为了保证您的正常使用",
      "您的行为存在异常",
      "您当前请求存在异常",
      "暂时限制本次访问",
      "限制访问",
      "请求异常",
      "访问频率过快",
      "访问过于频繁",
      "请求过于频繁",
      "操作过于频繁",
      "请稍后再试",
      "小管家反馈" // 知乎特有
    };

    // 少量 meta / 结构化标记关键词
    String[] technicalMarkers = {
      "cf-ray",          // Cloudflare
      "cf-bm",           // Cloudflare bot management cookie
      "distil_r_captcha", // Distil Networks
      "gee_test",        // 极验
      "hcaptcha",        // hCaptcha
      "recaptcha"        // Google reCAPTCHA
    };

    String haystack = title + " " + bodyText;

    for (String k : englishKeywords) {
      if (haystack.contains(k)) {
        return true;
      }
    }
    for (String k : chineseKeywords) {
      if (haystack.contains(k)) {
        return true;
      }
    }
    for (String k : technicalMarkers) {
      if (haystack.contains(k)) {
        return true;
      }
    }

    return false;
  }

  /**
   * 检查是否是 JSON 格式的错误响应（常见于 API 接口返回的反爬错误）。
   *
   * <p>支持的格式示例：</p>
   * <ul>
   *   <li>知乎：{"error":{"message":"您当前请求存在异常...","code":40362}}</li>
   *   <li>微博：{"error":"访问受限","code":403}</li>
   *   <li>通用：{"error":"access denied","status":403}</li>
   * </ul>
   *
   * @param content 响应内容
   * @return true 如果是 JSON 格式的错误响应
   */
  private static boolean isJsonErrorResponse(String content) {
    if (StringUtils.isBlank(content)) {
      return false;
    }

    String trimmed = content.trim();
    // 快速检查：JSON 通常以 { 或 [ 开头
    if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
      return false;
    }

    try {
      JsonNode root = JsonUtil.getObjectMapper().readTree(trimmed);

      // 检查 error 字段
      if (root.has("error") && checkErrorField(root.get("error"))) {
        return true;
      }

      // 检查其他常见的错误字段
      if (checkOtherErrorFields(root)) {
        return true;
      }
    }
    catch (JsonProcessingException e) {
      // 不是有效的 JSON，忽略
      // JsonProcessingException 继承自 IOException，所以捕获它即可覆盖所有 JSON 解析异常
      return false;
    }

    return false;
  }

  /**
   * 检查 error 字段是否是反爬错误。
   *
   * @param errorNode error 字段的 JsonNode
   * @return true 如果是反爬错误
   */
  private static boolean checkErrorField(JsonNode errorNode) {
    if (errorNode.isObject()) {
      return checkErrorObject(errorNode);
    }
    else if (errorNode.isTextual()) {
      return checkErrorText(errorNode.asText(""));
    }
    return false;
  }

  /**
   * 检查 error 对象（包含 code 和 message）。
   *
   * @param errorNode error 对象的 JsonNode
   * @return true 如果是反爬错误
   */
  private static boolean checkErrorObject(JsonNode errorNode) {
    // 检查错误码
    if (errorNode.has("code")) {
      int code = errorNode.get("code").asInt(0);
      if (isAntiBotErrorCode(code)) {
        return true;
      }
    }

    // 检查错误消息
    if (errorNode.has("message")) {
      String errorText = errorNode.get("message").asText("").toLowerCase();
      if (containsAntiBotKeywords(errorText)) {
        return true;
      }
    }

    return false;
  }

  /**
   * 检查错误文本是否包含反爬关键词。
   *
   * @param errorText 错误文本
   * @return true 如果包含反爬关键词
   */
  private static boolean checkErrorText(String errorText) {
    if (StringUtils.isBlank(errorText)) {
      return false;
    }
    return containsAntiBotKeywords(errorText.toLowerCase());
  }

  /**
   * 检查错误码是否是反爬相关的错误码。
   *
   * @param code 错误码
   * @return true 如果是反爬错误码
   */
  private static boolean isAntiBotErrorCode(int code) {
    return code == 403 || code == 40362 || code == 429 || (code >= 400 && code < 500);
  }

  /**
   * 检查文本是否包含反爬关键词。
   *
   * @param text 要检查的文本
   * @return true 如果包含反爬关键词
   */
  private static boolean containsAntiBotKeywords(String text) {
    if (StringUtils.isBlank(text)) {
      return false;
    }

    String[] errorKeywords = {
      "访问受限", "访问限制", "限制访问", "请求异常", "访问异常",
      "访问频率", "过于频繁", "操作频繁", "请稍后再试",
      "access denied", "access restricted", "rate limit",
      "too many requests", "forbidden", "blocked"
    };

    for (String keyword : errorKeywords) {
      if (text.contains(keyword)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 检查其他常见的错误字段（status、code、message）。
   *
   * @param root JSON 根节点
   * @return true 如果是反爬错误
   */
  private static boolean checkOtherErrorFields(JsonNode root) {
    // 检查 status 字段
    if (root.has("status")) {
      int status = root.get("status").asInt(0);
      if (status >= 400) {
        return true;
      }
    }

    // 检查 code 字段
    if (root.has("code")) {
      int code = root.get("code").asInt(0);
      if (code >= 400) {
        return true;
      }
    }

    // 检查 message 字段
    if (root.has("message")) {
      String message = root.get("message").asText("").toLowerCase();
      if (containsAntiBotKeywords(message)) {
        return true;
      }
    }

    return false;
  }

  /**
   * 给出一个简要的人类可读说明，提示可能是反爬拦截导致抓取失败。
   *
   * @param url  当前抓取的 URL
   * @return 说明性提示语
   */
  public static String buildAntiBotHintMessage(String url) {
    String safeUrl = StringUtils.defaultIfBlank(url, "目标链接");
    return "目标网站可能触发了反爬虫或人机验证机制，无法正常获取页面内容：url=" + safeUrl
      + "。建议尝试以下方式：1）在浏览器中手动访问一次该页面并完成验证；2）降低抓取频率；3）如有需要，可联系运维为该域名配置代理 / 特殊策略。";
  }
}
