package com.iwhalecloud.bote.doc.consts;

import java.util.Arrays;
import lombok.Getter;

/**
 * 文档在线预览的转换状态
 *
 * @author Aiqing
 * @since 2025/9/8
 */
@Getter
public enum FilePreviewConvertStatus {

  /**
   * 转换中
   */
  converting("converting"),
  /**
   * 成功
   */
  success("success"),
  /**
   * 失败
   */
  FAILED("failed");

  private final String code;

  FilePreviewConvertStatus(String code) {
    this.code = code;
  }

  public static FilePreviewConvertStatus getByCode(String code) {
    return Arrays.stream(values())
      .filter(status -> status.getCode().equals(code))
      .findFirst()
      .orElse(null);
  }
}
