package com.iwhalecloud.bote.doc.module.document.mapper;

import com.iwhalecloud.bote.doc.module.document.dto.DocumentCommentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.request.QueryCommentsRequest;
import com.iwhalecloud.bote.doc.module.document.entity.DocumentCommentEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 文档评论Mapper接口
 *
 * @author Aiqing
 * @since 2025-10-16
 */
public interface DocumentCommentMapper {

  /**
   * 插入评论记录
   *
   * @param comment 评论实体
   * @return 影响行数
   */
  int insert(@Param("comment") DocumentCommentEntity comment);

  /**
   * 根据ID查询评论
   *
   * @param commentId 评论ID
   * @return 评论信息
   */
  DocumentCommentDTO selectById(@Param("commentId") Long commentId);

  /**
   * 根据ID查询评论
   *
   * @param uuid 生成的唯一id
   * @return 评论信息
   */
  DocumentCommentDTO selectByUuid(@Param("uuid") String uuid, @Param("documentId") String documentId);

  /**
   * 更新评论内容
   *
   * @param comment 评论实体
   * @return 影响行数
   */
  int updateContent(@Param("comment") DocumentCommentEntity comment);

  /**
   * 更新审核状态
   *
   * @param comment 评论实体
   * @return 影响行数
   */
  int updateReviewStatus(@Param("comment") DocumentCommentEntity comment);

  /**
   * 逻辑删除评论
   *
   * @param commentId 评论ID
   * @param updatorId 更新人ID
   * @return 影响行数
   */
  int deleteById(@Param("commentId") Long commentId, @Param("updatorId") Long updatorId);

  /**
   * 根据文档ID查询评论列表
   *
   * @param request 查询请求
   * @return 评论列表
   */
  List<DocumentCommentDTO> selectByDocumentId(@Param("request") QueryCommentsRequest request);

  /**
   * 根据UUID列表查询修正记录
   *
   * @param uuids UUID列表
   * @return 修正记录列表
   */
  List<DocumentCommentDTO> selectByUuids(@Param("uuids") List<String> uuids);

  /**
   * 查询评论总数
   *
   * @param request 查询请求
   * @return 总数
   */
  long countByDocumentId(@Param("request") QueryCommentsRequest request);

  /**
   * 根据父ID查询子评论
   *
   * @param parentId 父评论ID
   * @return 子评论列表
   */
  List<DocumentCommentDTO> selectByParentId(@Param("parentId") Long parentId);
}
