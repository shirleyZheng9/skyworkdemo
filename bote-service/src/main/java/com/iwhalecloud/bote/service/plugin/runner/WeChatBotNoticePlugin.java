package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.WechatBotNoticePluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;


/**
 * 微信发送机器人消息
 *
 * @author qian.sisheng
 * @since 2025-04-14
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class WeChatBotNoticePlugin extends AbstractPlugin<WechatBotNoticePluginParams> {

  /** 发送消息的url */
  private static final String NOTICE_URL = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=";
  /** 微信机器人通知请求参数 */
  private static final ClassPathResource resource = new ClassPathResource("/plugin-params/weChatBotNotice.json");

  public WeChatBotNoticePlugin() {
    super(WechatBotNoticePluginParams.class);
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
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_WE_CHAT_BOT_NOTICE;
  }

  @Override
  public void validateParams(WechatBotNoticePluginParams params) {
    Assert.notNull(params.getMessage().getMsgType(), "msgType不能为空");
    Assert.notNull(params.getKey(), "key不能为空");
  }

  @Override
  public Object doRun(WechatBotNoticePluginParams plugin) {
    Map<String, Object> requestParams = buildParams(plugin.getMessage().getMsgType(), plugin);
    String url = NOTICE_URL + plugin.getKey();
    Map<String, Object> result = HttpUtil.post(url, requestParams, new ParameterizedTypeReference<Map<String, Object>>() {
    });
    if (MapUtils.isEmpty(result)) {
      throw new BssException("发送微信企业微信通知失败: result is empty");
    }
    String errorCode = MapUtils.getString(result, "errcode");
    if (!"0".equals(errorCode)) {
      throw new BssException("发送微信企业微信通知失败: errcode: " + errorCode + ", errmsg: " + MapUtils.getString(result, "errmsg"));
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> buildParams(String msgType, WechatBotNoticePluginParams plugin) {
    // 将消息对象转换为Map，自动处理驼峰转下划线
    Map<String, Object> requestParams = snakeCaseMapper.convertValue(plugin.getMessage(), Map.class);
    // 处理模板卡片消息类型的特殊情况
    if (PluginConsts.MESSAGE_TYPE_TEMPLATE_CARD.equals(msgType)) {
      msgType = camelToUnderScore(msgType);
    }
    requestParams.remove("msg_type");
    // 设置消息类型
    requestParams.put(PluginConsts.MESSAGE_TYPE, msgType);
    return requestParams;
  }
}
