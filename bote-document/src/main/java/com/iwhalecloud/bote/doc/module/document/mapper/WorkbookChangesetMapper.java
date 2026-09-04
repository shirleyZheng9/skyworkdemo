package com.iwhalecloud.bote.doc.module.document.mapper;

import com.iwhalecloud.bote.doc.module.document.entity.WorkbookChangesetEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 表格变更集相关数据库操作
 *
 * @author Aiqing
 * @since 2025-08-15
 */
public interface WorkbookChangesetMapper {
  /**
   * 插入记录
   */
  int insert(@Param("entity") WorkbookChangesetEntity entity);

  /**
   * 逻辑删除
   */
  int deleteByPrimaryKey(@Param("id") Long id);

  /**
   * 查询某版本后的变更集
   *
   * @param documentId 文档ID
   * @param revision 版本号
   * @return 变更集
   */
  List<WorkbookChangesetEntity> selectByDocumentIdAndRevision(@Param("documentId") String documentId,
                                                              @Param("revision") Long revision);

  /**
   * 查询当前最大的版本号
   *
   * @param documentId 文档ID
   * @return 版本号
   */
  Long selectMaxRevision(@Param("documentId") String documentId);
}
