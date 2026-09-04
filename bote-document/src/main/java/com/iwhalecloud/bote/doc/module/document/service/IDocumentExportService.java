package com.iwhalecloud.bote.doc.module.document.service;

import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Aiqing
 * @since 2025/9/25
 */
public interface IDocumentExportService {

  /**
   * 导出文档
   *
   * @param documentId 文档ID
   * @param outputStream 输出流
   */
  void exportDocument(String documentId, OutputStream outputStream) throws IOException;

  /**
   * 导出文档
   *
   * @param documentId 文档ID
   * @return 文件信息
   */
  FileInfoVO exportDocument(String documentId);

  /**
   * 导出文档
   *
   * @param documentDTO 文档信息
   * @param outputStream 输出流
   */
  void exportDocument(DcDocumentDTO documentDTO, OutputStream outputStream) throws IOException;

  /**
   * 下载文档内容
   *
   * @param documentDTO 文档信息
   * @param response response
   */
  void downloadDocument(DcDocumentDTO documentDTO, HttpServletResponse response) throws IOException;
}
