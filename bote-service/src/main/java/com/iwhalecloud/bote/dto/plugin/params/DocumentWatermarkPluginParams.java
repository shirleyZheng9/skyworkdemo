package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档水印参数
 *
 * @author zhao.xu104
 * @since 2025-11-28
 */
@Getter
@Setter
@ToString
public class DocumentWatermarkPluginParams extends AbstractFileParams {
  /** 水印文本内容 */
  private String watermarkText;
  /** 水印图片文件ID（可选，如果提供则使用图片水印，否则使用文本水印） */
  private Long watermarkImageId;
  /** 水印位置（top-left/top-right/bottom-left/bottom-right/center/repeat） */
  private String position;
  /** 字体大小（可选，默认24，仅文本水印） */
  private Integer fontSize;
  /** 字体颜色（可选，默认#808080，仅文本水印，支持十六进制颜色码） */
  private String color;
  /** 透明度（0.0-1.0，可选，默认0.5） */
  private Float opacity;
  /** 旋转角度（可选，默认0，单位：度） */
  private Integer angle;

  public DocumentWatermarkPluginParams() {
    super(PluginConsts.PLUGIN_CODE_DOCUMENT_WATERMARK);
  }
}
