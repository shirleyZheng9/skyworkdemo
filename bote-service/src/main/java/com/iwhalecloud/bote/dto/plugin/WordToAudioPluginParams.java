package com.iwhalecloud.bote.dto.plugin;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文字转语音参数
 *
 * @author qian.sisheng
 * @since 2025-11-21
 */
@Getter
@Setter
@ToString
public class WordToAudioPluginParams extends AbstractPluginParams {
  /** 文本 */
  private String text;
  /** 音色名称 man, sweetGirl */
  private String audioName;
  /** 速率0.1~2.0 */
  private String speedFactor;

  public WordToAudioPluginParams() {
    super(PluginConsts.PLUGIN_CODE_WORD_TO_AUDIO);
  }
}
