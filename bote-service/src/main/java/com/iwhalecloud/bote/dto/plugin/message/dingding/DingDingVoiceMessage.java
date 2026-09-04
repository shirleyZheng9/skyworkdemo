package com.iwhalecloud.bote.dto.plugin.message.dingding;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 音频消息
 *
 * @author qian.sisheng
 * @since 2025-04-16
 */
@Getter
@Setter
@ToString
public class DingDingVoiceMessage extends AbstractDingDingMessage {
  /** 语音消息 */
  private Voice voice;

  public DingDingVoiceMessage() {
    super(PluginConsts.MESSAGE_TYPE_VOICE);
  }

  @Getter
  @Setter
  @ToString
  public static class Voice {
    /** 语音长度 */
    private String duration;
    /** 语音文件id */
    private String mediaId;
  }
}
