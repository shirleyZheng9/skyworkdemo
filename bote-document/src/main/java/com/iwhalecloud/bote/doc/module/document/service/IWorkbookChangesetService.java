package com.iwhalecloud.bote.doc.module.document.service;

import com.iwhalecloud.bote.doc.module.document.entity.WorkbookChangesetEntity;
import java.util.List;

/**
 * 在线表格变更集服务类
 *
 * @author Aiqing
 * @since 2025/9/2
 */
public interface IWorkbookChangesetService {

  /**
   * 查询表格的变更集
   *
   * @param documentId 文档ID
   * @param revision 基础版本
   * @return 变更集
   */
  List<WorkbookChangesetEntity> selectByDocumentIdAndRevision(String documentId, Long revision);

  /**
   * 保存changeset
   *
   * @param changesetEntity 实体信息
   */
  void save(WorkbookChangesetEntity changesetEntity);


  Long getCurrentRevision(String documentId);

  List<WorkbookChangesetEntity> selectByDocumentIdAndRevisionRange(String documentId, Long expectRevision, Long currentRevision);
}
