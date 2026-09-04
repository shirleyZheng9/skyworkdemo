package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 图片水印参数
 *
 * @author zhao.xu104
 * @since 2025-11-20
 */
@Getter
@Setter
@ToString
public class ImageWatermarkPluginParams extends AbstractFileParams {
  /** 水印文本内容 */
  private String watermarkText;
  /** 水印位置（top-left/top-right/bottom-left/bottom-right/center） */
  private String position;
  /** 字体大小（可选，默认24） */
  private Integer fontSize;
  /** 字体颜色（可选，默认#808080，支持十六进制颜色码） */
  private String color;
  /** 透明度（0.0-1.0，可选，默认0.5） */
  private Float opacity;
  /** 旋转角度（可选，默认0，单位：度） */
  private Integer angle;
  /** 重复水印间距倍数（可选，默认1.5，表示水印间距为文本宽度的倍数，仅当position="repeat"时有效） */
  private Float spacing;

  public ImageWatermarkPluginParams() {
    super(PluginConsts.PLUGIN_CODE_IMAGE_WATERMARK);
  }
}

