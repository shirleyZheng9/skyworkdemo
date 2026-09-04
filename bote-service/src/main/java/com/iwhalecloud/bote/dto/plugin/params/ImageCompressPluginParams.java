package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 图片压缩参数
 *
 * @author zhao.xu104
 * @since 2025-11-20
 */
@Getter
@Setter
@ToString
public class ImageCompressPluginParams extends AbstractFileParams {
  /** 压缩质量（0.0-1.0，可选，默认0.8） */
  private Float quality;
  /** 最大宽度（可选，按比例缩放） */
  private Integer maxWidth;
  /** 最大高度（可选，按比例缩放） */
  private Integer maxHeight;

  public ImageCompressPluginParams() {
    super(PluginConsts.PLUGIN_CODE_IMAGE_COMPRESS);
  }
}

