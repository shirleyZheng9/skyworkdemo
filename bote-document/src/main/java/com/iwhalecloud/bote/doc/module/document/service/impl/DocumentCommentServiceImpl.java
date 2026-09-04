package com.iwhalecloud.bote.doc.module.document.service.impl;

import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.cache.DcDocumentNodeCache;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.exception.DocumentNodeAccessDenyException;
import com.iwhalecloud.bote.doc.common.exception.NodeNotExistException;
import com.iwhalecloud.bote.doc.common.exception.NodeOperationDeniedException;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.consts.DocumentPermConsts;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRole;
import com.iwhalecloud.bote.doc.module.control.model.NodeBaseInfoDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentCommentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.request.AddCommentRequest;
import com.iwhalecloud.bote.doc.module.document.dto.request.QueryCommentsRequest;
import com.iwhalecloud.bote.doc.module.document.dto.request.ReviewCommentRequest;
import com.iwhalecloud.bote.doc.module.document.dto.request.UpdateCommentRequest;
import com.iwhalecloud.bote.doc.module.document.entity.DocumentCommentEntity;
import com.iwhalecloud.bote.doc.module.document.enums.RecordTypeEnum;
import com.iwhalecloud.bote.doc.module.document.enums.ReviewStatusEnum;
import com.iwhalecloud.bote.doc.module.document.mapper.DocumentCommentMapper;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentCommentService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.BeanUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 文档评论服务实现类
 *
 * @author Aiqing
 * @since 2025-10-16
 */
@Service
@RequiredArgsConstructor
public class DocumentCommentServiceImpl implements IDocumentCommentService {

  private final DocumentCommentMapper documentCommentMapper;

  private final DcDocumentNodeCache dcDocumentNodeCache;

  private final ControlTemplate controlTemplate;

  private final IDcUserService dcUserService;

  @Override
  @Transactional
  public DocumentCommentDTO addComment(AddCommentRequest request, Long userId) {
    validateAddCommentRequest(request, userId);
    String documentId = request.getDocumentId();
    NodeBaseInfoDTO nodeInfo = dcDocumentNodeCache.getDocumentNodeInfo(documentId);
    if (nodeInfo == null) {
      throw new NodeNotExistException();
    }
    String recordType = request.getRecordType();
    boolean isCorrection = isCorrection(recordType);

    ControlRole controlRole = checkPermissions(request, userId, nodeInfo, isCorrection);

    DocumentCommentDTO commentDTO = documentCommentMapper.selectByUuid(request.getUuid(), request.getDocumentId());
    DocumentCommentDTO result = commentDTO != null ? handleExistingComment(commentDTO, request, userId) : createNewComment(request, userId, documentId, recordType, isCorrection);

    fillUserInfo(Collections.singletonList(result));
    setCommentPermissions(result, controlRole, userId);
    return result;
  }

  private void validateAddCommentRequest(AddCommentRequest request, Long userId) {
    Assert.notNull(request, "请求参数不能为空");
    Assert.notNull(userId, "用户ID不能为空");
    if (StringUtils.isBlank(request.getDocumentId())) {
      throw new BssException("文档ID不能为空");
    }
    if (StringUtils.isBlank(request.getRecordType())) {
      throw new BssException("记录类型不能为空");
    }
    if (StringUtils.isBlank(request.getUuid())) {
      throw new BssException("修订的uuid不能为空！");
    }
  }

  private boolean isCorrection(String recordType) {
    return Objects.equals(recordType, RecordTypeEnum.CORRECTION.getCode());
  }

  private ControlRole checkPermissions(AddCommentRequest request, Long userId, NodeBaseInfoDTO nodeInfo, boolean isCorrection) {
    ControlRole controlRole = controlTemplate.fetchNodeRole(nodeInfo.getLibraryId(), userId, request.getDocumentId());
    if (isCorrection) {
      if (StringUtils.isBlank(request.getCorrectionType())) {
        throw new BssException("修订的类型不能为空！");
      }
      if (!controlRole.hasPermission(NodePermission.COMMENT_NODE)) {
        throw new NodeOperationDeniedException();
      }
    }
    else if (!controlRole.hasPermission(NodePermission.CONTENT_CORRECTION)) {
      throw new NodeOperationDeniedException();
    }
    return controlRole;
  }

  private DocumentCommentDTO handleExistingComment(DocumentCommentDTO existing, AddCommentRequest request, Long userId) {
    if (StringUtils.isEmpty(request.getContent())) {
      documentCommentMapper.deleteById(existing.getCommentId(), userId);
      return existing;
    }
    DocumentCommentEntity comment = new DocumentCommentEntity();
    comment.setCommentId(existing.getCommentId());
    comment.setContent(request.getContent());
    comment.setEdit(DocBaseConsts.TRUE);
    comment.setUpdatorId(userId);
    documentCommentMapper.updateContent(comment);

    existing.setContent(request.getContent());
    existing.setEdit(DocBaseConsts.TRUE);
    existing.setUpdatorId(userId);
    return existing;
  }

