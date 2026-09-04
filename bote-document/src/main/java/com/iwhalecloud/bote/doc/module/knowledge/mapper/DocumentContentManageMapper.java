package com.iwhalecloud.bote.doc.module.knowledge.mapper;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentContentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocumentContentQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 文档内容管理
 *
 * @author auto
 * @since 2025-01-15
 */
public interface DocumentContentManageMapper {

  /**
   * 根据主键获取文档内容
   *
   * @param contentId 文档内容主键
   * @return 文档内容
   */
  DocumentContentDTO getDocumentContent(@Param("id") Long contentId);

  /**
   * 新增文档内容
   *
   * @param documentContent 文档内容
   * @return 结果
   */
  int insertDocumentContent(@Param("dto") DocumentContentDTO documentContent);

  /**
   * 批量新增文档内容
   *
   * @param documentContents 文档内容列表
   * @return 结果
   */
  int batchInsertDocumentContent(@Param("list") List<DocumentContentDTO> documentContents);

  /**
   * 修改文档内容
   *
   * @param documentContent 文档内容
   * @return 结果
   */
  int updateDocumentContent(@Param("dto") DocumentContentDTO documentContent);

  /**
   *
   * 批量修改文档内哦让那个
   *
   * @param dto 文档内容
   * @return 结果
   */
  int batchUpdateDocumentContent(@Param("dto") DocumentContentDTO dto);

  /**
   * 根据文档ID删除内容
   */
  int deleteDocumentContentByDocumentId(@Param("tenantId") Long tenantId, @Param("documentId") Long documentId, @Param("updatorId") Long updatorId);

  /**
   * 批量删除内容
   */
  int batchDeleteDocumentContent(@Param("tenantId") Long tenantId, @Param("dto") DocumentDTO document, @Param("updatorId") Long updatorId);

  /**
   * 获取文档内容列表
   *
   * @param queryParams 查询条件
   * @return 文档内容列表
   */
  List<DocumentContentDTO> selectDocumentContentList(@Param("query") DocumentContentQueryParams queryParams);

  /**
   * 获取文档内容列表（分页）
   *
   * @param queryParams 查询条件
   * @return 文档内容分页列表
   */
  Page<DocumentContentDTO> selectDocumentContentPage(@Param("query") DocumentContentQueryParams queryParams, RowBounds rowBounds);
}
