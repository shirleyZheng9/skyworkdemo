package com.iwhalecloud.bote.common.util;

import lombok.Getter;
import org.apache.tika.Tika;

/**
 * 文件类型探测工具类
 *
 * @author bianjp
 * @since 2025-10-23
 */
public final class TikaUtil {
  private TikaUtil() {
  }

  /** Tika 实例 */
  @Getter
  private static final Tika tika = new Tika();

  /**
   * 根据文件名称探测媒体类型
   *
   * @param name 文件名称或 URL, 如果是 URL 会尝试获取文件内容
   * @return 媒体类型，探测不到时返回 application/octet-stream
   */
  public static String detect(String name) {
    return tika.detect(name);
  }

}
