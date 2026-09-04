package com.iwhalecloud.bote.doc.module.document.service.impl;

import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.exception.NodeNotExistException;
import com.iwhalecloud.bote.doc.consts.ContentSourceEnum;
import com.iwhalecloud.bote.doc.consts.DocumentTypeEnum;
import com.iwhalecloud.bote.doc.module.base.service.impl.FileUploadHelper;
import com.iwhalecloud.bote.doc.module.collaboration.doc.service.NodeJsService;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.ExportDocumentFileDTO;
import com.iwhalecloud.bote.doc.module.document.entity.DocumentExportSnapshotEntity;
import com.iwhalecloud.bote.doc.module.document.entity.WorkbookContentEntity;
import com.iwhalecloud.bote.doc.module.document.mapper.WorkbookContentMapper;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentExportService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentExportSnapshotService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.common.utils.ContentTypeUtil;
import com.iwhalecloud.bote.doc.common.utils.DcExcelutil;
import com.iwhalecloud.bote.doc.module.document.service.helper.DocumentAttachmentHelper;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.mapper.base.BoteFileInfoMapper;
import com.iwhalecloud.bote.mapper.base.FileInfoManageMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/**
 * 文档导出服务
 *
 * @author Aiqing
 * @since 2025/9/25
 */
@Service
@RequiredArgsConstructor
public class DocumentExportServiceImpl implements IDocumentExportService {

  private static final Logger logger = LoggerFactory.getLogger(DocumentExportServiceImpl.class);

  private final NodeJsService nodeJsService;
  private final BoteFileInfoMapper fileInfoMapper;
  private final FileInfoManageMapper fileInfoManageMapper;
  private final FileUploadHelper fileUploadHelper;
  private final WorkbookContentMapper workbookContentMapper;
  private final IFileStoreService fileStoreService;
  private final IDocumentService documentService;
  private final IDocumentExportSnapshotService documentExportSnapshotService;
  private final DocumentAttachmentHelper documentAttachmentHelper;

  @Override
  public void exportDocument(String documentId, OutputStream outputStream) throws IOException {
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    if (documentDTO == null) {
      throw new NodeNotExistException();
    }
    exportDocument(documentDTO, outputStream);
  }

  @Override
  public FileInfoVO exportDocument(String documentId) {
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    if (documentDTO == null) {
      throw new NodeNotExistException();
    }
    return exportDocumentFile(documentDTO);
  }

  @Override
  public void exportDocument(DcDocumentDTO documentDTO, OutputStream outputStream) throws IOException {
    FileInfoVO fileInfoVO = exportDocumentFile(documentDTO);
    try (InputStream inputStream = fileUploadHelper.downloadFileStream(fileInfoVO)) {
      IOUtils.copy(inputStream, outputStream);
      outputStream.flush();
    }
  }

  @Override
  public void downloadDocument(DcDocumentDTO documentDTO, HttpServletResponse response) throws IOException {
    FileInfoVO fileInfoVO = exportDocumentFile(documentDTO);
    String fileName = fileInfoVO.getFileName();

    // 下载响应头
    response.setContentType(ContentTypeUtil.getContentType(fileName));
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString());

