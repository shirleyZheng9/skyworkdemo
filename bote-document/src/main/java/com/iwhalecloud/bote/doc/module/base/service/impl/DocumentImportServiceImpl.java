package com.iwhalecloud.bote.doc.module.base.service.impl;

import com.iwhalecloud.bote.doc.module.base.dto.UnifiedUploadRequest;
import com.iwhalecloud.bote.doc.module.base.service.IDocumentImportService;
import com.iwhalecloud.bote.doc.module.collaboration.doc.service.NodeJsService;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.mapper.DocumentMapper;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentAttachmentService;
import com.iwhalecloud.bote.doc.common.utils.DocumentImportUtils;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档导入服务实现类 用于将本地文档转换为在线文档
 *
 * @author lizuyin
 * @since 2025-10-22
 */
@Service
@RequiredArgsConstructor
public class DocumentImportServiceImpl implements IDocumentImportService {

  private static final Logger logger = LoggerFactory.getLogger(DocumentImportServiceImpl.class);

  private final NodeJsService nodeJsService;

  private final DocumentMapper documentMapper;
  private final FileUploadHelper fileUploadHelper;
  private final IDocumentAttachmentService documentAttachmentService;

  /**
   * 将上传的本地文档转换为在线文档
   *
   * @param request 上传请求参数
   * @param file 上传的文件
   * @param documentDTO 上传成功的文档信息
   * @param userId 当前用户ID
   */
  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void convertToOnlineDocument(UnifiedUploadRequest request, MultipartFile file, DcDocumentDTO documentDTO,
    Long userId) {
    try {
      String fileName = request.getOriginalFileName();
      String documentId = documentDTO.getDocumentId();

      logger.debug("开始将本地文档转换为在线文档: documentId={}, fileName={}", documentId, fileName);

      // 验证文件类型是否为word文档
      if (DocumentImportUtils.isNotWordDocument(fileName)) {
        logger.warn("非word文档不支持转换为在线文档: fileName={}", fileName);
        return;
      }

      // 将word文档转换为html，并在转换前提取并上传图片到文件服务器
      String htmlContent = DocumentImportUtils.convertWordToHtml(file, fileName, documentId, documentAttachmentService, userId);
      logger.debug("Word文档转换为HTML成功: documentId={}, htmlLength={}", documentId, htmlContent.length());

      // 调用NodeJsService导入内容到在线文档
      nodeJsService.importDocumentContent(documentId, "html", htmlContent, userId.toString());
      logger.debug("在线文档内容导入成功: documentId={}", documentId);

      // 更新文档信息：去掉文件名后缀，修改类型为在线文档
      String documentNameWithoutExt = removeFileExtension(fileName);
      int updateCount = documentMapper.updateConvertedToOnlineDocument(documentId, documentNameWithoutExt, userId);
      if (updateCount > 0) {
        logger.debug("文档转换为在线文档后，数据库更新成功: documentId={}, newName={}", documentId,
          documentNameWithoutExt);
      }
      else {
        logger.warn("文档转换为在线文档后，数据库更新失败: documentId={}", documentId);
      }

    }
    catch (Exception e) {
      // 转换失败不影响原上传结果，仅记录日志
      logger.error("将本地文档转换为在线文档失败: documentId={}, error={}", documentDTO.getDocumentId(), e.getMessage(),
        e);
    }
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void convertToOnlineDocument(FileInfoVO fileInfoVo, DcDocumentDTO documentDTO, Long userId) {
    try {
      String fileName = fileInfoVo.getFileName();
      String documentId = documentDTO.getDocumentId();

      logger.debug("开始将本地文档转换为在线文档: documentId={}, fileName={}", documentId, fileName);

      // 验证文件类型是否为word文档
      if (DocumentImportUtils.isNotWordDocument(fileName)) {
        logger.warn("非word文档不支持转换为在线文档: fileName={}", fileName);
        return;
      }
      String htmlContent = "";
      // 获取文件大小，用于优化内存使用（避免 OOM）
      Long fileSize = fileInfoVo.getFileSize();
      try (InputStream inputStream = fileUploadHelper.downloadFileStream(fileInfoVo)) {
        // 将word文档转换为html，并在转换前提取并上传图片到文件服务器
        htmlContent = DocumentImportUtils.processWordToHtml(inputStream, fileName, documentId, documentAttachmentService, userId, fileSize);
      }

      logger.debug("Word文档转换为HTML成功: documentId={}, htmlLength={}", documentId, htmlContent.length());

      // 调用NodeJsService导入内容到在线文档
      nodeJsService.importDocumentContent(documentId, "html", htmlContent, userId.toString());
      logger.debug("在线文档内容导入成功: documentId={}", documentId);

      // 更新文档信息：去掉文件名后缀，修改类型为在线文档
      String documentNameWithoutExt = removeFileExtension(fileName);
      int updateCount = documentMapper.updateConvertedToOnlineDocument(documentId, documentNameWithoutExt, userId);
      if (updateCount > 0) {
        logger.debug("文档转换为在线文档后，数据库更新成功: documentId={}, newName={}", documentId,
          documentNameWithoutExt);
      }
      else {
        logger.warn("文档转换为在线文档后，数据库更新失败: documentId={}", documentId);
      }

    }
    catch (Exception e) {
      // 转换失败不影响原上传结果，仅记录日志
      logger.error("将本地文档转换为在线文档失败: documentId={}, error={}", documentDTO.getDocumentId(), e.getMessage(),
        e);
    }
  }

  /**
   * 去除文件名的扩展名
   *
   * @param fileName 文件名
   * @return 去除扩展名后的文件名
   */
  private String removeFileExtension(String fileName) {
    if (StringUtils.isBlank(fileName)) {
      return fileName;
    }

    // 去除.doc或.docx后缀
    if (fileName.toLowerCase().endsWith(".docx")) {
      return fileName.substring(0, fileName.length() - 5);
    }
    else if (fileName.toLowerCase().endsWith(".doc")) {
      return fileName.substring(0, fileName.length() - 4);
    }

    return fileName;
  }
}

