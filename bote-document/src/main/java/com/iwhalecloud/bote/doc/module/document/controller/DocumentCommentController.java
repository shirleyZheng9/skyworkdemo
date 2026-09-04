package com.iwhalecloud.bote.doc.module.document.controller;

import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.common.lock.DistributedLock;
import com.iwhalecloud.bote.common.lock.DistributedLockFactory;
import com.iwhalecloud.bote.doc.consts.DocLockConsts;
import com.iwhalecloud.bote.doc.module.control.model.NodeBaseInfoDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentCommentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.request.AddCommentRequest;
import com.iwhalecloud.bote.doc.module.document.dto.request.CommentDeleteRequest;
import com.iwhalecloud.bote.doc.module.document.dto.request.QueryCommentsRequest;
import com.iwhalecloud.bote.doc.module.document.dto.request.QueryCorrectionsRequest;
import com.iwhalecloud.bote.doc.module.document.dto.request.ReviewCommentRequest;
import com.iwhalecloud.bote.doc.module.document.dto.request.UpdateCommentRequest;
import com.iwhalecloud.bote.doc.module.document.enums.RecordTypeEnum;
import com.iwhalecloud.bote.doc.module.document.enums.ReviewStatusEnum;
import com.iwhalecloud.bote.doc.module.document.service.DocumentChangEventPublisher;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentCommentService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文档评论管理Controller
 *
 * @author Aiqing
 * @since 2025-10-16
 */
@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "dc/document/comment")
@RequiredArgsConstructor
@Tag(name = "文档中心-文档评论管理")
public class DocumentCommentController {
  private final IDocumentCommentService documentCommentService;
  private final DocumentChangEventPublisher documentChangEventPublisher;
  private final DistributedLockFactory distributedLockFactory;

  @Operation(summary = "新增评论&修订")
  @PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResultVO<DocumentCommentDTO> addComment(@RequestBody @Valid AddCommentRequest request) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    DistributedLock lock = distributedLockFactory.getBizLock(DocLockConsts.DOCUMENT_PERMISSION_SAVE_LOCK, request.getUuid());
    DocumentCommentDTO commentDTO;
    try {
      lock.lock();
      commentDTO = documentCommentService.addComment(request, userId);
    }
    finally {
      lock.unlock();
    }
    return ResultVO.success(commentDTO);
  }

  @Operation(summary = "编辑评论&修订")
  @PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResultVO<Boolean> updateComment(@RequestBody @Valid UpdateCommentRequest request) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    Boolean result = documentCommentService.updateComment(request, userId);

    return ResultVO.success(result);
  }

  @Operation(summary = "删除评论")
  @PostMapping("/delete")
  public ResultVO<Boolean> deleteComment(@RequestBody @Valid CommentDeleteRequest commentDeleteRequest) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    Boolean result = documentCommentService.deleteComment(commentDeleteRequest.getDocumentId(),
      commentDeleteRequest.getCommentId(), userId);

    return ResultVO.success(result);
  }

  @Operation(summary = "审批修订记录")
  @PostMapping(value = "/approve", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResultVO<Boolean> approveComment(@RequestBody @Valid ReviewCommentRequest request) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    request.setReviewStatus(ReviewStatusEnum.APPROVED.getCode());
    NodeBaseInfoDTO nodeInfo = documentCommentService.approveComment(request, userId);
    documentChangEventPublisher.publishOnLineEditEvent(nodeInfo.getLibraryId(), nodeInfo.getNodeId(), userId, null, CommonConsts.TRUE);
    return ResultVO.success(Boolean.TRUE);
  }

  @Operation(summary = "拒绝修订记录")
  @PostMapping(value = "/reject", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResultVO<Boolean> rejectComment(@RequestBody @Valid ReviewCommentRequest request) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    request.setReviewStatus(ReviewStatusEnum.REJECTED.getCode());
    Boolean result = documentCommentService.rejectComment(request, userId);

    return ResultVO.success(result);
  }

  @Operation(summary = "根据多个UUID查询修正记录")
  @PostMapping(value = "/queryCorrectionsByUuid", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResultVO<List<DocumentCommentDTO>> queryCorrections(@RequestBody @Valid QueryCorrectionsRequest request) {
    List<DocumentCommentDTO> result = documentCommentService.queryCorrections(request.getDocumentId(), request.getUuids());

    return ResultVO.success(result);
  }

  @Operation(summary = "查询所有的评论记录")
  @PostMapping(value = "/queryComments", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResultVO<List<DocumentCommentDTO>> queryComments(@RequestBody @Valid QueryCommentsRequest request) {
    request.setRecordType(RecordTypeEnum.COMMENT.getCode());
    List<DocumentCommentDTO> result = documentCommentService.queryComments(request);

    return ResultVO.success(result);
  }

  @Operation(summary = "查询所有的修订记录")
  @PostMapping(value = "/queryCorrections", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResultVO<List<DocumentCommentDTO>> queryCorrections(@RequestBody @Valid QueryCommentsRequest request) {
    request.setRecordType(RecordTypeEnum.CORRECTION.getCode());
    request.setReviewStatus(ReviewStatusEnum.PENDING.getCode());
    List<DocumentCommentDTO> result = documentCommentService.queryComments(request);

    return ResultVO.success(result);
  }
}
