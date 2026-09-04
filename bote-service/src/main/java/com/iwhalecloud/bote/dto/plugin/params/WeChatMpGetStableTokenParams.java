package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微信公众号获取稳定TOKEN
 *
 * @author fan.cong
 * @since 2025-08-18
 */
@Getter
@Setter
@ToString
public class WeChatMpGetStableTokenParams extends AbstractPluginParams {
  // 微信公众号appid
  private String appId;

  // 微信公众号secret
  private String secret;

  // 是否强制刷新
  private boolean forceRefresh;

  public WeChatMpGetStableTokenParams() {
    super(PluginConsts.PLUGIN_CODE_WE_CHAT_MP_GET_STABLE_TOKEN);
  }

}
