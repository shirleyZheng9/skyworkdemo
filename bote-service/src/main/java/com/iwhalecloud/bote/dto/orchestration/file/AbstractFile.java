package com.iwhalecloud.bote.dto.orchestration.file;

import org.springframework.lang.Nullable;

/**
 * 文件抽象类
 *
 * @author bianjp
 * @since 2025-10-23
 */
public abstract sealed class AbstractFile permits UrlFile, DataUrlFile, FileServerFile {
  /**
   * 获取文件名称
   *
   * @return 文件名称
   */
  @Nullable
  public abstract String getFilename();

  /**
   * 文件类型（扩展名）
   *
   * @return 文件类型
   */
  @Nullable
  public abstract String getFileType();

  /**
   * 获取媒体类型
   *
   * @return 媒体类型
   */
  @Nullable
  public abstract String getMimeType();
}
