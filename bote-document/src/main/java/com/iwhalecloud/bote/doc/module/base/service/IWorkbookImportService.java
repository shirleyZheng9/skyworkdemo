package com.iwhalecloud.bote.doc.module.base.service;

import com.iwhalecloud.bote.doc.module.base.dto.UnifiedUploadRequest;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 工作簿导入服务接口
 * 用于将Excel文件转换为在线表格
 *
 * @author system
 * @since 2025-12-19
 */
public interface IWorkbookImportService {

  /**
   * 将上传的Excel文件转换为在线表格
   *
   * @param request 上传请求参数
   * @param file 上传的文件
   * @param documentDTO 上传成功的文档信息
   * @param userId 当前用户ID
   */
  void convertToOnlineWorkbook(UnifiedUploadRequest request, MultipartFile file, DcDocumentDTO documentDTO, Long userId);
  /**
   * 将上传的Excel文件转换为在线表格
   *
   * @param fileInfoVo 上传的文件信息
   * @param documentDTO 上传成功的文档信息
   * @param userId 当前用户ID
   */
  void convertToOnlineWorkbook(FileInfoVO fileInfoVo, DcDocumentDTO documentDTO, Long userId);
}

