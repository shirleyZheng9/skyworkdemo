package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import com.iwhalecloud.bote.dto.plugin.message.wechat.AbstractWeChatMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微信机器人通知插件
 *
 * @author qian.sisheng
 * @since 2025-04-14
 */
@Getter
@Setter
@ToString
public class WechatBotNoticePluginParams extends AbstractPluginParams {
  /** 密钥 */
  private String secret;
  /** 企业 ID */
  private String corpId;
  /** webhook地址 */
  private String key;
  /** 消息内容 */
  private AbstractWeChatMessage message;

  public WechatBotNoticePluginParams() {
    super(PluginConsts.PLUGIN_CODE_WE_CHAT_BOT_NOTICE);
  }
}
