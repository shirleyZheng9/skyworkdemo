package com.iwhalecloud.bote.service.external;

import java.io.File;

/**
 * 磐智orc能力
 *
 * @author qian.sisheng
 * @since 2025-09-13
 */
public interface PanzhiClient {
  /**
   * 解析语音内容
   *
   * @param file 文件
   * @return 语音内容
   */
  String parseVoice(File file);
}
