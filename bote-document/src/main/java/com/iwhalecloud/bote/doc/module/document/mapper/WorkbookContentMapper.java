package com.iwhalecloud.bote.doc.module.document.mapper;

import com.iwhalecloud.bote.doc.module.document.entity.WorkbookContentEntity;
import org.apache.ibatis.annotations.Param;

/**
 * 工作簿内容表数据库操作
 *
 * @author system
 * @since 2025-09-26
 */
public interface WorkbookContentMapper {

  /**
   * 根据主键查询
   *
   * @param id 主键ID
   * @return 工作簿内容实体
   */
  WorkbookContentEntity selectByPrimaryKey(@Param("id") Long id);

  /**
   * 根据文档ID查询
   *
   * @param documentId 文档ID
   * @return 工作簿内容实体
   */
  WorkbookContentEntity selectByDocumentId(@Param("documentId") String documentId);

  /**
   * 根据文档ID和版本号查询
   *
   * @param documentId 文档ID
   * @param revision 版本号
   * @return 工作簿内容实体
   */
  WorkbookContentEntity selectByDocumentIdAndRevision(@Param("documentId") String documentId, @Param("revision") Long revision);

  /**
   * 插入记录
   *
   * @param entity 工作簿内容实体
   * @return 影响行数
   */
  int insert(@Param("entity") WorkbookContentEntity entity);

  /**
   * 更新记录
   *
   * @param entity 工作簿内容实体
   * @return 影响行数
   */
  int updateByPrimaryKey(@Param("entity") WorkbookContentEntity entity);

  /**
   * 根据文档ID更新
   *
   * @param entity 工作簿内容实体
   * @return 影响行数
   */
  int updateByDocumentId(@Param("entity") WorkbookContentEntity entity);

  /**
   * 查询表格内容最后版本号
   *
   * @param documentId 文档ID
   * @return 版本号
   */
  Long selectLastRevision(@Param("documentId") String documentId);
}
