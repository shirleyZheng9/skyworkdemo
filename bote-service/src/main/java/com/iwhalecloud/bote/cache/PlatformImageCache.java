package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.HttpCacheUtil;
import com.iwhalecloud.bote.dto.base.SimpleDcParamDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 平台图片缓存
 *
 * <p>直接使用 DcParamCache 缓存，不增加新的缓存，不然刷新缓存时需要同步刷新，不太好实现。</p>
 *
 * @author bianjp
 * @since 2025-06-24
 */
@Component
@RequiredArgsConstructor
public class PlatformImageCache {
  private static final Logger logger = LoggerFactory.getLogger(PlatformImageCache.class);

  private final DcParamCache dcParamCache;

  /**
   * 发送图片，支持 HTTP 缓存
   *
   * @param request 请求
   * @param response 响应
   * @param systemParameter 系统参数
   * @param imageName 图片名称，用于构造错误信息
   * @param defaultImage 默认图片，可选，未配置且没有默认图片时返回 204
   */
  public void sendImage(HttpServletRequest request, HttpServletResponse response, SystemParameter systemParameter, String imageName,
                        @Nullable Resource defaultImage) throws IOException {
    PlatformImageInfo info;
    try {
      SimpleDcParamDTO param = dcParamCache.get(systemParameter.getCode());
      Assert.notNull(param, "缺少平台配置参数: " + systemParameter.getCode());
      info = param.getParsedValue(value -> parseImage(value, imageName, defaultImage));
    }
    catch (Exception e) {
      logger.warn("Failed to get platform image", e);
      HttpCacheUtil.sendError(response, e);
      return;
    }
    // 未配置且没有默认图片时返回 204
    if (info == null) {
      response.setStatus(HttpStatus.NO_CONTENT.value());
      return;
    }

    // 发送缓存响应头
    if (HttpCacheUtil.sendCacheHeader(request, response, info.getDigest())) {
      return;
    }

    response.setContentType(info.getMediaType());
    response.setContentLengthLong(info.getLength());
    byte[] content = info.getContent();
    Resource resource = info.getResource();
    if (content != null) {
      response.getOutputStream().write(content);
    }
    else if (resource != null) {
      try (InputStream inputStream = resource.getInputStream()) {
        IOUtils.copy(inputStream, response.getOutputStream());
      }
    }
    else {
      throw new IllegalStateException("图片信息错误");
    }
  }

  /**
   * 解析以 base64 形式存储的图片
   */
  @SuppressFBWarnings("WEAK_MESSAGE_DIGEST_MD5")
  @Nullable
  private PlatformImageInfo parseImage(@Nullable String value, String imageName, @Nullable Resource defaultImage) {
    if (StringUtils.isBlank(value)) {
      return defaultImage != null ? buildDefaultImage(defaultImage) : null;
    }
    if (!value.startsWith("data:image/") || !value.contains(";base64,")) {
      throw new BssException(imageName + "不合法");
    }
    String mediaType = value.substring("data:".length(), value.indexOf(";"));
    String base64 = value.substring(value.indexOf(";base64,") + ";base64,".length());
    byte[] content;
    try {
      content = Base64.getDecoder().decode(base64);
    }
    catch (IllegalArgumentException e) {
      throw new BssException(imageName + "不合法", e);
    }
    PlatformImageInfo info = new PlatformImageInfo();
    info.setDigest(DigestUtils.md5Hex(content));
    info.setMediaType(mediaType);
    info.setLength(content.length);
    info.setContent(content);
    return info;
  }

  /**
   * 构造默认图片信息
   */
  @SuppressFBWarnings("WEAK_MESSAGE_DIGEST_MD5")
  private PlatformImageInfo buildDefaultImage(Resource resource) {
    String filename = resource.getFilename();
    Assert.notNull(filename, "获取资源文件的文件名失败");
    // 媒体类型，只会有 png, jpg 两种
    String mediaType;
    if (filename.endsWith(".png")) {
      mediaType = MediaType.IMAGE_PNG_VALUE;
    }
    else if (filename.endsWith(".jpg")) {
      mediaType = MediaType.IMAGE_JPEG_VALUE;
    }
    else {
      throw new BssException("不支持的资源文件类型: " + filename);
    }

    try (InputStream inputStream = resource.getInputStream()) {
      byte[] content = IOUtils.toByteArray(inputStream);
      String digest = DigestUtils.md5Hex(content);
      PlatformImageInfo info = new PlatformImageInfo();
      info.setDigest(digest);
      info.setMediaType(mediaType);
      // 部署环境资源文件不会变化，可以存储长度，避免重复读取；本地开发替换资源文件时会变化，重启下就行了
      info.setLength(content.length);
      // 只存储资源实例，不存储文件内容，以减少内存占用
      info.setResource(resource);
      return info;
    }
    catch (Exception e) {
      throw new BssException("读取图片失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 图片信息
   */
  @Getter
  @Setter
  @ToString
  private static final class PlatformImageInfo {
    /** 摘要 */
    private String digest;
    /** 媒体类型 */
    private String mediaType;
    /** 内容长度 */
    private long length;
    /** 内容 */
    @Nullable
    private byte[] content;
    /** 资源文件 */
    @Nullable
    private Resource resource;
  }
}
