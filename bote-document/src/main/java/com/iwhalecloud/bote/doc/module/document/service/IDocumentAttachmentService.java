package com.iwhalecloud.bote.doc.module.document.service;

import com.iwhalecloud.bote.doc.module.document.dto.DocumentAttachmentDTO;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 文档附件资源表
 *
 * @author Aiqing
 * @since 2025/8/20
 */
public interface IDocumentAttachmentService {

  /**
   * 上传文件
   *
   * @param file 文件
   * @param documentId 文档ID
   */
  DocumentAttachmentDTO upload(File file, String documentId, Long userId);

  /**
   * 保存文档和附件的关系
   * @param fileInfoVOs 图片附件信息
   * @param documentId 文档id
   * @param userId 操作人
   * @return 返回附件和文档的关系
   */
  List<DocumentAttachmentDTO> save(List<FileInfoVO> fileInfoVOs, String documentId, Long userId);

  /**
   * 查询附件信息
   *
   * @param attachmentId 附件ID
   * @return 附件信息
   */
  DocumentAttachmentDTO getAttachmentInfo(Long attachmentId);

  /**
   * 读取文档的图片附件
   *
   * @param filePath 附件路径
   * @return 文件流
   */
  DocumentAttachmentDTO parseDocumentImageFile(String filePath);


  /**
   * 获取文档附件文件流
   *
   * @param attachmentDTO 附件信息
   * @param outputStream 输出流
   */
  void fetchAttachmentFile(DocumentAttachmentDTO attachmentDTO, OutputStream outputStream) throws IOException;

  /**
   * 从附件路径中解析出附件ID
   *
   * @param filePath 附件路径
   * @return 文档ID->附件ID
   */
  Long resolveAttachmentId(String filePath);
}
