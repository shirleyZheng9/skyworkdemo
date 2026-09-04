package com.iwhalecloud.bote.service.publish.platform.weclaw;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.dto.beyond.PublishChannelDTO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 个人微信（ClawBot）配置。
 *
 * @author chen.linfa
 * @since 2026-04-02
 */
@Getter
public final class WeClawBotConfig {
  /** iLink API baseUrl，默认 https://ilinkai.weixin.qq.com */
  private final String baseUrl;
  /** 扫码登录后获得的 bot_token（Bearer token） */
  private final String botToken;
  /** 长轮询游标，用于断点续传 */
  private String cursor;

  private WeClawBotConfig(String baseUrl, String botToken) {
    this.baseUrl = baseUrl;
    this.botToken = botToken;
  }

  public static WeClawBotConfig fromMap(PublishChannelDTO dto) {
    if (dto == null) {
      return new WeClawBotConfig("", "");
    }
    return new WeClawBotConfig("", "");
  }

  /**
   * 从发布记录 publishParams JSON 解析配置（Controller 回填 botToken/baseUrl 后，此 JSON 包含完整信息）。
   */
  public static WeClawBotConfig fromPublishParams(String publishParamsJson) {
    if (StringUtils.isBlank(publishParamsJson)) {
      return new WeClawBotConfig("", "");
    }
    try {
      Map<String, Object> map = JsonUtil.parseJsonRequired(publishParamsJson, new TypeReference<>() {
      });
      String baseUrl = MapUtils.getString(map, "baseUrl", "");
      String botToken = MapUtils.getString(map, "botToken", "");
      String cursor = MapUtils.getString(map, "cursor", "");
      WeClawBotConfig config = new WeClawBotConfig(baseUrl, botToken);
      config.cursor = cursor;
      return config;
    }
    catch (Exception ignored) {
      // ignore parse errors
    }
    return new WeClawBotConfig("", "");
  }

  /**
   * 将当前配置序列化回 publishParams JSON（包含 botToken/baseUrl/cursor）。
   */
  public String toPublishParamsJson() {
    Map<String, String> map = new HashMap<>();
    if (StringUtils.isNotBlank(baseUrl)) {
      map.put("baseUrl", baseUrl);
    }
    if (StringUtils.isNotBlank(botToken)) {
      map.put("botToken", botToken);
    }
    if (StringUtils.isNotBlank(cursor)) {
      map.put("cursor", cursor);
    }
    return JsonUtil.toJsonString(map);
  }

  public void setCursor(String cursor) {
    this.cursor = cursor;
  }

  public boolean isValidForReceive() {
    return botToken != null && !botToken.isBlank();
  }
}
