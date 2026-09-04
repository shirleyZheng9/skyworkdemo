package com.iwhalecloud.bote.doc.module.document.controller;

import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.space.annotation.IgnoreSpace;
import com.iwhalecloud.bote.doc.common.tenant.annotation.IgnoreTenant;
import com.iwhalecloud.bote.doc.consts.DocumentPermConsts;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.WorkbookContentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.WorkbookContentHistoryInfoDTO;
import com.iwhalecloud.bote.doc.module.document.dto.WorkbookLockRequestDTO;
import com.iwhalecloud.bote.doc.module.document.dto.WorkbookLockStatusDTO;
import com.iwhalecloud.bote.doc.module.document.dto.WorkbookSaveRequestDTO;
import com.iwhalecloud.bote.doc.module.document.service.DocumentChangEventPublisher;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.module.document.service.IWorkbookContentService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 在线表格相关接口
 *
 * @author Aiqing
 * @since 2025/9/26
 */
@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "dc/document/workbook", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "文档中心-在线表格")
public class WorkbookController {

  private static final Logger logger = LoggerFactory.getLogger(WorkbookController.class);

  private final IWorkbookContentService workbookService;
  private final DocumentChangEventPublisher documentChangEventPublisher;
  private final ControlTemplate controlTemplate;
  private final IDocumentService documentService;

  @Operation(summary = "查询工作簿锁定状态")
  @GetMapping("/lockStatus/{documentId}")
  public ResultVO<WorkbookLockStatusDTO> getLockStatus(
    @Parameter(description = "文档ID", required = true)
    @PathVariable String documentId) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    String currentSessionId = SessionUtil.getSessionId();
    WorkbookLockStatusDTO lockStatus = workbookService.getLockStatus(documentId, userId, currentSessionId);
    return ResultVO.success(lockStatus);
  }

  @Operation(summary = "锁定/解锁工作簿", description = "同一用户重复锁定会自动延长锁定时间")
  @PostMapping("/lock")
  public ResultVO<WorkbookLockStatusDTO> lockWorkbook(
    @Parameter(description = "锁定请求参数", required = true)
    @RequestBody @Valid WorkbookLockRequestDTO request) {
    Long currentUserId = SessionUtil.getLoginInfo().getUserId();
    String currentSessionId = SessionUtil.getSessionId();
    WorkbookLockStatusDTO lockStatus = workbookService.lockWorkbook(request, currentUserId, currentSessionId);
    return ResultVO.success(lockStatus);
  }

  @Operation(summary = "保存工作簿内容")
  @PostMapping("/save")
  public ResultVO<WorkbookContentDTO> saveWorkbook(
    @Parameter(description = "保存请求参数", required = true)
    @RequestBody @Valid WorkbookSaveRequestDTO request) {
    Long currentUserId = SessionUtil.getLoginInfo().getUserId();
    WorkbookContentDTO result = workbookService.saveWorkbook(request, currentUserId);
    if (result != null) {
      documentChangEventPublisher.publishEditEvent(result.getLibraryId(), request.getDocumentId(), currentUserId);
    }
    return ResultVO.success(result);
  }

  @Operation(summary = "获取工作簿内容")
  @GetMapping("/content/{documentId}")
  public ResultVO<WorkbookContentDTO> getWorkbookContent(
    @Parameter(description = "文档ID", required = true)
    @PathVariable String documentId) {
    Long currentUserId = SessionUtil.getLoginInfo().getUserId();
    WorkbookContentDTO content = workbookService.getWorkbookContent(documentId, currentUserId);
    return ResultVO.success(content);
  }

  @IgnoreTenant
  @IgnoreSpace
  @Operation(summary = "查询在线表格历史版本内容")
  @GetMapping("/getWorkbookHisContent")
  public ResultVO<String> getWorkbookHisContent(@RequestParam("id") Long id) {
    return ResultVO.success(workbookService.getWorkbookHisContent(id));
  }

  @IgnoreTenant
  @IgnoreSpace
  @Operation(summary = "查询在线表格历史版本")
  @GetMapping("/getWorkbookVersions")
  public ResultVO<List<WorkbookContentHistoryInfoDTO>> getWorkbookVersions(@RequestParam("documentId") String documentId) {
    checkDocument(documentId, NodePermission.READ_NODE, DocumentPermConsts.ACCESS_DENIED_CALLBACK);
    return ResultVO.success(workbookService.getWorkbookVersions(documentId));
  }

  @IgnoreTenant
  @IgnoreSpace
  @Operation(summary = "使用指定版本的文档内容")
  @PostMapping("/restoreWorkbookVersion/{documentId}/{id}")
  public ResultVO<Void> restoreContentVersion(@PathVariable("documentId") String documentId, @PathVariable("id") Long id) {
    checkDocument(documentId, NodePermission.EDIT_NODE, DocumentPermConsts.EDIT_DENIED_CALLBACK);
    Long currentUserId = SessionUtil.getLoginInfo().getUserId();
    String currentSessionId = SessionUtil.getSessionId();
    WorkbookLockRequestDTO lockRequest = new WorkbookLockRequestDTO();
    lockRequest.setDocumentId(documentId);
    lockRequest.setLockAction(DocBaseConsts.DOCUMENT_EDIT_ACTION_LOCK);
    workbookService.lockWorkbook(lockRequest, currentUserId, currentSessionId);
    try {
      WorkbookContentDTO result = workbookService.restoreContentVersion(documentId, id);
      if (result != null) {
        documentChangEventPublisher.publishEditEvent(result.getLibraryId(), documentId, currentUserId);
      }
      return ResultVO.success();
    }
    finally {
      WorkbookLockRequestDTO unlockRequest = new WorkbookLockRequestDTO();
      unlockRequest.setDocumentId(documentId);
      unlockRequest.setLockAction(DocBaseConsts.DOCUMENT_EDIT_ACTION_UNLOCK);
      try {
        workbookService.lockWorkbook(unlockRequest, currentUserId, currentSessionId);
      }
      catch (Exception e) {
        logger.warn("Restore workbook version: unlock failed, documentId={}", documentId, e);
      }
    }
  }

  private void checkDocument(String documentId, NodePermission permission, Consumer<Boolean> resultCallback) {
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    if (documentDTO == null) {
      throw new BssException("文件不存在");
    }
    Long creatorId = SessionUtil.getLoginInfo().getUserId();
    //无查看权限
    controlTemplate.checkNodePermission(documentDTO.getLibraryId(), creatorId, documentId,
      permission, resultCallback);
  }

}
