package com.iwhalecloud.bote.doc.module.knowledge.mapper;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentParameterDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocumentContentQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 参数管理
 *
 * @author auto
 * @since 2025-01-14
 */
public interface DocumentParameterManageMapper {

  /**
   * 新增参数
   */
  int insertDocumentParameter(@Param("dto") DocumentParameterDTO parameter);

  /**
   * 批量新增参数
   */
  int batchInsertDocumentParameter(@Param("list") List<DocumentParameterDTO> parameters);

  /**
   * 修改参数
   */
  int updateDocumentParameter(@Param("dto") DocumentParameterDTO parameter);

  /**
   * 根据文档ID删除参数
   */
  int deleteDocumentParameterByDocumentId(@Param("tenantId") Long tenantId, @Param("documentId") Long documentId, @Param("updatorId") Long updatorId);

  /**
   * 查询取参数列表
   *
   * @param documentId 文档ID
   * @param tenantId 租户 ID
   * @return 参数列表
   */
  List<DocumentParameterDTO> selectDocumentParameterList(@Param("documentId") Long documentId, @Param("tenantId") Long tenantId);

  /**
   * 查询取参数列表
   *
   * @param queryParams 查询参数
   * @return 结果
   */
  List<DocumentParameterDTO> selectParameterList(@Param("query") DocumentContentQueryParams queryParams);

  /**
   * 查询参数列表分页
   *
   * @param queryParams 查询参数
   * @param rowBounds 分页参数
   * @return 结果
   */
  Page<DocumentParameterDTO> selectDocumentParameterPage(@Param("query") DocumentContentQueryParams queryParams, RowBounds rowBounds);
}

