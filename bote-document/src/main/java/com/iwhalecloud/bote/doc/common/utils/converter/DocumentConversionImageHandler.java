package com.iwhalecloud.bote.doc.common.utils.converter;

import com.iwhalecloud.bote.doc.module.document.dto.DocumentAttachmentDTO;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentAttachmentService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.docx4j.model.images.AbstractWordXmlPicture;
import org.docx4j.model.images.ConversionImageHandler;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.relationships.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * docx4j HTML 转换阶段的图片处理器。
 * <p>在生成 &lt;img&gt; 标签时同步上传图片并返回文件服务器 URL，避免 HTML 内嵌 Base64。</p>
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class DocumentConversionImageHandler implements ConversionImageHandler {
  private static final Logger logger = LoggerFactory.getLogger(DocumentConversionImageHandler.class);
  private final IDocumentAttachmentService attachmentService;
  private final String documentId;
  private final Long userId;
  private final Map<String, String> cache = new HashMap<>();

  public DocumentConversionImageHandler(IDocumentAttachmentService attachmentService, String documentId, Long userId) {
    this.attachmentService = attachmentService;
    this.documentId = documentId;
    this.userId = userId;
  }

  @Override
  public String handleImage(AbstractWordXmlPicture picture, Relationship relationship, BinaryPart binaryPart)
    throws Docx4JException {
    if (!validateInputs()) {
      return null;
    }

    BinaryPartAbstractImage imagePart = extractImagePart(binaryPart);
    if (imagePart == null) {
      return null;
    }

    String relId = extractRelId(relationship, binaryPart);
    String cachedUrl = getCachedUrl(relId);
    if (cachedUrl != null) {
      return cachedUrl;
    }

    return processImageUpload(imagePart, relId);
  }

  /**
   * 验证输入参数
   */
  private boolean validateInputs() {
    return attachmentService != null && userId != null && StringUtils.isNotBlank(documentId);
  }

  /**
   * 提取图片部分
   */
  private BinaryPartAbstractImage extractImagePart(BinaryPart binaryPart) {
    if (binaryPart instanceof BinaryPartAbstractImage imagePart) {
      return imagePart;
    }
    return null;
  }

  /**
   * 提取关系ID
   */
  private String extractRelId(Relationship relationship, BinaryPart binaryPart) {
    if (relationship != null) {
      return relationship.getId();
    }
    return binaryPart.getPartName().getName();
  }

  /**
   * 获取缓存的URL
   */
  private String getCachedUrl(String relId) {
    if (relId != null && cache.containsKey(relId)) {
      return cache.get(relId);
    }
    return null;
  }

  /**
   * 处理图片上传并缓存结果
   */
  private String processImageUpload(BinaryPartAbstractImage imagePart, String relId) {
    try {
      String imageUrl = uploadImageAndGetUrl(imagePart, relId, attachmentService, documentId, userId);
      if (StringUtils.isNotBlank(imageUrl) && relId != null) {
        cache.put(relId, imageUrl);
      }
      return imageUrl;
    }
    catch (Exception ex) {
      logger.warn("HTML转换上传图片失败: documentId={}, relId={}, error={}", documentId, relId, ex.getMessage());
      return null;
    }
  }
  /**
   * 上传图片并返回URL
   *
   * @param imagePart 图片部分
   * @param relId 关系ID
   * @param documentAttachmentService 文档附件服务
   * @param documentId 文档ID
   * @param userId 用户ID
   * @return 上传成功返回图片URL，失败返回null
   */
  private String uploadImageAndGetUrl(BinaryPartAbstractImage imagePart, String relId,
    IDocumentAttachmentService documentAttachmentService, String documentId, Long userId) {
    byte[] imageBytes = imagePart.getBytes();
    if (imageBytes == null || imageBytes.length == 0) {
      return null;
    }

    String imageFileName = generateImageFileName(relId, imagePart);
    File tempFile = null;
    try {
      tempFile = UploadingPicturesManager.createFileFromBytes(imageBytes, imageFileName);
      DocumentAttachmentDTO attachmentDTO = documentAttachmentService.upload(tempFile, documentId, userId);

      if (attachmentDTO == null || StringUtils.isBlank(attachmentDTO.getUrl())) {
        return null;
      }

      String imageUrl = attachmentDTO.getUrl();
      logger.debug("图片上传成功: relId={}, fileName={}, url={}", relId, imageFileName, imageUrl);
      return imageUrl;
    }
    catch (Exception e) {
      logger.warn("上传图片失败: relId={}, fileName={}, error={}", relId, imageFileName, e.getMessage());
      return null;
    }
    finally {
      // 确保临时文件被清理，即使发生异常
      if (tempFile != null) {
        try {
          Files.deleteIfExists(tempFile.toPath());
        }
        catch (IOException e) {
          logger.debug("删除临时文件失败: {}", tempFile.getAbsolutePath(), e);
        }
      }
    }
  }
  /**
   * 生成图片文件名
   */
  private String generateImageFileName(String relId, BinaryPartAbstractImage imagePart) {
    String contentType = imagePart.getContentType();
    String extension = "png"; // 默认扩展名

    if (contentType != null) {
      if (contentType.contains("jpeg") || contentType.contains("jpg")) {
        extension = "jpg";
      }
      else if (contentType.contains("png")) {
        extension = "png";
      }
      else if (contentType.contains("gif")) {
        extension = "gif";
      }
      else if (contentType.contains("bmp")) {
        extension = "bmp";
      }
    }

    return "image_" + relId + "." + extension;
  }
}
