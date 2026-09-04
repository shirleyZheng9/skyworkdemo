package com.iwhalecloud.bote.doc.module.document.mapper;

import com.iwhalecloud.bote.doc.module.document.entity.WorksheetEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * Sheet页相关数据库操作
 *
 * @author Aiqing
 * @since 2025-08-15
 */
public interface WorksheetMapper {
  /**
   * 插入记录
   */
  int insert(@Param("entity") WorksheetEntity entity);


  /**
   * 根据文档ID和快照ID查询sheet记录
   *
   * @param documentId 文档ID
   * @param snapshotId 快照ID
   * @return sheet记录
   */
  List<WorksheetEntity> selectWorkbookSheetsBySnapshotId(@Param("documentId") String documentId,
                                                         @Param("snapshotId") Long snapshotId);

  /**
   * 根据文档ID和sheet 数据块ID查询sheet记录
   *
   * @param documentId 文档ID
   * @param blockId 数据块ID
   * @return sheet数据
   */
  WorksheetEntity queryWorksheetByDocumentIdAndBlockId(@Param("documentId") String documentId,
                                                       @Param("blockId") Long blockId);

}
