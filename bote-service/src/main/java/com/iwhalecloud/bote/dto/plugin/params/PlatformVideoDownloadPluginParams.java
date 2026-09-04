package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 多平台视频下载参数
 *
 * @author auto
 * @since 2025-10-21
 */
@Getter
@Setter
@ToString
public class PlatformVideoDownloadPluginParams extends AbstractPluginParams {
  /**
   * 视频地址（支持抖音、小红书、Bilibili、快手、西瓜视频等）
   */
  private String url;

  public PlatformVideoDownloadPluginParams() {
    super(PluginConsts.PLUGIN_CODE_PLATFORM_VIDEO_DOWNLOAD);
  }
}

