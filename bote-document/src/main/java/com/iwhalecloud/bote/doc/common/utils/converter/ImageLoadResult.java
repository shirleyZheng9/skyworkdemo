package com.iwhalecloud.bote.doc.common.utils.converter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 图片加载结果
 */
@Getter
@Setter
@ToString
public final class ImageLoadResult {
  private Boolean success;
  private byte[] imageBytes;
  private Integer pictureType;

  /**
   * 构造函数
   */
  public ImageLoadResult(Boolean success, byte[] imageBytes, Integer pictureType) {
    this.success = success;
    this.imageBytes = imageBytes;
    this.pictureType = pictureType;
  }
}
