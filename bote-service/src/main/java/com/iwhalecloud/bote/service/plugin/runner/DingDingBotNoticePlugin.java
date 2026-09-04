package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.DingDingBotNoticePluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 钉钉机器人通知插件执行器
 *
 * @author qian.sisheng
 * @see <a href="https://open.dingtalk.com/document/orgapp/custom-bot-to-send-group-chat-messages?spm=ding_open_doc.document.0.0.7863e645jiDs9u">custom-bot-to-send-group-chat-messages</a>
 * @since 2025-04-10
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class DingDingBotNoticePlugin extends AbstractPlugin<DingDingBotNoticePluginParams> {

  /**  发送消息的url */
  private static final String URL = "https://oapi.dingtalk.com/robot/send";
  /**  MAC 算法的标准名称 */
  private static final String ALGORITHM = "HmacSHA256";
  /** 钉钉机器人通知请求参数 */
  private static final ClassPathResource resource = new ClassPathResource("/plugin-params/dingDingBotNotice.json");

  public DingDingBotNoticePlugin() {
    super(DingDingBotNoticePluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_DING_DING_BOT_NOTICE;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    try (InputStream inputStream = resource.getInputStream()) {
      return JsonUtil.parseJsonRequired(inputStream, ParameterSpec.class);
    }
    catch (Exception e) {
      logger.error("Failed to createRequestParameter, message={}", e.getMessage(), e);
      throw new BssException("创建钉钉机器人通知插件请求参数失败: ", e.getMessage(), e);
    }
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(
      ParameterSpec.newProperty("errmsg", "错误信息，执行成功ok", AttrDataType.STRING),
      ParameterSpec.newProperty("errcode", "错误码，0表示成功", AttrDataType.INTEGER)
    ));
  }

  @Override
  public void validateParams(DingDingBotNoticePluginParams plugin) {
    Assert.notNull(plugin.getAccessToken(), "accessToken不能为空");
  }

  @Override
  public Object doRun(DingDingBotNoticePluginParams plugin) {
    Long timestamp = System.currentTimeMillis();
    String url = buildUrl(timestamp, plugin);
    try {
      Map<String, Object> params = buildParams(plugin);
      Map<String, Object> result = HttpUtil.post(url, params, new ParameterizedTypeReference<Map<String, Object>>() {
      });
      if (!"0".equals(MapUtils.getString(result, "errcode"))) {
        throw new BssException("发送钉钉机器人通知失败: " + MapUtils.getString(result, "errmsg"));
      }
      return result;
    }
    catch (Exception e) {
      logger.error("发送钉钉机器人通知失败: message={}", e.getMessage(), e);
      throw new BssException("发送钉钉机器人通知失败: " + e.getMessage(), e);
    }
  }

  private String buildUrl(Long timestamp, DingDingBotNoticePluginParams dingDingBotNoticePluginParams) {
    try {
      String url = URL + "?access_token=" + dingDingBotNoticePluginParams.getAccessToken();
      String secret = dingDingBotNoticePluginParams.getSecret();
      if (StringUtils.isEmpty(secret)) {
        return url;
      }
      String stringToSign = timestamp + "\n" + secret;
      Mac mac = Mac.getInstance(ALGORITHM);
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
      byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
      String sign = URLEncoder.encode(Base64.encodeBase64String(signData), StandardCharsets.UTF_8);
      return url + "&timestamp=" + timestamp + "&sign=" + sign;
    }
    catch (Exception e) {
      logger.error("Failed to buildUrl, message={}", e.getMessage(), e);
      throw new BssException("发送钉钉钉机器人通知失败, 构建url异常: ", e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> buildParams(DingDingBotNoticePluginParams plugin) {
    Map<String, Object> params = JsonUtil.convert(plugin.getMessage(), Map.class);
    if (PluginConsts.MESSAGE_TYPE_ACTION_CARD.equals(plugin.getMessage().getMsgType())) {
      Map<String, Object> actionCard = JsonUtil.convert(MapUtils.getMap(params, PluginConsts.MESSAGE_TYPE_ACTION_CARD), Map.class);
      actionCard.put("singleURL", MapUtils.getString(actionCard, "singleUrl"));
      params.put(PluginConsts.MESSAGE_TYPE_ACTION_CARD, actionCard);
    }
    params.put("at", JsonUtil.convert(plugin.getAt(), Map.class));
    params.put(PluginConsts.MESSAGE_TYPE, plugin.getMessage().getMsgType());
    return params;
  }
}
