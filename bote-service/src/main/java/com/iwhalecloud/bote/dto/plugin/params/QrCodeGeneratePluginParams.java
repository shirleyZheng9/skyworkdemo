package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 二维码生成插件参数
 *
 * @author lizuyin
 * @since 2025-11-25
 */
@Getter
@Setter
@ToString
public class QrCodeGeneratePluginParams extends AbstractPluginParams {
  /** 二维码内容 */
  private String content;
  /** 二维码宽度（像素），默认300 */
  private Integer width;
  /** 二维码高度（像素），默认300 */
  private Integer height;
  /** 图片格式，png, jpg, jpeg，默认png */
  private String format;
  /** 错误纠正级别，L, M, Q, H，默认M */
  private String errorCorrectionLevel;
  /** 边距（像素），默认4 */
  private Integer margin;
  /** Logo Base64编码（可选，data URI格式或纯Base64字符串） */
  private String logoBase64;
  /** Logo大小（像素），默认60 */
  private Integer logoSize;
  /** 前景色（十六进制，如#000000），默认#000000 */
  private String foregroundColor;
  /** 背景色（十六进制，如#FFFFFF），默认#FFFFFF */
  private String backgroundColor;

  public QrCodeGeneratePluginParams() {
    super(PluginConsts.PLUGIN_CODE_QR_CODE_GENERATE);
  }
}

