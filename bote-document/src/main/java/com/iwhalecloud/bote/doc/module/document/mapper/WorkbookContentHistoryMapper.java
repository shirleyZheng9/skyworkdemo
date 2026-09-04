package com.iwhalecloud.bote.doc.module.document.mapper;

import com.iwhalecloud.bote.doc.module.document.dto.WorkbookContentHistoryDTO;
import com.iwhalecloud.bote.doc.module.document.entity.WorkbookContentHistoryEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 工作簿内容历史表数据库操作
 *
 * @author system
 * @since 2025-09-26
 */
public interface WorkbookContentHistoryMapper {

  /**
   * 根据文档ID查询历史记录列表
   *
   * @param documentId 文档ID
   * @return 工作簿内容历史列表
   */
  List<WorkbookContentHistoryEntity> selectByDocumentId(@Param("documentId") String documentId);

  /**
   * 根据文档ID和版本号查询
   *
   * @param documentId 文档ID
   * @param revision 版本号
   * @return 工作簿内容历史实体
   */
  WorkbookContentHistoryEntity selectByDocumentIdAndRevision(@Param("documentId") String documentId, @Param("revision") Long revision);

  /**
   * 根据文档ID查询最大版本号的历史记录
   *
   * @param documentId 文档ID
   * @return 最大版本号的工作簿内容历史实体，如果没有则返回null
   */
  WorkbookContentHistoryEntity selectByMaxRevisionAndDocumentId(@Param("documentId") String documentId);

  /**
   * 插入记录
   *
   * @param entity 工作簿内容历史实体
   * @return 影响行数
   */
  int insert(@Param("entity") WorkbookContentHistoryEntity entity);

  /**
   * 逻辑删除
   *
   * @param id 主键ID
   * @return 影响行数
   */
  int deleteByPrimaryKey(@Param("id") Long id);

  /**
   * 根据文档ID逻辑删除历史记录
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
   * 根据文档ID和版本号删除小于指定版本的历史记录
   *
   * @param documentId 文档ID
   * @param maxRevision 最大保留版本号（删除小于此版本的记录）
   * @return 影响行数
   */
  int deleteByDocumentIdAndRevisionLessThan(@Param("documentId") String documentId, @Param("maxRevision") Long maxRevision);

  /**
   * 根据文档ID查询历史版本总数
   *
   * @param documentId 文档ID
   * @return 历史版本总数
   */
  Long countByDocumentId(@Param("documentId") String documentId);

  /**
   * 根据文档ID和版本号查询小于指定版本的版本号列表
   *
   * @param documentId 文档ID
   * @param maxRevision 最大保留版本号（查询小于此版本的版本号）
   * @return 版本号列表
   */
  List<Long> selectRevisionsByDocumentIdAndRevisionLessThan(@Param("documentId") String documentId, @Param("maxRevision") Long maxRevision);

  /**
   * 查询在线文档的历史列表
   * @param documentId 文档id
   * @return 返回在线文档的历史列表
   */
  List<WorkbookContentHistoryDTO> getWorkbookVersions(@Param("documentId")String documentId);

  /**
   * 查询历史
   * @param id 历史表主键
   * @return 返回历史详情
   */
  WorkbookContentHistoryEntity selectByVersionId(@Param("id") Long id);
}
