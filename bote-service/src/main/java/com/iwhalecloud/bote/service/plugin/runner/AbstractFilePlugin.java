package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.plugin.params.AbstractFileParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreProcessor;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.util.FileStoreUtils;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * 文件操作插件抽象类
 * 提供通用的文件下载、上传、文件名获取等操作
 *
 * @author zhao.xu104
 * @since 2025-11-28
 */
@SuppressWarnings("PMD.GuardLogStatement")
public abstract class AbstractFilePlugin<T extends AbstractFileParams> extends AbstractPlugin<T> {
  protected static final Logger logger = LoggerFactory.getLogger(AbstractFilePlugin.class);
  protected final IFileStoreService fileStoreService;

  public AbstractFilePlugin(Class<T> clazz, IFileStoreService fileStoreService) {
    super(clazz);
    this.fileStoreService = fileStoreService;
  }

  /**
   * 下载文件数据
   * 支持从 fileId 或 fileUrl 下载文件
   *
   * @param params 插件参数
   * @return 文件字节数组
   */
  protected byte[] downloadFile(T params) {
    byte[] fileBytes = null;
    if (params.getFileId() != null) {
      fileBytes = fileStoreService.downloadFile(params.getFileId());
      if (fileBytes == null || fileBytes.length == 0) {
        throw new BssException("文件不存在或文件内容为空, fileId=" + params.getFileId());
      }
    }
    else if (StringUtils.isNotEmpty(params.getFileUrl())) {
      String fileUrl = params.getFileUrl();
      if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
        fileBytes = downloadFromHttpUrl(fileUrl);
      }
      else {
        // 内部存储的文件，使用FileStoreProcessor
        IFileStoreProcessor fileStoreProcessor = FileStoreUtils.getDefaultProcessor();
        try {
          fileBytes = fileStoreProcessor.download(fileUrl);
        }
        catch (Exception e) {
          throw new BssException("下载文件失败, fileUrl=" + fileUrl + ", error=" + e.getMessage(), e);
        }
      }
      if (fileBytes == null || fileBytes.length == 0) {
        throw new BssException("文件不存在或文件内容为空, fileUrl=" + fileUrl);
      }
    }
    return fileBytes;
  }

  /**
   * 从HTTP URL下载文件
   *
   * @param url HTTP/HTTPS URL
   * @return 文件字节数组
   */
  protected byte[] downloadFromHttpUrl(String url) {
    try {
      URI uri = URI.create(url);
      return HttpUtil.getRestTemplate().execute(uri, HttpMethod.GET, null, response -> {
        try (InputStream inputStream = response.getBody()) {
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          byte[] buffer = new byte[8192];
          int bytesRead;
          while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
          }
          return outputStream.toByteArray();
        }
      });
    }
    catch (HttpStatusCodeException e) {
      int status = e.getStatusCode().value();
      String body = e.getResponseBodyAsString();
      logger.warn("Failed to download file from HTTP URL: url={}, status={}, body={}", url, status, body);
      throw new BssException("文件下载失败，HTTP 状态码: " + status + "，响应内容: " + body, e);
    }
    catch (BssException e) {
      throw e;
    }
    catch (Exception e) {
      logger.warn("Failed to download file from HTTP URL: url={}", url, e);
      throw new BssException("文件下载失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 获取原始文件名
   *
   * @param params 插件参数
   * @param defaultFileName 默认文件名（当无法获取文件名时使用）
   * @return 文件名
   */
  protected String getOriginalFileName(T params, String defaultFileName) {
    if (params.getFileId() != null) {
      FileInfoVO fileInfo = fileStoreService.getFileInfoById(params.getFileId());
      if (fileInfo != null && StringUtils.isNotBlank(fileInfo.getFileName())) {
        return fileInfo.getFileName();
      }
    }
    if (StringUtils.isNotEmpty(params.getFileUrl())) {
      String url = params.getFileUrl();
      int lastSlash = url.lastIndexOf('/');
      if (lastSlash >= 0 && lastSlash < url.length() - 1) {
        String fileName = url.substring(lastSlash + 1);
        int questionMark = fileName.indexOf('?');
        if (questionMark > 0) {
          fileName = fileName.substring(0, questionMark);
        }
        if (StringUtils.isNotBlank(fileName)) {
          return fileName;
        }
      }
    }
    return defaultFileName;
  }

  /**
   * 获取文件扩展名
   *
   * @param fileName 文件名
   * @param defaultExtension 默认扩展名（当无法获取扩展名时使用）
   * @return 文件扩展名（小写）
   */
  protected String getFileExtension(String fileName, String defaultExtension) {
    if (StringUtils.isBlank(fileName)) {
      return defaultExtension;
    }
    String extension = FilenameUtils.getExtension(fileName);
    if (StringUtils.isBlank(extension)) {
      return defaultExtension;
    }
    return extension.toLowerCase();
  }

  /**
   * 上传文件
   *
   * @param fileBytes 文件字节数组
   * @param fileName 文件名
   * @param fileType 文件类型（扩展名）
   * @return 文件信息
   */
  protected FileInfoVO uploadFile(byte[] fileBytes, String fileName, String fileType) {
    UploadConfigVO uploadConfig = new UploadConfigVO();
    uploadConfig.setOriginalFileName(fileName);
    uploadConfig.setFileSize((long) fileBytes.length);
    uploadConfig.setFileType(fileType);
    uploadConfig.setStoreType(FileStoreUtils.getDefaultStoreType());

    try (InputStream fileStream = new ByteArrayInputStream(fileBytes)) {
      return fileStoreService.uploadFile(fileStream, uploadConfig);
    }
    catch (Exception e) {
      logger.error("文件上传失败: fileName={}, error={}", fileName, e.getMessage(), e);
      throw new BssException("文件上传失败: " + e.getMessage(), e);
    }
  }

  /**
   * 验证文件参数
   * 确保 fileId 和 fileUrl 至少有一个不为空
   *
   * @param params 插件参数
   */
  protected void validateFileParams(T params) {
    if (params.getFileId() == null && StringUtils.isEmpty(params.getFileUrl())) {
      throw new BssException("fileId和fileUrl不能同时为空");
    }
  }
}