    ServletOutputStream outputStream = response.getOutputStream(); //NOPMD - suppressed CloseResource - HTTP 输出流不需要手动关闭
    try (InputStream inputStream = fileUploadHelper.downloadFileStream(fileInfoVO)) {
      IOUtils.copy(inputStream, outputStream);
      outputStream.flush();
    }
  }

  @NonNull
  private FileInfoVO exportDocumentFile(DcDocumentDTO documentDTO) {
    String documentId = documentDTO.getDocumentId();
    if (ContentSourceEnum.UPLOAD.getCode().equals(documentDTO.getContentSource())) {
      return checkFileExists(documentDTO);
    }
    else {
      // 在线文档，先从导出记录中取, 按照默认导出格式
      String exportExtension = matchOnlineDocExportExtension(documentDTO.getDocumentType());
      DocumentExportSnapshotEntity snapshotEntity =
        documentExportSnapshotService.selectByDocumentIdAndRevision(documentId, documentDTO.getRevision(), exportExtension);
      if (snapshotEntity != null) {
        FileInfoVO fileInfoVO = fileInfoMapper.getFileInfoById(snapshotEntity.getFileId());
        if (fileInfoVO == null) {
          return exportFromLastContent(documentId, documentDTO);
        }
        return fileInfoVO;
      }
      return exportFromLastContent(documentId, documentDTO);
    }
  }

  private @NonNull FileInfoVO exportFromLastContent(String documentId, DcDocumentDTO documentDTO) {
    Path tempDir = null;
    try {
      // 创建临时目录
      tempDir = Files.createTempDirectory("doc_export_");

      Pair<ExportDocumentFileDTO, Path> exportFilePair = processOnlineDocumentExport(documentDTO, tempDir);
      ExportDocumentFileDTO fileDTO = exportFilePair.getLeft();
      Path tempFile = exportFilePair.getRight();

      // 从临时文件上传
      try (FileInputStream inputStream = new FileInputStream(tempFile.toFile())) {
        FileInfoVO fileInfoVO = fileUploadHelper.uploadFile(inputStream, fileDTO.getFileName(), fileDTO.getFileSize());

        // 保存导出快照
        DocumentExportSnapshotEntity exportSnapshotEntity = new DocumentExportSnapshotEntity();
        exportSnapshotEntity.setId(IDUtils.nextId());
        exportSnapshotEntity.setDocumentId(documentId);
        exportSnapshotEntity.setRevision(documentDTO.getRevision());
        exportSnapshotEntity.setFileId(fileInfoVO.getFileId());
        exportSnapshotEntity.setFileExtension(fileDTO.getFileExtension());
        exportSnapshotEntity.setFileSize(fileDTO.getFileSize());
        exportSnapshotEntity.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
        exportSnapshotEntity.setTenantId(documentDTO.getTenantId());
        documentExportSnapshotService.insert(exportSnapshotEntity);
        return fileInfoVO;
      }
    }
    catch (IOException e) {
      throw new BssException("导出文档失败: " + e.getMessage(), e);
    }
    finally {
      // 清理临时目录及其中的所有文件
      if (tempDir != null) {
        deleteDirectoryRecursively(tempDir);
      }
    }
  }

  @NonNull
  private FileInfoVO checkFileExists(DcDocumentDTO documentDTO) {
    Long fileInfoId = documentDTO.getFileInfoId();
    FileInfoDTO fileInfo = fileInfoManageMapper.getFileInfo(documentDTO.getTenantId(), fileInfoId);
    if (fileInfo == null) {
      throw new BssException("文件不存在");
    }
    long fileSize = fileStoreService.getFileSize(fileInfo.getFileId());
    if (fileSize < 0) {
      throw new BssException("文件不存在");
    }
    FileInfoVO vo = new FileInfoVO();
    vo.setFileId(fileInfo.getFileId());
    vo.setFileName(fileInfo.getFileName());
    vo.setFileType(fileInfo.getFileType());
    vo.setFileSize(fileInfo.getFileSize());
    vo.setStoreType(fileInfo.getStoreType());
    vo.setFilePathInServer(fileInfo.getFilePathInServer());
    return vo;
  }

  private Pair<ExportDocumentFileDTO, Path> processOnlineDocumentExport(DcDocumentDTO documentDTO, Path tempDir) throws IOException {
    // 获取在线文档内容
    String documentType = documentDTO.getDocumentType();

    if (DocumentTypeEnum.EXCEL_ONLINE.getCode().equals(documentType)) {
      WorkbookContentEntity workbookContentEntity = workbookContentMapper.selectByDocumentIdAndRevision(
        documentDTO.getDocumentId(), documentDTO.getRevision());
      if (workbookContentEntity == null || workbookContentEntity.getContent() == null) {
        throw new BssException("在线文档数据丢失");
      }
      // 在临时目录中创建临时文件
      String fileExtension = DocBaseConsts.DOCUMENT_EXCEL_EXTENSION;
      String fileName = documentDTO.getDocumentName() + "." + fileExtension;
      Path tempFile = tempDir.resolve("excel_export_" + System.currentTimeMillis() + "." + fileExtension);

      // 转换Luckysheet数据为Excel并写入临时文件
      DcExcelutil.convertLuckysheetToExcel(workbookContentEntity.getContent(), tempFile.toString(), documentAttachmentHelper);

      long fileSize = Files.size(tempFile);
      ExportDocumentFileDTO fileDTO = new ExportDocumentFileDTO(fileName, fileSize, fileExtension);

      return Pair.of(fileDTO, tempFile);
    }
    else if (DocumentTypeEnum.WORD_ONLINE.getCode().equals(documentType)) {
      String fileExtension = DocBaseConsts.DOCUMENT_DOCX_EXTENSION;
      String fileName = documentDTO.getDocumentName() + "." + fileExtension;
      // 使用NodeJsService创建临时文件
      Path tempFile = nodeJsService.exportDocumentWithDocxFormatToTempFile(documentDTO.getDocumentId(), tempDir);
      long fileSize = Files.size(tempFile);
      ExportDocumentFileDTO fileDTO = new ExportDocumentFileDTO(fileName, fileSize, fileExtension);
      return Pair.of(fileDTO, tempFile);
    }
    else {
      throw new BssException("不支持的文档格式");
    }
  }

  private String matchOnlineDocExportExtension(String documentType) {
    if (DocumentTypeEnum.EXCEL_ONLINE.getCode().equals(documentType)) {
      return DocBaseConsts.DOCUMENT_EXCEL_EXTENSION;
    }
    if (DocumentTypeEnum.WORD_ONLINE.getCode().equals(documentType)) {
      return DocBaseConsts.DOCUMENT_DOCX_EXTENSION;
    }
    throw new BssException("不支持的文档格式");
  }

  /**
   * 递归删除目录及其中的所有文件
   *
   * @param directory 要删除的目录路径
   */
  private void deleteDirectoryRecursively(Path directory) {
    if (directory != null && Files.exists(directory)) {
      boolean deleted = FileUtils.deleteQuietly(directory.toFile());
      if (!deleted) {
        logger.warn("清理临时目录失败: {}", directory);
      }
    }
  }

}
