package com.iwhalecloud.bote.doc.module.document.mapper;

import com.iwhalecloud.bote.doc.module.document.entity.DocumentExportSnapshotEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 文档导出快照表数据库操作
 *
 * @author system
 * @since 2025-09-26
 */
public interface DocumentExportSnapshotMapper {

  /**
   * 根据文档ID查询快照列表
   *
   * @param documentId 文档ID
   * @return 文档导出快照列表
   */
  List<DocumentExportSnapshotEntity> selectByDocumentId(@Param("documentId") String documentId);

  /**
   * 根据文档ID、版本号和文件扩展名查询
   *
   * @param documentId 文档ID
   * @param revision 版本号
   * @param fileExtension 文件扩展名
   * @return 文档导出快照实体
   */
  DocumentExportSnapshotEntity selectByDocumentIdAndRevisionAndExtension(@Param("documentId") String documentId,
                                                                         @Param("revision") Long revision,
                                                                         @Param("fileExtension") String fileExtension);

  /**
   * 根据文件ID查询快照列表
   *
   * @param fileId 文件ID
   * @return 文档导出快照列表
   */
  List<DocumentExportSnapshotEntity> selectByFileId(@Param("fileId") Long fileId);

  /**
   * 插入记录
   *
   * @param entity 文档导出快照实体
   * @return 影响行数
   */
  int insert(@Param("entity") DocumentExportSnapshotEntity entity);

  /**
   * 更新记录
   *
   * @param entity 文档导出快照实体
   * @return 影响行数
   */
  int updateByPrimaryKey(@Param("entity") DocumentExportSnapshotEntity entity);

  /**
   * 逻辑删除
   *
   * @param id 主键ID
   * @return 影响行数
   */
  int deleteByPrimaryKey(@Param("id") Long id);

  /**
   * 根据文档ID逻辑删除快照记录
   *
   * @param documentId 文档ID
   * @return 影响行数
   */
  int deleteByDocumentId(@Param("documentId") String documentId);

  /**
   * 根据文档ID查询最近15条记录的最小版本号
   *
   * @param documentId 文档ID
   * @return 最小版本号，如果没有记录则返回null
   */
  Long selectMinRevisionByDocumentId(@Param("documentId") String documentId);

  /**
   * 根据文档ID和版本号查询小于指定版本的快照记录
   *
   * @param documentId 文档ID
   * @param maxRevision 最大保留版本号（查询小于此版本的记录）
   * @return 快照记录列表
   */
  List<DocumentExportSnapshotEntity> selectByDocumentIdAndRevisionLessThan(@Param("documentId") String documentId, @Param("maxRevision") Long maxRevision);

  /**
   * 根据文档ID和版本号删除小于指定版本的快照记录
   *
   * @param documentId 文档ID
   * @param maxRevision 最大保留版本号（删除小于此版本的记录）
   * @return 影响行数
   */
  int deleteByDocumentIdAndRevisionLessThan(@Param("documentId") String documentId, @Param("maxRevision") Long maxRevision);

  /**
   * 根据文档ID和版本号列表查询快照记录
   *
   * @param documentId 文档ID
   * @param revisions 版本号列表
   * @return 快照记录列表
   */
  List<DocumentExportSnapshotEntity> selectByDocumentIdAndRevisions(@Param("documentId") String documentId, @Param("revisions") List<Long> revisions);

  /**
   * 根据文档ID和版本号列表删除快照记录
   *
   * @param documentId 文档ID
   * @param revisions 版本号列表
   * @return 影响行数
   */
  int deleteByDocumentIdAndRevisions(@Param("documentId") String documentId, @Param("revisions") List<Long> revisions);

}
