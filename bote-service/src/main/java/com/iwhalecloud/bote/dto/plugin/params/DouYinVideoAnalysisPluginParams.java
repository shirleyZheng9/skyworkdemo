package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 抖音视频分析参数
 *
 * @author qian.sisheng
 * @since 2025-08-15
 */
@Getter
@Setter
@ToString
public class DouYinVideoAnalysisPluginParams extends AbstractPluginParams {
  /** 视频地址 */
  private String url;
  /** 分析模式 1：音频解析，2：音频解析+截图, 默认音频解析 */
  private String analysisMode;

  public DouYinVideoAnalysisPluginParams() {
    super(PluginConsts.PLUGIN_CODE_DOU_YIN_VIDEO_ANALYSIS);
  }
}
