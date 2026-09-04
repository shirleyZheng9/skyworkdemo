package com.iwhalecloud.bote.service.external;

import java.io.InputStream;

/**
 * 聚智平台 dict 团队专用的能力客户端
 *
 * @author wangtingyun
 * @since 2025-07-16
 */
public interface JuzhiDictClient {

  /**
   * 解析文档内容: 给 dict 团队用的
   *
   * @param fileStream 文件流
   * @param fileName 文件名
   * @return 文档内容
   */
  String parseFileContent(InputStream fileStream, String fileName);

}
