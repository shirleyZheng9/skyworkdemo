package com.iwhalecloud.bote.doc.module.document.service;

import com.iwhalecloud.bote.doc.module.control.model.NodeBaseInfoDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentCommentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.request.AddCommentRequest;
import com.iwhalecloud.bote.doc.module.document.dto.request.QueryCommentsRequest;
import com.iwhalecloud.bote.doc.module.document.dto.request.ReviewCommentRequest;
import com.iwhalecloud.bote.doc.module.document.dto.request.UpdateCommentRequest;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 文档评论服务接口
 *
 * @author Aiqing
 * @since 2025-10-16
 */
public interface IDocumentCommentService {

  /**
   * 新增评论
   *
   * @param request 新增评论请求
   * @param userId 用户ID
   * @return 评论ID
   */
  DocumentCommentDTO addComment(AddCommentRequest request, Long userId);

  /**
   * 编辑评论
   *
   * @param request 编辑评论请求
   * @param userId 用户ID
   * @return 是否成功
   */
  Boolean updateComment(UpdateCommentRequest request, Long userId);

  /**
   * 删除评论
   *
   * @param commentId 评论ID
   * @param userId 用户ID
   * @return 是否成功
   */
  Boolean deleteComment(@NotEmpty String documentId, Long commentId, Long userId);

  /**
   * 审批修订记录
   *
   * @param request 审批请求
   * @param userId 用户ID
   * @return 是否成功
   */
  NodeBaseInfoDTO approveComment(ReviewCommentRequest request, Long userId);

  /**
   * 拒绝修订记录
   *
   * @param request 拒绝请求
   * @param userId 用户ID
   * @return 是否成功
   */
  Boolean rejectComment(ReviewCommentRequest request, Long userId);

  /**
   * 根据多个UUID查询修正记录
   *
   * @param uuids 查询参数
   * @return 修正记录列表
   */
  List<DocumentCommentDTO> queryCorrections(String documentId, List<String> uuids);

  /**
   * 查询所有的评论记录
   *
   * @param request 查询请求
   * @return 评论记录分页结果
   */
  List<DocumentCommentDTO> queryComments(QueryCommentsRequest request);

}