  private DocumentCommentDTO createNewComment(AddCommentRequest request, Long userId, String documentId, String recordType, boolean isCorrection) {

    DocumentCommentEntity comment = new DocumentCommentEntity();
    comment.setCommentId(IDUtils.nextId());
    comment.setDocumentId(documentId);
    comment.setUuid(request.getUuid());
    comment.setContent(request.getContent());
    comment.setOldContent(request.getOldContent());
    comment.setRecordType(recordType);
    comment.setCorrectionType(request.getCorrectionType());
    comment.setCorrectionReason(request.getCorrectionReason());
    comment.setReviewStatus(isCorrection ? ReviewStatusEnum.PENDING.getCode() : ReviewStatusEnum.APPROVED.getCode());
    comment.setParentId(request.getParentId());
    comment.setCreatorId(userId);
    comment.setCreatedTime(new Date());
    comment.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    if (StringUtils.isNotEmpty(request.getContent())) {
      int result = documentCommentMapper.insert(comment);
      if (result <= 0) {
        throw new BssException("新增修订记录失败");
      }
    }
    return BeanUtil.copy(comment, DocumentCommentDTO.class);
  }

  @Override
  @Transactional
  public Boolean updateComment(UpdateCommentRequest request, Long userId) {
    Assert.notNull(request, "请求参数不能为空");
    Assert.notNull(userId, "用户ID不能为空");

    // 检查评论是否存在
    DocumentCommentDTO existingComment = documentCommentMapper.selectById(request.getCommentId());
    if (existingComment == null) {
      throw new BssException("修订记录不存在");
    }

    // 检查权限（只有创建人可以编辑）- 校验后无需再次校验文档权限
    if (!existingComment.getCreatorId().equals(userId)) {
      throw new BssException("无权限编辑此修订记录");
    }
    if (Objects.equals(existingComment.getRecordType(), RecordTypeEnum.CORRECTION.getCode())
      && !Objects.equals(existingComment.getReviewStatus(), ReviewStatusEnum.PENDING.getCode())) {
      throw new BssException("修订已审批，无法再修改");
    }

    // 构建更新实体
    DocumentCommentEntity comment = new DocumentCommentEntity();
    comment.setCommentId(request.getCommentId());
    comment.setContent(request.getContent());
    comment.setUpdatorId(userId);

    // 更新数据库
    int result = documentCommentMapper.updateContent(comment);
    if (result <= 0) {
      throw new BssException("编辑修订记录失败");
    }

    return true;
  }

  @Override
  @Transactional
  public Boolean deleteComment(@NotEmpty String documentId, Long commentId, Long userId) {
    Assert.notNull(commentId, "评论ID不能为空");
    Assert.notNull(userId, "用户ID不能为空");

    // 检查评论是否存在
    DocumentCommentDTO existingComment = documentCommentMapper.selectById(commentId);
    if (existingComment == null) {
      throw new BssException("修订记录不存在");
    }
    Assert.isTrue(Objects.equals(documentId, existingComment.getDocumentId()), "参数非法，文档ID不一致");

    // 检查权限（只有创建人可以删除）
    if (!existingComment.getCreatorId().equals(userId)) {
      throw new BssException("无权限删除此修订记录");
    }

    // 逻辑删除
    int result = documentCommentMapper.deleteById(commentId, userId);
    if (result <= 0) {
      throw new BssException("删除修订记录失败");
    }

    return true;
  }

  @Override
  @Transactional
  public NodeBaseInfoDTO approveComment(ReviewCommentRequest request, Long userId) {
    // 前置检查
    DocumentCommentDTO commentDTO = checkCorrectionApprove(request, userId);

    // 构建更新实体
    DocumentCommentEntity comment = new DocumentCommentEntity();
    comment.setCommentId(request.getCommentId());
    comment.setReviewStatus(ReviewStatusEnum.APPROVED.getCode());
    comment.setReviewerId(userId);
    comment.setReviewTime(new Date());
    comment.setUpdatorId(userId);

    // 更新数据库
    int result = documentCommentMapper.updateReviewStatus(comment);
    if (result <= 0) {
      throw new BssException("审批修订记录失败");
    }

    return dcDocumentNodeCache.getDocumentNodeInfo(commentDTO.getDocumentId());
  }

  @Override
  @Transactional
  public Boolean rejectComment(ReviewCommentRequest request, Long userId) {
    // 前置检查
    checkCorrectionApprove(request, userId);

    // 构建更新实体
    DocumentCommentEntity comment = new DocumentCommentEntity();
    comment.setCommentId(request.getCommentId());
    comment.setReviewStatus(ReviewStatusEnum.REJECTED.getCode());
    comment.setReviewerId(userId);
    comment.setReviewTime(new Date());
    comment.setUpdatorId(userId);

    // 更新数据库
    int result = documentCommentMapper.updateReviewStatus(comment);
    if (result <= 0) {
      throw new BssException("拒绝修订记录失败");
    }

    return true;
  }

