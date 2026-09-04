package com.iwhalecloud.bote.doc.common.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

/**
 * Content-Type工具类
 *
 * <p>提供文件扩展名到MIME类型的映射，支持更多文件格式的自动识别。</p>
 *
 * @author Aiqing
 * @since 2025/09/09
 */
public final class ContentTypeUtil {
  private ContentTypeUtil() {
  }

  /**
   * 扩展名到MIME类型的映射表
   * 主要处理Spring MimeTypeUtils无法识别的特殊格式
   */
  private static final Map<String, String> EXTENSION_TO_MIME_TYPE = new HashMap<>();

  static {
    // Office文档
    EXTENSION_TO_MIME_TYPE.put("doc", "application/msword");
    EXTENSION_TO_MIME_TYPE.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    EXTENSION_TO_MIME_TYPE.put("xls", "application/vnd.ms-excel");
    EXTENSION_TO_MIME_TYPE.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    EXTENSION_TO_MIME_TYPE.put("ppt", "application/vnd.ms-powerpoint");
    EXTENSION_TO_MIME_TYPE.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");

    // 压缩文件
    EXTENSION_TO_MIME_TYPE.put("rar", "application/x-rar-compressed");
    EXTENSION_TO_MIME_TYPE.put("7z", "application/x-7z-compressed");
    EXTENSION_TO_MIME_TYPE.put("tar", "application/x-tar");
    EXTENSION_TO_MIME_TYPE.put("gz", "application/gzip");
    EXTENSION_TO_MIME_TYPE.put("bz2", "application/x-bzip2");
    EXTENSION_TO_MIME_TYPE.put("xz", "application/x-xz");

    // 音频文件
    EXTENSION_TO_MIME_TYPE.put("mp3", "audio/mpeg");
    EXTENSION_TO_MIME_TYPE.put("wav", "audio/wav");
    EXTENSION_TO_MIME_TYPE.put("flac", "audio/flac");
    EXTENSION_TO_MIME_TYPE.put("aac", "audio/aac");
    EXTENSION_TO_MIME_TYPE.put("ogg", "audio/ogg");
    EXTENSION_TO_MIME_TYPE.put("wma", "audio/x-ms-wma");

    // 视频文件
    EXTENSION_TO_MIME_TYPE.put("mp4", "video/mp4");
    EXTENSION_TO_MIME_TYPE.put("avi", "video/x-msvideo");
    EXTENSION_TO_MIME_TYPE.put("mov", "video/quicktime");
    EXTENSION_TO_MIME_TYPE.put("wmv", "video/x-ms-wmv");
    EXTENSION_TO_MIME_TYPE.put("flv", "video/x-flv");
    EXTENSION_TO_MIME_TYPE.put("mkv", "video/x-matroska");
    EXTENSION_TO_MIME_TYPE.put("webm", "video/webm");
    EXTENSION_TO_MIME_TYPE.put("3gp", "video/3gpp");

    // 图片文件（补充一些MimeTypeUtils可能不支持的格式）
    EXTENSION_TO_MIME_TYPE.put("bmp", "image/bmp");
    EXTENSION_TO_MIME_TYPE.put("tiff", "image/tiff");
    EXTENSION_TO_MIME_TYPE.put("tif", "image/tiff");
    EXTENSION_TO_MIME_TYPE.put("ico", "image/x-icon");
    EXTENSION_TO_MIME_TYPE.put("svg", "image/svg+xml");
    EXTENSION_TO_MIME_TYPE.put("webp", "image/webp");

    // 文本和代码文件
    EXTENSION_TO_MIME_TYPE.put("json", "application/json");
    EXTENSION_TO_MIME_TYPE.put("xml", "application/xml");
    EXTENSION_TO_MIME_TYPE.put("csv", "text/csv");
    EXTENSION_TO_MIME_TYPE.put("md", "text/markdown");
    EXTENSION_TO_MIME_TYPE.put("rtf", "application/rtf");
    EXTENSION_TO_MIME_TYPE.put("html", "text/html");
    EXTENSION_TO_MIME_TYPE.put("htm", "text/html");
    EXTENSION_TO_MIME_TYPE.put("css", "text/css");
    EXTENSION_TO_MIME_TYPE.put("js", "application/javascript");
    EXTENSION_TO_MIME_TYPE.put("php", "application/x-httpd-php");
    EXTENSION_TO_MIME_TYPE.put("py", "text/x-python");
    EXTENSION_TO_MIME_TYPE.put("java", "text/x-java-source");
    EXTENSION_TO_MIME_TYPE.put("cpp", "text/x-c");
    EXTENSION_TO_MIME_TYPE.put("c", "text/x-c");
    EXTENSION_TO_MIME_TYPE.put("h", "text/x-c");

    // 其他常见格式
    EXTENSION_TO_MIME_TYPE.put("exe", "application/x-msdownload");
    EXTENSION_TO_MIME_TYPE.put("msi", "application/x-msdownload");
    EXTENSION_TO_MIME_TYPE.put("deb", "application/x-debian-package");
    EXTENSION_TO_MIME_TYPE.put("rpm", "application/x-redhat-package-manager");
    EXTENSION_TO_MIME_TYPE.put("apk", "application/vnd.android.package-archive");
    EXTENSION_TO_MIME_TYPE.put("ipa", "application/octet-stream");
  }

