package com.iwhalecloud.bote.doc.module.document.service;

import com.iwhalecloud.bote.doc.module.document.entity.DocumentExportSnapshotEntity;

/**
 * 文档导出快照service
 *
 * @author Aiqing
 * @since 2025/9/29
 */
public interface IDocumentExportSnapshotService {

  /**
   * 保存导出快照记录
   *
   * @param exportSnapshotEntity 导出信息
   */
  void insert(DocumentExportSnapshotEntity exportSnapshotEntity);

  /**
   * 查询指定版本的导出快照
   *
   * @param documentId 文档ID
   * @param revision 文档版本号
   * @return 导出记录
   */
  DocumentExportSnapshotEntity selectByDocumentIdAndRevision(String documentId, Long revision, String extension);
}
