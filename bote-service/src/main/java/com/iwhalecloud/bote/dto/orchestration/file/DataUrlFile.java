package com.iwhalecloud.bote.dto.orchestration.file;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * data URL 文件
 *
 * @author bianjp
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/URI/Reference/Schemes/data">data: URLs</a>
 * @since 2025-10-23
 */
@Getter
public final class DataUrlFile extends AbstractFile {
  /** data URL, 格式为 data:MEDIA_TYPE;base64,DATA */
  private final String url;

  public DataUrlFile(String url) {
    this.url = url;
  }

  @Override
  @Nullable
  public String getFilename() {
    return null;
  }

  @Override
  @Nullable
  public String getFileType() {
    return null;
  }

  @Override
  @Nullable
  public String getMimeType() {
    String mimeType = StringUtils.substringBetween(url, "data:", ";base64,");
    return StringUtils.isNotEmpty(mimeType) ? mimeType : null;
  }

  /**
   * 获取文件内容(base64 形式)
   *
   * @return 文件内容(base64 形式)
   */
  public String getFileContent() {
    int pos = url.indexOf(";base64,");
    Assert.isTrue(pos > 0, "data URL 不合法: " + url);
    return url.substring(pos + ";base64,".length());
  }
}
