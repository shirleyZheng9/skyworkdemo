package com.iwhalecloud.bote.doc.module.base.service;

import com.iwhalecloud.bote.doc.module.base.dto.UnifiedUploadRequest;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档导入服务接口
 * 用于将本地文档转换为在线文档
 *
 * @author lizuyin
 * @since 2025-10-22
 */
public interface IDocumentImportService {

  /**
   * 将上传的本地文档转换为在线文档
   *
   * @param file 上传的文件
   * @param documentDTO 上传成功的文档信息
   * @param userId 当前用户ID
   */
  void convertToOnlineDocument(UnifiedUploadRequest request, MultipartFile file, DcDocumentDTO documentDTO, Long userId);

  /**
   * 将上传的本地文档转换为在线文档
   *
   * @param fileInfoVo 上传的文件信息
   * @param documentDTO 上传成功的文档信息
   * @param userId 当前用户ID
   */
  void convertToOnlineDocument(FileInfoVO fileInfoVo, DcDocumentDTO documentDTO, Long userId);
}

