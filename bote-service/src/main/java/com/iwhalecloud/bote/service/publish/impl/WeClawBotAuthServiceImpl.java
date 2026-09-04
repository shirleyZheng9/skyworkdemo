package com.iwhalecloud.bote.service.publish.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.PublishChannelEnum;
import com.iwhalecloud.bote.dto.channel.AiChannelDTO;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
import com.iwhalecloud.bote.mapper.channel.AiChannelManagerMapper;
import com.iwhalecloud.bote.mapper.publish.ResourcePublishRecordMapper;
import com.iwhalecloud.bote.service.publish.IWeClawBotAuthService;
import com.iwhalecloud.bote.service.publish.SdkConnectionManager;
import com.iwhalecloud.bote.service.publish.platform.weclaw.WeClawBotApiClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * 个人微信（ClawBot）认证与连接管理实现。
 *
 * @author chen.linfa
 * @since 2026-04-11
 */
@Service
@RequiredArgsConstructor
public class WeClawBotAuthServiceImpl implements IWeClawBotAuthService {

  private final ResourcePublishRecordMapper resourcePublishRecordMapper;
  private final AiChannelManagerMapper aiChannelManagerMapper;
  private final SdkConnectionManager sdkConnectionManager;

  private static final String WECLAWBOT_BASE_URL = "https://ilinkai.weixin.qq.com";

  private static final String ATTR_VALUE_NAME = "attrValueName";
  private static final String ATTR_VALUE = "attrValue";
  private static final String IS_CONFIRMED = "isConfirmed";
  private static final String BOT_TOKEN_LABEL = "Bot Token";

  @Override
  public Map<String, Object> getQrcode(String callbackCode) {
    ResourcePublishRecordDTO record = resourcePublishRecordMapper.getRecordByCallbackCode(callbackCode);
    if (record == null) {
      throw new BssException("发布记录不存在");
    }
    WeClawBotApiClient client = new WeClawBotApiClient(null, WECLAWBOT_BASE_URL);
    return client.getBotQrcode();
  }

  @Override
  public Map<String, Object> confirmLogin(String callbackCode, String qrcode, Integer maxWaitSeconds,
    Integer pollIntervalMs, Long userId) {
    if (StringUtils.isBlank(qrcode)) {
      throw new BssException("qrcode 不能为空");
    }
    ResourcePublishRecordDTO record = resourcePublishRecordMapper.getRecordByCallbackCode(callbackCode);
    if (record == null) {
      throw new BssException("发布记录不存在");
    }
    int maxWait = maxWaitSeconds == null ? 300 : Math.max(1, maxWaitSeconds);
    int interval = pollIntervalMs == null ? 1500 : Math.max(200, pollIntervalMs);
    Map<String, Object> publishParams = parsePublishParams(record.getPublishParams());
    WeClawBotApiClient client = new WeClawBotApiClient(null, WECLAWBOT_BASE_URL);

    long deadline = System.currentTimeMillis() + maxWait * 1000L;
    Map<String, Object> last = Map.of();
    while (System.currentTimeMillis() < deadline) {
      Map<String, Object> status = client.getQrcodeStatus(qrcode);
      last = status;
      String s = String.valueOf(status.getOrDefault("status", ""));
      Map<String, Object> confirmed = completeLoginIfConfirmed(record, publishParams, WECLAWBOT_BASE_URL, status, s, callbackCode,
        userId);
      if (confirmed != null) {
        return confirmed;
      }
      if ("expired".equalsIgnoreCase(s)) {
        updateAiChannelWeClawIsConfirmed(record.getTenantId(), record.getResourceId(), userId, BaseConsts.FALSE);
        throw new BssException("二维码已过期，请重新获取二维码");
      }
      sleepPollInterval(interval);
    }
    updateAiChannelWeClawIsConfirmed(record.getTenantId(), record.getResourceId(), userId, BaseConsts.FALSE);
    return Map.of("status", String.valueOf(last.getOrDefault("status", "timeout")));
  }

  @Nullable
  private Map<String, Object> completeLoginIfConfirmed(ResourcePublishRecordDTO record,
    Map<String, Object> publishParams, String baseUrl, Map<String, Object> status, String statusStr,
    String callbackCode, Long userId) {
    if (!"confirmed".equalsIgnoreCase(statusStr)) {
      return null;
    }
    String botToken = String.valueOf(status.getOrDefault("bot_token", ""));
    String newBaseUrl = String.valueOf(status.getOrDefault("baseurl", baseUrl));
    if (StringUtils.isBlank(botToken)) {
      updateAiChannelWeClawIsConfirmed(record.getTenantId(), record.getResourceId(), userId, BaseConsts.FALSE);
      throw new BssException("扫码已确认，但 bot_token 为空");
    }
    Map<String, Object> newParams = new HashMap<>(publishParams);
    newParams.put("botToken", botToken);
    if (StringUtils.isNotBlank(newBaseUrl)) {
      newParams.put("baseUrl", newBaseUrl);
    }
    ResourcePublishRecordDTO toUpdate = new ResourcePublishRecordDTO();
    toUpdate.setRecordId(record.getRecordId());
    toUpdate.setPublishParams(JsonUtil.toJsonString(newParams));
    toUpdate.setUpdatorId(userId);
    toUpdate.setExtResourceId(record.getExtResourceId());
    toUpdate.setPublishStatus(record.getPublishStatus());
    toUpdate.setPublishMsg(record.getPublishMsg());
    toUpdate.setCallbackCode(record.getCallbackCode());
    resourcePublishRecordMapper.updateResourcePublishRecord(toUpdate);
    updateAiChannelWeClawIsConfirmed(record.getTenantId(), record.getResourceId(), userId, BaseConsts.TRUE);
    sdkConnectionManager.restartConnection(callbackCode);
    return Map.of("status", "confirmed");
  }

