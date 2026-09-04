package com.iwhalecloud.bote.doc.module.document.controller;

import java.util.List;

import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.consts.DocumentPermConsts;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentFileLogInfoDTO;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentFileLogService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 文档上传文件日志
 *
 * @author bote-doc
 * @since 2026-04-15
 */
@RestController
@RequestMapping(DocBaseConsts.API_PREFIX + "dc/document/fileLog")
@RequiredArgsConstructor
@Tag(name = "文档中心-上传文件日志", description = "文档上传文件版本日志查询")
public class DocumentFileLogController {

  private final IDocumentFileLogService documentFileLogService;
  private final IDocumentService documentService;
  private final ControlTemplate controlTemplate;

  @GetMapping(params = "documentId")
  @Operation(summary = "按文档查询上传文件日志列表",
    description = "关联文档表、文件表返回文档名称、文件名、文档内容来源类型；按自然日分多组返回，revisionName 当天为「今天」其余为 yyyy-MM-dd，data 为组内列表（创建时间倒序）")
  public ResultVO<List<DocumentFileLogInfoDTO>> listByDocumentId(
    @Parameter(description = "文档ID", required = true) @RequestParam("documentId") String documentId,
    @RequestParam("tenantId") Long tenantId) {
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    Assert.notNull(documentDTO, "文档不存在");
    Long userId = SessionUtil.getLoginInfo().getUserId();
    controlTemplate.checkNodePermission(documentDTO.getLibraryId(), userId, documentId, NodePermission.READ_NODE,
      DocumentPermConsts.ACCESS_DENIED_CALLBACK);
    return ResultVO.success(documentFileLogService.listDetailByDocumentId(documentId, tenantId));
  }

}
