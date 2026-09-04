package com.iwhalecloud.bote.dto.plugin.image;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 图片识别参数
 *
 * @author qian.sisheng
 * @since 2025-11-17
 */
@Getter
@Setter
@ToString
public class ImageIdentifyPluginParams extends AbstractPluginParams {
  /** 图片base64编码或文件ID */
  private String file;
  /** 图片类型: bote:文件ID，base64: base64编码 */
  private String type;

  public ImageIdentifyPluginParams() {
    super(PluginConsts.PLUGIN_CODE_IMAGE_IDENTIFY);
  }
}