  /**
   * 更新 bt_ai_channel.channel_json 中的 isConfirmed；格式与渠道列表一致（JSON 数组，attrValueName/attrValue）。
   */
  private void updateAiChannelWeClawIsConfirmed(Long tenantId, Long botId, Long userId, String isConfirmed) {
    List<AiChannelDTO> channels = aiChannelManagerMapper.selectAiChannelList(tenantId, botId, userId);
    if (CollectionUtils.isEmpty(channels)) {
      return;
    }
    AiChannelDTO weclaw = channels.stream()
      .filter(c -> PublishChannelEnum.WECLAWBOT.getCode().equalsIgnoreCase(c.getChannelType()))
      .findFirst()
      .orElse(null);
    if (weclaw == null) {
      return;
    }
    AiChannelDTO toUpdate = new AiChannelDTO();
    toUpdate.setId(weclaw.getId());
    toUpdate.setChannelJson(patchChannelJsonIsConfirmed(weclaw.getChannelJson(), isConfirmed));
    toUpdate.setUpdatorId(userId);
    aiChannelManagerMapper.updateAiChannel(toUpdate);
  }

  /**
   * 在 channel_json 上写入 isConfirmed：支持数组；兼容历史对象 {@code {"isConfirmed":"T"}}。不落库 Bot Token。
   */
  private static String patchChannelJsonIsConfirmed(String existingJson, String isConfirmed) {
    List<Map<String, Object>> rows = new ArrayList<>();
    if (StringUtils.isNotBlank(existingJson)) {
      String trimmed = existingJson.trim();
      if (trimmed.startsWith("[")) {
        List<Map<String, Object>> parsed = JsonUtil.parseJson(existingJson, new TypeReference<>() {
        });
        if (parsed != null) {
          rows.addAll(parsed);
        }
      }
      else if (trimmed.startsWith("{")) {
        Map<String, Object> legacy = JsonUtil.parseJson(existingJson, new TypeReference<>() {
        });
        if (legacy != null && legacy.containsKey(IS_CONFIRMED)) {
          Map<String, Object> row = new HashMap<>(2);
          row.put(ATTR_VALUE_NAME, IS_CONFIRMED);
          row.put(ATTR_VALUE, String.valueOf(legacy.get(IS_CONFIRMED)));
          rows.add(row);
        }
      }
    }
    rows.removeIf(r -> BOT_TOKEN_LABEL.equals(String.valueOf(r.get(ATTR_VALUE_NAME))));
    for (Map<String, Object> row : rows) {
      if (IS_CONFIRMED.equals(String.valueOf(row.get(ATTR_VALUE_NAME)))) {
        row.put(ATTR_VALUE, isConfirmed);
        return JsonUtil.toJsonString(rows);
      }
    }
    Map<String, Object> row = new HashMap<>(2);
    row.put(ATTR_VALUE_NAME, IS_CONFIRMED);
    row.put(ATTR_VALUE, isConfirmed);
    rows.add(row);
    return JsonUtil.toJsonString(rows);
  }

  private void sleepPollInterval(int intervalMs) {
    try {
      Thread.sleep(intervalMs);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new BssException("等待扫码被中断", e);
    }
  }

  @Override
  public void disconnect(String callbackCode, Long userId) {
    ResourcePublishRecordDTO record = resourcePublishRecordMapper.getRecordByCallbackCode(callbackCode);
    if (record == null) {
      throw new BssException("发布记录不存在");
    }
    aiChannelManagerMapper.deleteAiChannelsBySpaceBotUser(record.getTenantId(), record.getResourceId(), userId);
    resourcePublishRecordMapper.deleteResourcePublishRecord(record.getTenantId(), record.getRecordId(), userId);
    sdkConnectionManager.broadcastStopConnection(callbackCode);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> parsePublishParams(String json) {
    if (StringUtils.isBlank(json)) {
      return new HashMap<>();
    }
    Object obj = JsonUtil.parseJson(json, Object.class);
    if (obj instanceof Map<?, ?> map) {
      return (Map<String, Object>) map;
    }
    return new HashMap<>();
  }
}
