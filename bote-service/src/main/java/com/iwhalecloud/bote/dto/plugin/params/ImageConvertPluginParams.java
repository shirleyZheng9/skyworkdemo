package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 图片格式转换参数
 *
 * @author zhao.xu104
 * @since 2025-11-20
 */
@Getter
@Setter
@ToString
public class ImageConvertPluginParams extends AbstractFileParams {
  /** 目标格式（png, jpg, jpeg, bmp, gif） */
  private String targetFormat;

  public ImageConvertPluginParams() {
    super(PluginConsts.PLUGIN_CODE_IMAGE_CONVERT);
  }
}

