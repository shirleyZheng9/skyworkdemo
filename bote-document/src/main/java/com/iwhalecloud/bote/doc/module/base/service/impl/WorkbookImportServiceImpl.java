package com.iwhalecloud.bote.doc.module.base.service.impl;

import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.utils.DcExcelutil;
import com.iwhalecloud.bote.doc.module.base.dto.UnifiedUploadRequest;
import com.iwhalecloud.bote.doc.module.base.service.IWorkbookImportService;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.entity.WorkbookContentEntity;
import com.iwhalecloud.bote.doc.module.document.mapper.DocumentMapper;
import com.iwhalecloud.bote.doc.module.document.mapper.WorkbookContentMapper;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentAttachmentService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;

/**
 * 工作簿导入服务实现类 用于将Excel文件转换为在线表格
 *
 * @author lizuyin
 * @since 2025-11-04
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class WorkbookImportServiceImpl implements IWorkbookImportService {

  private static final Logger logger = LoggerFactory.getLogger(WorkbookImportServiceImpl.class);

  private final WorkbookContentMapper workbookContentMapper;

  private final DocumentMapper documentMapper;
  private final FileUploadHelper fileUploadHelper;
  private final IDocumentAttachmentService documentAttachmentService;

  /**
   * 将上传的Excel文件转换为在线表格
   *
   * @param request 上传请求参数
   * @param file 上传的文件
   * @param documentDTO 上传成功的文档信息
   * @param userId 当前用户ID
   */
  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void convertToOnlineWorkbook(UnifiedUploadRequest request, MultipartFile file, DcDocumentDTO documentDTO,
    Long userId) {
    Assert.notNull(request, "excel转换请求不能为空");
    Assert.notNull(file, "上传的文件不能为空");

    String fileName = request.getOriginalFileName();
    if (StringUtils.isBlank(fileName)) {
      fileName = file.getOriginalFilename();
    }
    String documentId = documentDTO.getDocumentId();

    logger.debug("开始将Excel文件转换为在线表格: documentId={}, fileName={}", documentId, fileName);

    // 验证文件类型是否为Excel文件
    if (StringUtils.isBlank(fileName) || isNotExcelDocument(fileName)) {
      logger.warn("非Excel文件不支持转换为在线表格: fileName={}", fileName);
      return;
    }

    File tempFile = null;
    try {
      // 将文件保存到临时路径
      tempFile = File.createTempFile("excel_import_", getFileExtension(fileName));
      file.transferTo(tempFile);
      logger.debug("Excel文件保存到临时路径: tempPath={}", tempFile.getAbsolutePath());

      // 调用DcExcelutil转换为LuckySheet JSON（传入文档附件服务以支持图片上传）
      String luckysheetContent = DcExcelutil.convertExcelToLuckysheet(tempFile.getAbsolutePath(), documentId, userId,
        documentAttachmentService);

      // 保存并更新文档
      saveAndUpdateWorkbookContent(documentId, fileName, luckysheetContent, userId);
    }
    catch (Exception e) {
      logger.error("将Excel文件转换为在线表格失败: documentId={}, error={}", documentId, e.getMessage(), e);
      throw new RuntimeException(e);
    }
    finally {
      // 清理临时文件
      if (tempFile != null) {
        cleanupTempFile(tempFile);
      }
    }
  }

  @Override
  public void convertToOnlineWorkbook(FileInfoVO fileInfoVo, DcDocumentDTO documentDTO, Long userId) {
    Assert.notNull(fileInfoVo, "上传的文件不能为空");

    String fileName = fileInfoVo.getFileName();
    String documentId = documentDTO.getDocumentId();

    logger.debug("开始将Excel文件转换为在线表格: documentId={}, fileName={}", documentId, fileName);

    // 验证文件类型是否为Excel文件
    if (StringUtils.isBlank(fileName) || isNotExcelDocument(fileName)) {
      logger.warn("非Excel文件不支持转换为在线表格: fileName={}", fileName);
      return;
    }

    try {
      // 调用DcExcelutil转换为LuckySheet JSON（传入文档附件服务以支持图片上传）
      String luckysheetContent;
      try (InputStream inputStream = fileUploadHelper.downloadFileStream(fileInfoVo)) {
        luckysheetContent = DcExcelutil.convertExcelToLuckysheet(inputStream, documentId, userId,
          documentAttachmentService);
      }

      // 保存并更新文档
      saveAndUpdateWorkbookContent(documentId, fileName, luckysheetContent, userId);
    }
    catch (Exception e) {
      logger.error("将Excel文件转换为在线表格失败: documentId={}, error={}", documentId, e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  /**
   * 保存工作簿内容到数据库并更新文档信息（公共逻辑）
   *
   * @param documentId 文档ID
   * @param fileName 文件名
   * @param luckysheetContent LuckySheet JSON内容
   * @param userId 用户ID
   */
  private void saveAndUpdateWorkbookContent(String documentId, String fileName, String luckysheetContent,
    Long userId) {
    logger.debug("Excel转换为LuckySheet成功: documentId={}, contentLength={}", documentId,
      luckysheetContent.length());

    // 创建WorkbookContentEntity并保存到数据库
    WorkbookContentEntity entity = new WorkbookContentEntity();
    entity.setId(IDUtils.nextId());
    entity.setDocumentId(documentId);
    entity.setContent(luckysheetContent);
    entity.setRevision(DocBaseConsts.DOCUMENT_INIT_REVISION);
    entity.setCreatorId(userId);
    entity.setUpdatorId(userId);
    entity.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    workbookContentMapper.insert(entity);
    logger.debug("工作簿内容保存成功: documentId={}", documentId);

    // 更新文档信息：去掉文件名后缀，修改类型为在线表格
    String documentNameWithoutExt = removeFileExtension(fileName);
    int updateCount = documentMapper.updateConvertedToOnlineWorkbook(documentId, documentNameWithoutExt, userId);
    if (updateCount > 0) {
      logger.debug("文档转换为在线表格后，数据库更新成功: documentId={}, newName={}", documentId,
        documentNameWithoutExt);
    }
    else {
      logger.warn("文档转换为在线表格后，数据库更新失败: documentId={}", documentId);
    }
  }

  /**
   * 清理临时文件
   *
   * @param tempFile 临时文件
   */
  private void cleanupTempFile(File tempFile) {
    if (tempFile != null && tempFile.exists()) {
      try {
        Files.delete(tempFile.toPath());
        logger.debug("临时文件已删除: tempPath={}", tempFile.getAbsolutePath());
      }
      catch (IOException e) {
        logger.warn("删除临时文件失败: tempPath={}, error={}", tempFile.getAbsolutePath(), e.getMessage());
      }
    }
  }

  /**
   * 判断文件是否不为Excel文档
   *
   * @param fileName 文件名
   * @return 如果是Excel文档返回false，否则返回true
   */
  private boolean isNotExcelDocument(String fileName) {
    if (StringUtils.isBlank(fileName)) {
      return true;
    }

    String lowerCaseFileName = fileName.toLowerCase();
    return !lowerCaseFileName.endsWith(".xls") && !lowerCaseFileName.endsWith(".xlsx");
  }

  /**
   * 获取文件扩展名
   *
   * @param fileName 文件名
   * @return 文件扩展名（包含点号）
   */
  private String getFileExtension(String fileName) {
    if (StringUtils.isBlank(fileName)) {
      return ".tmp";
    }
    int lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
      return fileName.substring(lastDotIndex);
    }
    return ".tmp";
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

    String lowerCaseFileName = fileName.toLowerCase();
    // 去除.xls或.xlsx后缀
    if (lowerCaseFileName.endsWith(".xlsx")) {
      return fileName.substring(0, fileName.length() - 5);
    }
    else if (lowerCaseFileName.endsWith(".xls")) {
      return fileName.substring(0, fileName.length() - 4);
    }

    return fileName;
  }
}

