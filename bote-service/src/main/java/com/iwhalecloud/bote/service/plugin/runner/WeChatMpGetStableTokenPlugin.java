package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.WeChatMpApiUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.WeChatMpGetStableTokenParams;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 微信公众号获取稳定TOKEN
 *
 * @author fan.cong
 * @since 2025-08-18
 */
@Component
public class WeChatMpGetStableTokenPlugin extends AbstractPlugin<WeChatMpGetStableTokenParams> {

  public WeChatMpGetStableTokenPlugin() {
    super(WeChatMpGetStableTokenParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_WE_CHAT_MP_GET_STABLE_TOKEN;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(
      Arrays.asList(ParameterSpec.newProperty("appId", "微信公众号appid", AttrDataType.STRING),
        ParameterSpec.newProperty("secret", "微信公众号secret", AttrDataType.STRING),
        ParameterSpec.newProperty("forceRefresh", "是否强制刷新", AttrDataType.BOOLEAN)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("message", "执行信息", AttrDataType.STRING),
      ParameterSpec.newProperty("accessToken", "公众号accessToken", AttrDataType.STRING),
      ParameterSpec.newProperty("expiresIn", "公众号accessToken剩余时间", AttrDataType.INTEGER)));
  }

  @Override
  public void validateParams(WeChatMpGetStableTokenParams params) {
    Assert.notNull(params.getAppId(), "微信公众号appid不能为空");
    Assert.notNull(params.getSecret(), "微信公众号secret不能为空");
  }

  @Override
  public Object doRun(WeChatMpGetStableTokenParams pluginParams) {
    return WeChatMpApiUtil.getStableToken(pluginParams.getAppId(), pluginParams.getSecret(),
      pluginParams.isForceRefresh());
  }
}