  /**
   * 根据文件名获取Content-Type
   *
   * @param fileName 文件名
   * @return MIME类型字符串
   */
  public static String getContentType(String fileName) {
    if (StringUtils.isBlank(fileName)) {
      return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    String extension = FilenameUtils.getExtension(fileName);
    if (StringUtils.isBlank(extension)) {
      return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    return getContentTypeByExtension(extension.toLowerCase());
  }

  /**
   * 根据文件扩展名获取Content-Type
   *
   * @param extension 文件扩展名（不包含点号）
   * @return MIME类型字符串
   */
  public static String getContentTypeByExtension(String extension) {
    if (StringUtils.isBlank(extension)) {
      return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    extension = extension.toLowerCase();

    // 首先尝试使用Spring的MimeTypeUtils
    try {
      MimeType mimeType = MimeTypeUtils.parseMimeType("." + extension);
      if (!MediaType.APPLICATION_OCTET_STREAM_VALUE.equals(mimeType.toString())) {
        return mimeType.toString();
      }
    }
    catch (Exception e) {
      // 如果解析失败，继续使用备用方案
    }

    // 使用自定义映射表
    String mimeType = EXTENSION_TO_MIME_TYPE.get(extension);
    if (mimeType != null) {
      return mimeType;
    }

    // 默认返回二进制流类型
    return MediaType.APPLICATION_OCTET_STREAM_VALUE;
  }

  /**
   * 判断是否为图片文件
   *
   * @param fileName 文件名
   * @return 是否为图片文件
   */
  public static boolean isImageFile(String fileName) {
    String contentType = getContentType(fileName);
    return contentType.startsWith("image/");
  }

  /**
   * 判断是否为视频文件
   *
   * @param fileName 文件名
   * @return 是否为视频文件
   */
  public static boolean isVideoFile(String fileName) {
    String contentType = getContentType(fileName);
    return contentType.startsWith("video/");
  }

  /**
   * 判断是否为音频文件
   *
   * @param fileName 文件名
   * @return 是否为音频文件
   */
  public static boolean isAudioFile(String fileName) {
    String contentType = getContentType(fileName);
    return contentType.startsWith("audio/");
  }

  /**
   * 判断是否为文本文件
   *
   * @param fileName 文件名
   * @return 是否为文本文件
   */
  public static boolean isTextFile(String fileName) {
    String contentType = getContentType(fileName);
    return contentType.startsWith("text/") ||
      "application/json".equals(contentType) ||
      "application/xml".equals(contentType);
  }

  /**
   * 判断是否为Office文档
   *
   * @param fileName 文件名
   * @return 是否为Office文档
   */
  public static boolean isOfficeDocument(String fileName) {
    String extension = FilenameUtils.getExtension(fileName).toLowerCase();
    return "doc".equals(extension) || "docx".equals(extension) ||
      "xls".equals(extension) || "xlsx".equals(extension) ||
      "ppt".equals(extension) || "pptx".equals(extension);
  }

  /**
   * 获取所有支持的扩展名
   *
   * @return 支持的扩展名集合
   */
  public static Set<String> getSupportedExtensions() {
    return EXTENSION_TO_MIME_TYPE.keySet();
  }
}