  @Override
  public List<DocumentCommentDTO> queryCorrections(String documentId, List<String> uuids) {
    Assert.notEmpty(uuids, "UUID列表不能为空");
    NodeBaseInfoDTO nodeInfo = dcDocumentNodeCache.getDocumentNodeInfo(documentId);
    if (nodeInfo == null) {
      throw new NodeNotExistException();
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    controlTemplate.checkNodePermission(nodeInfo.getLibraryId(), userId, documentId, NodePermission.READ_NODE,
      DocumentPermConsts.ACCESS_DENIED_CALLBACK);

    List<DocumentCommentDTO> corrections = documentCommentMapper.selectByUuids(uuids);

    // 填充用户信息
    fillUserInfo(corrections);
    return corrections;
  }

  @Override
  public List<DocumentCommentDTO> queryComments(QueryCommentsRequest request) {
    Assert.notNull(request, "请求参数不能为空");
    String documentId = request.getDocumentId();
    Assert.hasText(documentId, "文档ID不能为空");

    NodeBaseInfoDTO nodeInfo = dcDocumentNodeCache.getDocumentNodeInfo(documentId);
    if (nodeInfo == null) {
      throw new NodeNotExistException();
    }
    String libraryId = nodeInfo.getLibraryId();
    Long userId = SessionUtil.getLoginInfo().getUserId();
    ControlRole controlRole = controlTemplate.fetchNodeRole(libraryId, userId, documentId);
    boolean hasPermission = controlRole.hasPermission(NodePermission.READ_NODE);
    if (!hasPermission) {
      throw new DocumentNodeAccessDenyException();
    }

    // 查询评论列表
    List<DocumentCommentDTO> comments = documentCommentMapper.selectByDocumentId(request);

    // 填充用户信息
    fillUserInfo(comments);

    // 为每个评论设置权限信息
    for (DocumentCommentDTO comment : comments) {
      setCommentPermissions(comment, controlRole, userId);
    }

    return comments;
  }

  private DocumentCommentDTO checkCorrectionApprove(ReviewCommentRequest request, Long userId) {
    // 检查评论是否存在
    DocumentCommentDTO existingComment = documentCommentMapper.selectById(request.getCommentId());
    if (existingComment == null) {
      throw new BssException("修订记录不存在");
    }

    // 检查是否为修订记录
    if (!RecordTypeEnum.CORRECTION.getCode().equals(existingComment.getRecordType())) {
      throw new BssException("只能审核修订记录");
    }

    // 检查当前状态
    if (!ReviewStatusEnum.PENDING.getCode().equals(existingComment.getReviewStatus())) {
      throw new BssException("只能审核待审核状态的修订记录");
    }

    String documentId = existingComment.getDocumentId();
    NodeBaseInfoDTO nodeInfo = dcDocumentNodeCache.getDocumentNodeInfo(documentId);
    if (nodeInfo == null) {
      throw new NodeNotExistException();
    }
    String libraryId = nodeInfo.getLibraryId();
    controlTemplate.checkNodePermission(libraryId, userId, documentId, NodePermission.APPROVAL_CORRECTION, has -> {
      if (Boolean.FALSE.equals(has)) {
        throw new BssException("无审批修订权限");
      }
    });
    return existingComment;
  }

  /**
   * 设置评论权限信息
   *
   * @param comment 评论信息
   */
  private void setCommentPermissions(DocumentCommentDTO comment, ControlRole controlRole, Long userId) {
    // 只有创建人可以编辑和删除，管理员可以审核
    boolean isCreator = Objects.equals(userId, comment.getCreatorId());
    comment.setCanEdit(isCreator);
    comment.setCanDelete(isCreator || controlRole.hasPermission(NodePermission.MANAGE_NODE));
    comment.setCanReview(controlRole.hasPermission(NodePermission.APPROVAL_CORRECTION));
  }

  private void fillUserInfo(List<DocumentCommentDTO> commentDTOList) {
    if (commentDTOList == null || commentDTOList.isEmpty()) {
      return;
    }

    List<Long> userIdList = new ArrayList<>();
    commentDTOList.forEach(comment -> {
      userIdList.add(comment.getCreatorId());
      if (comment.getReviewerId() != null) {
        userIdList.add(comment.getReviewerId());
      }
    });

    if (userIdList.isEmpty()) {
      return;
    }

    Map<Long, PortalUserDTO> userDTOMap = dcUserService.findUserMapBatchByIds(userIdList);

    // 填充用户信息
    commentDTOList.forEach(comment -> {
      // 填充创建人信息
      PortalUserDTO creator = userDTOMap.get(comment.getCreatorId());
      if (creator != null) {
        comment.setCreatorName(creator.getUserName());
      }

      // 填充审核人信息
      if (comment.getReviewerId() != null) {
        PortalUserDTO reviewer = userDTOMap.get(comment.getReviewerId());
        if (reviewer != null) {
          comment.setReviewerName(reviewer.getUserName());
        }
      }
    });
  }
}
