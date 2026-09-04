package com.iwhalecloud.bote.dto.orchestration.file;

import com.iwhalecloud.bote.common.util.TikaUtil;
import java.net.URL;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import org.springframework.lang.Nullable;

/**
 * 链接表示的文件
 *
 * @author bianjp
 * @since 2025-10-23
 */
@Getter
public final class UrlFile extends AbstractFile {
  /** 链接 */
  private final URL url;

  public UrlFile(URL url) {
    this.url = url;
  }

  @Override
  @Nullable
  public String getFilename() {
    return FilenameUtils.getName(url.getPath());
  }

  @Override
  @Nullable
  public String getFileType() {
    return FilenameUtils.getExtension(url.getPath());
  }

  @Override
  public String getMimeType() {
    return TikaUtil.detect(FilenameUtils.getName(url.getPath()));
  }
}
