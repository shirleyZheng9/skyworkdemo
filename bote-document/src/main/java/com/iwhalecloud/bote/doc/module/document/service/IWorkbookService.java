package com.iwhalecloud.bote.doc.module.document.service;

import com.iwhalecloud.bote.doc.module.document.entity.WorkbookSnapshotEntity;
import com.iwhalecloud.bote.doc.module.document.entity.WorksheetEntity;
import java.util.List;

/**
 * 表格快照 Service 接口
 *
 * @author Aiqing
 * @since 2025-08-15
 */
public interface IWorkbookService {

  /**
   * 根据ID查询表格快照
   *
   * @param id 快照ID
   * @return 表格快照信息
   */
  WorkbookSnapshotEntity getSnapshotById(Long id);

  /**
   * 创建表格快照
   *
   * @param snapshot 表格快照信息
   * @return 创建结果
   */
  Long createSnapshot(WorkbookSnapshotEntity snapshot);

  /**
   * 创建Sheet页
   *
   * @param worksheet Sheet页信息
   * @return 创建结果
   */
  Long createWorksheet(WorksheetEntity worksheet);


  /**
   * 根据快照ID查询sheet信息
   *
   * @param documentId 文档ID
   * @param snapshotId 快照ID
   * @return sheet信息
   */
  List<WorksheetEntity> selectWorkbookSheetsBySnapshotId(String documentId, Long snapshotId);


  /**
   * 查询最后一个版本的快照记录
   *
   * @param documentId 文档ID
   * @return 快照记录
   */
  WorkbookSnapshotEntity queryLastRevisionSnapshot(String documentId);

  /**
   * 查询sheet数据
   *
   * @param documentId 文档ID
   * @param blockId 数据ID
   * @return sheet数据
   */
  WorksheetEntity queryWorksheetByDocumentIdAndBlockId(String documentId, Long blockId);


}
