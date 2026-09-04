package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 图片识别参数
 *
 * @author zhangJun
 * @since 2025-07-18
 */

@Getter
@Setter
@ToString
public class ImageAnalysisPluginParams extends AbstractPluginParams {
  /** 提示词 */
  private String prompt;
  /** 文件ID */
  private Long fileId;

  public ImageAnalysisPluginParams() {
    super(PluginConsts.PLUGIN_CODE_IMAGE_ANALYSIS);
  }
}
