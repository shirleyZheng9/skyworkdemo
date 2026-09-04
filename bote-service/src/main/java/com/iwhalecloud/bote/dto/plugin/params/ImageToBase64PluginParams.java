package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 图片转base64
 *
 * @author qian.sisheng
 * @since 2025-06-11
 */

@Getter
@Setter
@ToString
public class ImageToBase64PluginParams extends AbstractPluginParams {
  /** 文件ID */
  private Long fileId;
  /** 文件地址 */
  private String fileUrl;

  public ImageToBase64PluginParams() {
    super(PluginConsts.PLUGIN_CODE_IMAGE_TO_BASE_64);
  }

}
