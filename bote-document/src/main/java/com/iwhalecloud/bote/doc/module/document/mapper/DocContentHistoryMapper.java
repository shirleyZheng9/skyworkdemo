package com.iwhalecloud.bote.doc.module.document.mapper;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.doc.module.document.dto.DocContentHistoryDTO;
import com.iwhalecloud.bote.doc.module.document.entity.DocContentHistoryEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 文档版本相关数据库操作
 *
 * @author yangran
 * @since 2025-08-13
 */
public interface DocContentHistoryMapper {

  /**
   * 根据主键查询
   */
  DocContentHistoryEntity selectByVersionId(@Param("versionId") Long versionId);

  /**
   * 根据文档ID和版本号查询
   *
   * @param documentId 文档ID
   * @param revision 版本号
   * @return 文档内容历史实体
   */
  DocContentHistoryEntity selectByDocumentIdAndRevision(@Param("documentId") String documentId, @Param("revision") Long revision);

  /**
   * 插入记录
   */
  int insert(@Param("version") DocContentHistoryEntity version);

  /**
   * 更新记录
   */
  int update(@Param("version") DocContentHistoryEntity version);

  /**
   * 逻辑删除
   */
  int deleteByVersionId(@Param("versionId") Long versionId);

  /**
   * 查询最新的一个版本内容
   *
   * @param documentId 文档ID
   * @return 文档历史
   */
  Page<DocContentHistoryDTO> selectLastDocHistory(@Param("documentId") String documentId, RowBounds rowBounds);

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

  List<DocContentHistoryDTO> getDocumentContentVersions(@Param("documentId")String documentId);

  DocContentHistoryDTO getDocumentContentVersion(@Param("documentId")String documentId, @Param("id") Long id);

}
