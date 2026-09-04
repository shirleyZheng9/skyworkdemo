package com.iwhalecloud.bote.doc.module.person.mapper;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.doc.module.person.dto.share.MyShareDTO;
import com.iwhalecloud.bote.doc.module.person.dto.share.SharedTargetDTO;
import com.iwhalecloud.bote.doc.module.person.dto.share.SharedWithMeDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 共享模块 Mapper
 *
 * @author lizuyin
 * @since 2025-08-20
 */
public interface ShareMapper {

  /**
   * 查询与我共享 只查询直接共享给当前用户的文档：subject_id = currentUserId and subject_type = 'USER'
   */
  Page<SharedWithMeDTO> selectSharedWithMe(@Param("userId") Long userId, @Param("fileType") String fileType,
    @Param("sortBy") String sortBy, @Param("sortOrder") String sortOrder, RowBounds rowBounds,
    @Param("spaceId") Long spaceId, @Param("tenantId") Long tenantId, @Param("platform") String platform);

  /**
   * 查询指定文档的分享对象列表
   */
  List<MyShareDTO> selectSharedTargets(@Param("documentId") String documentId, @Param("userId") Long userId);

  /**
   * 查询指定文档的分享对象列表（用于组装接口出参的 sharedTo）
   */
  List<SharedTargetDTO> selectSharedTargetsForDTO(@Param("documentId") String documentId, @Param("userId") Long userId);

  /**
   * 查询我共享的文档（分页）- 第一阶段查询 以document_id为维度进行分页去重，包含文档基本信息、置顶/收藏状态
   */
  Page<MyShareDTO> selectMyShareDocuments(@Param("userId") Long userId, @Param("fileType") String fileType,
    @Param("sortBy") String sortBy, @Param("sortOrder") String sortOrder, RowBounds rowBounds,
    @Param("spaceId") Long spaceId, @Param("tenantId") Long tenantId, @Param("platform") String platform);

  /**
   * 查询指定文档列表的所有授权记录 - 第二阶段查询 根据documentId列表查询该用户分享给的所有权限记录（不分页，不去重）
   */
  List<MyShareDTO> selectPermissionsByDocumentIds(@Param("documentIds") List<String> documentIds,
    @Param("userId") Long userId);
}


