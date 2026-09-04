package com.iwhalecloud.bote.doc.module.document.mapper;

import com.iwhalecloud.bote.doc.module.document.entity.WorkbookSnapshotEntity;
import org.apache.ibatis.annotations.Param;

/**
 * 表格快照相关数据库操作
 *
 * @author Aiqing
 * @since 2025-08-15
 */
public interface WorkbookSnapshotMapper {

  /**
   * 根据主键查询
   */
  WorkbookSnapshotEntity selectByPrimaryKey(@Param("id") Long id);

  /**
   * 插入记录
   */
  int insert(@Param("entity") WorkbookSnapshotEntity entity);

  /**
   * 逻辑删除
   */
  int deleteByPrimaryKey(@Param("id") Long id);

  /**
   * 查询最后版本的快照记录ID
   *
   * @param documentId 文档ID
   * @return 记录ID
   */
  Long queryLastRevision(@Param("documentId") String documentId);

  /**
   * 根据版本号和文档ID查询快照记录
   *
   * @param documentId 文档ID
   * @param revision 版本号
   * @return 记录
   */
  WorkbookSnapshotEntity selectByRevisionAndDocumentId(@Param("documentId") String documentId,
                                                       @Param("revision") Long revision);
}
