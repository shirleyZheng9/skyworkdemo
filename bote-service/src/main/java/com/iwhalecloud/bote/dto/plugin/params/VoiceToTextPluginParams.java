package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 语音转文字参数
 *
 * @author qian.sisheng
 * @since 2025-06-11
 */
@Getter
@Setter
@ToString
public class VoiceToTextPluginParams extends AbstractPluginParams {
  /** 文件 ID */
  private Long fileId;

  public VoiceToTextPluginParams() {
    super(PluginConsts.PLUGIN_CODE_VOICE_TO_TEXT);
  }
}
