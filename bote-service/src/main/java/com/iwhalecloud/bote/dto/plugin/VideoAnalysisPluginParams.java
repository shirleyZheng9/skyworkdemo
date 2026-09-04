package com.iwhalecloud.bote.dto.plugin;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 视频解析插件参数
 */
@Getter
@Setter
@ToString
public class VideoAnalysisPluginParams extends AbstractPluginParams {
  /** 解析模式 1：音频解析，2：音频解析+截图 */
  private String analysisMode;
  /** 文件ID */
  private Long fileId;

  public VideoAnalysisPluginParams() {
    super(PluginConsts.PLUGIN_CODE_AUDIO_ANALYSIS);
  }
}
