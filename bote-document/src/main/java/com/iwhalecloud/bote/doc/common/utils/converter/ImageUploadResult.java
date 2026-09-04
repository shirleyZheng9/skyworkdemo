package com.iwhalecloud.bote.doc.common.utils.converter;

import java.io.File;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 图片上传结果
 */
@Getter
@Setter
@ToString
public final class ImageUploadResult {
  private String imageUrl;
  private File tempFile;

  /**
   * 构造函数
   */
  public ImageUploadResult(String imageUrl, File tempFile) {
    this.imageUrl = imageUrl;
    this.tempFile = tempFile;
  }
}

