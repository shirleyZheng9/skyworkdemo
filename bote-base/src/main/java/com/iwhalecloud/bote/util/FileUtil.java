package com.iwhalecloud.bote.util;

import java.net.URI;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;

/**
 * 文件工具类
 *
 * @author qian.sisheng
 * @since 2025-11-26
 */
public final class FileUtil {
  private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

  private FileUtil() {

  }

  /**
   * 提取文件名
   */
  @Nullable
  public static String getFileName(ClientHttpResponse response, URI url) {
    // 优先从 Content-Disposition 响应头提取（更准确）
    String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
    if (StringUtils.isNotEmpty(contentDisposition)) {
      try {
        ContentDisposition disposition = ContentDisposition.parse(contentDisposition);
        return StringUtils.trimToNull(disposition.getFilename());
      }
      catch (Exception e) {
        // 文件名不太重要，忽略解析失败
        logger.warn("Failed to parse content-disposition: {}", contentDisposition);
      }
    }

    // 其次从 URL 中提取
    return StringUtils.trimToNull(FilenameUtils.getName(url.getPath()));
  }

  /**
   * 提取文件扩展名
   */
  @Nullable
  @SuppressWarnings("PMD.GuardLogStatement")
  public static String getFileExtension(@Nullable MediaType contentType, @Nullable String filename) {
    // 优先从 Content-Type 响应头提取（更准确）
    if (contentType != null && !contentType.equalsTypeAndSubtype(MediaType.APPLICATION_OCTET_STREAM)) {
      try {
        MimeType mimeType = MimeTypes.getDefaultMimeTypes().forName(contentType.getType() + "/" + contentType.getSubtype());
        return StringUtils.removeStart(mimeType.getExtension(), '.');
      }
      catch (MimeTypeException e) {
        logger.warn("No mime type found for name: {}", contentType.getType());
      }
    }

    // 其次从文件名提取
    if (StringUtils.isNotEmpty(filename)) {
      return StringUtils.trimToNull(FilenameUtils.getExtension(filename));
    }
    return null;
  }
}
