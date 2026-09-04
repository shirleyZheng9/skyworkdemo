package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * 图标处理工具
 *
 * @author wangtingyun
 * @since 2025-09-12
 */
public final class IconUtil {

  private IconUtil() {
  }

  /**
   * 发送base64图片数据到请求接口响应
   *
   * @param response 请求接口响应
   * @param icon base64图片数据
   * @throws IOException 异常
   */
  public static void sendBase64Icon(HttpServletResponse response, String icon) throws IOException {
    if (!icon.startsWith("data:image/") || !icon.contains(";base64,")) {
      throw new BssException("不合法图标: icon=" + icon);
    }
    String mediaType = icon.substring("data:".length(), icon.indexOf(";"));
    String base64 = icon.substring(icon.indexOf(";base64,") + ";base64,".length());
    byte[] content;
    try {
      content = Base64.getDecoder().decode(base64);
    }
    catch (IllegalArgumentException e) {
      throw new BssException("不合法图标: icon=" + icon, e);
    }
    response.setContentType(mediaType);
    response.setContentLength(content.length);
    response.getOutputStream().write(content);
  }

  /**
   * 发送路径图片资源数据到接口响应
   *
   * @param response 请求接口响应
   * @param resource 类路径图片资源
   * @throws IOException 异常
   */
  public static void sendPathResourceIcon(HttpServletResponse response, @Nullable ClassPathResource resource) throws IOException {
    if (resource == null || !resource.exists()) {
      throw new BssException("图片资源不存在");
    }

    // 设置 response 内容相关信息
    response.setContentType(getImageType(resource.getFilename()));
    response.setContentLengthLong(resource.contentLength());

    // 将图片数据输出到 response
    try (InputStream inputStream = resource.getInputStream()) {
      IOUtils.copy(inputStream, response.getOutputStream());
    }
  }

  /**
   * 根据图片名称获取文件类型
   *
   * @param imageName 图片名戳
   * @return 图片类型
   */
  private static String getImageType(@Nullable String imageName) {
    if (StringUtils.isEmpty(imageName)) {
      return "";
    }
    int indexOf = imageName.lastIndexOf(".");
    String suffix = indexOf == -1 ? "" : imageName.substring(indexOf + 1).toLowerCase();
    return switch (suffix) {
      case "png" -> MediaType.IMAGE_PNG_VALUE;
      case "jpeg" -> MediaType.IMAGE_JPEG_VALUE;
      case "gif" -> MediaType.IMAGE_GIF_VALUE;
      default -> "";
    };
  }

}
