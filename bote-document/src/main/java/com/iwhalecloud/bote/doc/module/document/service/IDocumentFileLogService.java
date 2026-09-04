package com.iwhalecloud.bote.doc.module.document.service;

import com.iwhalecloud.bote.doc.module.document.dto.DocumentFileLogDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentFileLogInfoDTO;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 文档上传文件日志（bt_dc_document_file_log）
 *
 * @author bote-doc
 * @since 2026-04-15
 */
public interface IDocumentFileLogService {

  /**
   * 记录一次与文档版本、文件主键一致的上传/覆盖（直接写 file_id）。
   * 仅当系统参数 {@code DOCUMENT_HIS_LOG_ENABLED}（与 {@code com.iwhalecloud.bote.common.enums.SystemParameter#DOCUMENT_HIS_LOG_ENABLED} 同字段）为 T/Y/TRUE/1 时写入。
   *
   * @param documentId 文档 ID
   * @param fileId 文件主键
   * @param revision 与 bt_dc_document.revision 一致
   * @param creatorId 操作人
   * @param tenantId 租户 ID
   */
  void append(String documentId, Long fileId, Long revision, Long creatorId, Long tenantId);

  /**
   * 根据 file_info 主键解析 file_id 后写入日志
   *
   * @param documentId 文档 ID
   * @param fileInfoId 文件信息表主键
   * @param revision 与 bt_dc_document.revision 一致
   * @param creatorId 操作人
   * @param tenantId 租户 ID
   */
  void appendByFileInfoId(String documentId, Long fileInfoId, Long revision, Long creatorId, Long tenantId);

  /**
   * 按日志主键查询明细（关联文档表、文件表补充文档名称、文件名、内容来源）
   *
   * @param id 日志主键
   * @param tenantId 租户 ID
   * @return 明细，不存在时返回 null
   */
  @Nullable
  DocumentFileLogDTO getDetailById(Long id, Long tenantId);

  /**
   * 按文档 ID 查询日志列表（关联文档表、文件表补充文档名称、文件名、内容来源）
   *
   * @param documentId 文档 ID
   * @param tenantId 租户 ID
   * @return 按自然日分组的列表（每组含分组名与明细），无记录时返回空列表
   */
  List<DocumentFileLogInfoDTO> listDetailByDocumentId(@Nullable String documentId, @Nullable Long tenantId);
}
