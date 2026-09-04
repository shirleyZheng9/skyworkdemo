package com.iwhalecloud.bote.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

/**
 * 文件预览配置
 * 配置kkfileview等文件预览服务的相关参数
 *
 * @author Aiqing
 * @since 2025-09-06
 */
@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "bote.dc.file.preview")
public class FilePreviewConfig {

  /**
   * 是否启用文件预览功能
   */
  private Boolean enabled = true;

  /**
   * kkfileview服务基础地址
   */
  private String serverHost;

  /**
   * kkfileview服务配置
   */
  private String kkFileViewUrl;

  /**
   * 文档转换的状态地址
   */
  private String convertStatusUrl;

  /**
   * 文件下载的url前缀
   */
  private String fileDownloadUrl;

  /**
   * 文件大小限制（字节）, 默认500MB
   */
  private long maxFileSize = 500 * 1024 * 1024;

  /**
   * 分片文件大小限制（字节）, 默认5MB
   */
  private long maxChunkFileSize = 5 * 1024 * 1024;

  /**
   * 预览的最大文件大小
   */
  private Long previewMaxFileSize = 100 * 1024 * 1024L;

  /**
   * 是否启用预览
   */
  private Boolean enableWatermark;

  /**
   * 检查文件大小是否在限制范围内
   *
   * @param fileSize 文件大小（字节）
   * @return 是否在限制范围内
   */
  public boolean isFileSizeValid(long fileSize) {
    return fileSize >= 0 && fileSize <= this.maxFileSize;
  }

  /**
   * 检查预览文件大小是否在限制范围内
   *
   * @param fileSize 文件大小
   * @return 是否在限制范围内
   */
  public boolean isPreviewFileSizeValid(long fileSize) {
    Assert.notNull(this.previewMaxFileSize, "预览参数配置不正确");
    return fileSize >= 0 && fileSize <= this.previewMaxFileSize;
  }

  /**
   * 检查文件大小是否在限制范围内
   *
   * @param fileSize 文件大小（字节）
   * @return 是否在限制范围内
   */
  public boolean isChunkFileSizeValid(long fileSize) {
    return fileSize >= 0 && fileSize <= maxChunkFileSize;
  }
}
