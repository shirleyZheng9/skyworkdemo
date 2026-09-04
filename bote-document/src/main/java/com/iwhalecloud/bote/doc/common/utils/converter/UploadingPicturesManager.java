package com.iwhalecloud.bote.doc.common.utils.converter;

import com.iwhalecloud.bote.doc.module.document.dto.DocumentAttachmentDTO;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentAttachmentService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.GuardLogStatement")
public class UploadingPicturesManager implements PicturesManager {
  private static final Logger logger = LoggerFactory.getLogger(UploadingPicturesManager.class);
  private final IDocumentAttachmentService attachmentService;
  private final String documentId;
  private final Long userId;

  public UploadingPicturesManager(IDocumentAttachmentService attachmentService, String documentId, Long userId) {
    this.attachmentService = attachmentService;
    this.documentId = documentId;
    this.userId = userId;
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public String savePicture(byte[] content, PictureType pictureType, String suggestedName, float width, float height) {
    if (!validatePictureInput(content)) {
      return "";
    }
    if (shouldUseBase64Fallback()) {
      return buildBase64DataUri(content, pictureType);
    }

    try {
      String url = uploadPictureToService(content, suggestedName, pictureType);
      if (StringUtils.isNotBlank(url)) {
        return url;
      }
    }
    catch (Exception e) {
      logger.warn("上传旧版Word图片失败: documentId={}, error={}", documentId, e.getMessage());
    }
    return buildBase64DataUri(content, pictureType);
  }

  /**
   * 验证图片输入
   */
  private boolean validatePictureInput(byte[] content) {
    return content != null && content.length > 0;
  }

  /**
   * 判断是否应该使用 Base64 回退
   */
  private boolean shouldUseBase64Fallback() {
    return attachmentService == null || userId == null || StringUtils.isBlank(documentId);
  }

  /**
   * 上传图片到服务
   */
  private String uploadPictureToService(byte[] content, String suggestedName, PictureType pictureType)
    throws IOException {
    String fileName = createPictureFileName(suggestedName, pictureType);
    File tempFile = createFileFromBytes(content, fileName);
    try {
      DocumentAttachmentDTO attachmentDTO = attachmentService.upload(tempFile, documentId, userId);
      if (attachmentDTO != null && StringUtils.isNotBlank(attachmentDTO.getUrl())) {
        return attachmentDTO.getUrl();
      }
    }
    finally {
      cleanupTempFile(tempFile);
    }
    return null;
  }

  /**
   * 清理临时文件
   */
  private void cleanupTempFile(File tempFile) {
    if (tempFile != null) {
      try {
        Files.deleteIfExists(tempFile.toPath());
      }
      catch (IOException e) {
        logger.debug("删除临时文件失败: {}", tempFile.getAbsolutePath(), e);
      }
    }
  }

  private String createPictureFileName(String suggestedName, PictureType pictureType) {
    String extension = pictureType != null ? pictureType.getExtension() : "png";
    String baseName = StringUtils.isNotBlank(suggestedName) ? suggestedName : "doc_image_" + System.nanoTime();
    if (baseName.toLowerCase().endsWith("." + extension)) {
      return baseName;
    }
    return baseName + "." + extension;
  }
  private String buildBase64DataUri(byte[] content, PictureType pictureType) {
    String mime = pictureType != null ? pictureType.getMime() : "image/png";
    return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(content);
  }

  /**
   * 从字节数组创建临时文件
   * <p>注意：调用方负责在使用完毕后删除临时文件。建议使用 try-finally 或 try-with-resources 模式确保清理。</p>
   *
   * @param content 文件内容
   * @param fileName 文件名
   * @return 临时文件（调用方负责删除）
   * @throws IOException 如果文件创建失败
   */
  public static File createFileFromBytes(byte[] content, String fileName) throws IOException {
    File tempFile = File.createTempFile("upload_", "_" + fileName);
    // 设置 JVM 退出时删除，作为最后的清理机制（双重保护）
    tempFile.deleteOnExit();
    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
      fos.write(content);
      fos.flush();
    }
    return tempFile;
  }
}
