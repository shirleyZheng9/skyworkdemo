package com.iwhalecloud.bote.service.publish.platform.weclaw;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

/**
 * ClawBot HTTP API client（个人微信）。
 *
 * @author chen.linfa
 * @since 2026-04-02
 */
public final class WeClawBotApiClient {
  private static final String DEFAULT_BASE_URL = "https://ilinkai.weixin.qq.com";

  private static final String CHANNEL_VERSION = "2.0.1";

  /**
   * 长轮询 readTimeout=35s，足够等 iLink 服务器推送消息后返回
   */
  private static final RestTemplate LONG_POLLING_CLIENT = HttpUtil.createRestTemplateWithTimeout(5000, 35000);

  @Nullable
  private final String botToken;

  @Nullable
  private final String baseUrl;

  public WeClawBotApiClient(@Nullable String botToken, @Nullable String baseUrl) {
    this.botToken = botToken;
    this.baseUrl = baseUrl;
  }

  private String baseUrl() {
    String url = StringUtils.isBlank(baseUrl) ? DEFAULT_BASE_URL : baseUrl;
    return Strings.CS.removeEnd(url.trim(), "/");
  }

  /**
   * 获取登录二维码。 先以二进制获取响应，再按 JSON 解析。
   * iLink 返回：{ret, qrcode, qrcode_img_content}
   */
  public Map<String, Object> getBotQrcode() {
    String url = baseUrl() + "/ilink/bot/get_bot_qrcode?bot_type=3";
    byte[] bytes = HttpUtil.getForBytes(url, Map.of());
    if (bytes == null || bytes.length == 0) {
      return Map.of("errcode", -1, "errmsg", "empty response");
    }
    String body = new String(bytes, StandardCharsets.UTF_8);
    Map<String, Object> resp = JsonUtil.parseJson(body, new TypeReference<>() {
    });
    if (resp == null) {
      return Map.of("errcode", -1, "errmsg", "JSON parse failed: " + body);
    }
    int ret = parseInt(resp.get("ret"), -1);
    if (ret != 0) {
      return Map.of("errcode", ret, "errmsg", String.valueOf(resp.getOrDefault("errmsg", "API error")));
    }
    String qrcode = String.valueOf(resp.getOrDefault("qrcode", ""));
    String qrcodeUrl = String.valueOf(resp.getOrDefault("qrcode_img_content", ""));
    return Map.of("qrcode", qrcode, "qrcode_url", qrcodeUrl, "expiresIn", 300);
  }

  /**
   * 查询二维码扫码状态。 先以二进制获取响应，再按 JSON 解析。
   * iLink 返回：{ret, status, bot_token/baseurl when confirmed}
   */
  public Map<String, Object> getQrcodeStatus(String qrcode) {
    Assert.hasText(qrcode, "qrcode 不能为空");
    String url = baseUrl() + "/ilink/bot/get_qrcode_status?qrcode=" + qrcode;
    byte[] bytes = HttpUtil.getForBytes(url, Map.of());
    if (bytes == null || bytes.length == 0) {
      return Map.of("errcode", -1, "errmsg", "empty response");
    }
    String body = new String(bytes, StandardCharsets.UTF_8);
    Map<String, Object> resp = JsonUtil.parseJson(body, new TypeReference<>() {
    });
    return resp != null ? resp : Map.of();
  }

  /**
   * 长轮询获取新消息，使用独立配置的 RestTemplate（connectTimeout=5s, readTimeout=35s）。
   */
  public Map<String, Object> getUpdates(@Nullable String cursor) {
    String url = baseUrl() + "/ilink/bot/getupdates";
    Map<String, Object> body = new HashMap<>(4);
    body.put("get_updates_buf", StringUtils.defaultString(cursor));
    body.put("base_info", Map.of("channel_version", CHANNEL_VERSION));
    HttpEntity<Object> entity = new HttpEntity<>(body, buildHeaders());
    ResponseEntity<byte[]> response = LONG_POLLING_CLIENT.exchange(url, HttpMethod.POST, entity, byte[].class);
    byte[] bytes = response.getBody();
    if (bytes == null || bytes.length == 0) {
      return Map.of();
    }
    String responseBody = new String(bytes, StandardCharsets.UTF_8);
    Map<String, Object> resp = JsonUtil.parseJson(responseBody, new TypeReference<>() {
    });
    return resp != null ? resp : Map.of();
  }

  /**
   * 发送文本消息。
   */
  public Map<String, Object> sendText(String toUserId, String text, String contextToken) {
    Assert.hasText(toUserId, "toUserId 不能为空");
    Assert.hasText(text, "text 不能为空");
    Assert.hasText(contextToken, "contextToken 不能为空");
    String url = baseUrl() + "/ilink/bot/sendmessage";
    Map<String, Object> msg = new HashMap<>(8);
    msg.put("from_user_id", "");
    msg.put("to_user_id", toUserId);
    msg.put("client_id", java.util.UUID.randomUUID().toString());
    msg.put("message_type", 2);
    msg.put("message_state", 2);
    msg.put("context_token", contextToken);
    msg.put("item_list", java.util.List.of(Map.of("type", 1, "text_item", Map.of("text", text))));
    Map<String, Object> body = new HashMap<>(2);
    body.put("msg", msg);
    body.put("base_info", Map.of("channel_version", CHANNEL_VERSION));
    Map<String, Object> resp = HttpUtil.post(url, body, new ParameterizedTypeReference<>() {
    }, buildHeaders());
    return resp != null ? resp : Map.of();
  }

  private HttpHeaders buildHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
    Assert.hasText(botToken, "botToken 不能为空");
    headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + botToken);
    headers.set("AuthorizationType", "ilink_bot_token");
    // X-WECHAT-UIN: 每次请求带一个随机 uint32 的 base64 值，服务器用于防重放
    int uinVal = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
    String uinB64 = Base64.getEncoder().encodeToString(String.valueOf(uinVal).getBytes(StandardCharsets.UTF_8));
    headers.set("X-WECHAT-UIN", uinB64);
    return headers;
  }

  private int parseInt(Object value, int defaultValue) {
    if (value == null) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(String.valueOf(value));
    }
    catch (Exception e) {
      return defaultValue;
    }
  }
}
