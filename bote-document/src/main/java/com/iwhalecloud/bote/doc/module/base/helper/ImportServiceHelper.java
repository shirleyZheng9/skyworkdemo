package com.iwhalecloud.bote.doc.module.base.helper;

import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.consts.DocumentTypeEnum;
import com.iwhalecloud.bote.doc.module.base.dto.UnifiedUploadRequest;
import com.iwhalecloud.bote.doc.module.base.service.IDocumentImportService;
import com.iwhalecloud.bote.doc.module.base.service.IWorkbookImportService;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.mapper.base.BoteFileInfoMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传转在线文档的辅助类
 */
@Component
@RequiredArgsConstructor
public class ImportServiceHelper {

  private final IDocumentImportService documentImportService;
  private final IWorkbookImportService workbookImportService;
  private final BoteFileInfoMapper boteFileInfoMapper;

  /**
   * 将上传文件转在线文档，用于当个文件上传的情况
   * @param convertToOnline
   * @param documentDTO
   * @param userId
   */
  public void convertToOnlineIfNeeded(String convertToOnline, DcDocumentDTO documentDTO,
    Long userId) {
    if (!DocBaseConsts.TRUE.equals(convertToOnline)) {
      return;
    }
    FileInfoVO fileInfoVo = boteFileInfoMapper.getFileInfoByFileInfoId(documentDTO.getFileInfoId(), documentDTO.getTenantId());
    String fileType = FilenameUtils.getExtension(fileInfoVo.getFileName());
    Assert.hasText(fileType, "获取文件类型失败");
    if (isFileTypeAllowed(fileType, BaseSystemParameter.CONVERT_TO_ONLINE_WORD_TYPE.getValueFromDb())) {
      documentImportService.convertToOnlineDocument(fileInfoVo, documentDTO, userId);
    }
    if (isFileTypeAllowed(fileType, BaseSystemParameter.CONVERT_TO_ONLINE_EXCEL_TYPE.getValueFromDb())) {
      workbookImportService.convertToOnlineWorkbook(fileInfoVo, documentDTO, userId);
    }
  }

  /**
   * 将上传文件转在线文档，用于单个文件上传的情况
   * @param file
   * @param request
   * @param documentDTO
   * @param userId
   */
  public void convertToOnlineIfNeeded(MultipartFile file, UnifiedUploadRequest request, DcDocumentDTO documentDTO,
    Long userId) {
    if (!DocBaseConsts.TRUE.equals(request.getConvertToOnline()) || StringUtils.isBlank(documentDTO.getDocumentId())) {
      return;
    }
    String fileName = resolveFileName(file, request);
    String fileType = FilenameUtils.getExtension(fileName);
    Assert.hasText(fileType, "获取文件类型失败");
    if (isFileTypeAllowed(fileType, BaseSystemParameter.CONVERT_TO_ONLINE_WORD_TYPE.getValueFromDb())) {
      documentImportService.convertToOnlineDocument(request, file, documentDTO, userId);
    }
    if (isFileTypeAllowed(fileType, BaseSystemParameter.CONVERT_TO_ONLINE_EXCEL_TYPE.getValueFromDb())) {
      workbookImportService.convertToOnlineWorkbook(request, file, documentDTO, userId);
    }
  }

  public String resolveFileName(MultipartFile file, UnifiedUploadRequest request) {
    String fileName = StringUtils.trimToNull(request.getOriginalFileName());
    if (StringUtils.isBlank(fileName)) {
      fileName = StringUtils.trimToNull(file.getOriginalFilename());
    }
    Assert.hasText(fileName, "文件名不能为空");
    return fileName;
  }

  public String getFileType(String fileName) {
    String fileType = FilenameUtils.getExtension(fileName);
    Assert.hasText(fileType, "获取文件类型失败");
    if (isFileTypeAllowed(fileType, BaseSystemParameter.CONVERT_TO_ONLINE_WORD_TYPE.getValueFromDb())) {
      return DocumentTypeEnum.WORD_ONLINE.getCode();
    }
    if (isFileTypeAllowed(fileType, BaseSystemParameter.CONVERT_TO_ONLINE_EXCEL_TYPE.getValueFromDb())) {
      return DocumentTypeEnum.EXCEL_ONLINE.getCode();
    }
    throw new BssException("文件类型不支持");
  }

  private boolean isFileTypeAllowed(String fileType, String allowTypesFromDb) {
    String[] allowFileTypes = allowTypesFromDb.split(",");
    return ArrayUtils.contains(allowFileTypes, fileType);
  }
}
