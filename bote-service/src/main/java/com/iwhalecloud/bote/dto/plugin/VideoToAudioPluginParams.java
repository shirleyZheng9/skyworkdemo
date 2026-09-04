package com.iwhalecloud.bote.dto.plugin;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 视频转音频插件参数
 */
@Getter
@Setter
@ToString
public class VideoToAudioPluginParams extends AbstractPluginParams {

  /** 文件ID */
  private Long fileId;
  /** 转出来的音频文件格式 */
  private String audioFormat;

  public VideoToAudioPluginParams() {
    super(PluginConsts.PLUGIN_CODE_VIDEO_TO_AUDIO);
  }
}
