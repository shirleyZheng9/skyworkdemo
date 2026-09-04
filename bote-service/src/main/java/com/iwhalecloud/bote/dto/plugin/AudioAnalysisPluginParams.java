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
public class AudioAnalysisPluginParams extends AbstractPluginParams {

  /** 文件ID */
  private Long fileId;

  public AudioAnalysisPluginParams() {
    super(PluginConsts.PLUGIN_CODE_VIDEO_ANALYSIS);
  }
}
