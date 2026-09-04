package com.iwhalecloud.bote.service.external;

import java.io.InputStream;

/**
 * 聚智 Ocr能力客户端
 *
 * @author wangtingyun
 * @since 2025-06-25
 */
public interface JuzhiOcrClient {

  /**
   * 解析文档内容
   *
   * @param fileStream 文件流
   * @param fileName 文件名
   * @return 文档内容
   */
  Object parseFile(InputStream fileStream, String fileName);

  /**
   * 解析图片内容
   *
   * @param fileStream 文件流
   * @param fileName 文件名
   * @return 图片内容
   */
  Object parseImage(InputStream fileStream, String fileName);

  /**
   * 解析语音内容
   *
   * @param fileStream 文件流
   * @param fileName 文件名
   * @return 语音内容
   */
  String parseVoice(InputStream fileStream, String fileName);

}
