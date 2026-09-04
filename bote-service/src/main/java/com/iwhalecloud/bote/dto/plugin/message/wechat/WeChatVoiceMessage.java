package com.iwhalecloud.bote.dto.plugin.message.wechat;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 微信语音消息
 *
 * @author qian.sisheng
 * @since 2025-04-14
 */
@Getter
@Setter
@ToString
public class WeChatVoiceMessage extends AbstractWeChatMessage {
  /** 语音 */
  private Voice voice;

  public WeChatVoiceMessage() {
    super(PluginConsts.MESSAGE_TYPE_VOICE);
  }

  @Getter
  @Setter
  @ToString
  public static class Voice {
    /** 语音文件id，通过下文的文件上传接口获取 */
    private String mediaId;
  }
}
