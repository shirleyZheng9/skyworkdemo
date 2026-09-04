package com.iwhalecloud.bote.doc.module.document.controller;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.exception.DocumentLibraryAccessDenyException;
import com.iwhalecloud.bote.doc.consts.DocumentPermConsts;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentActivityDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentActivityQueryParams;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentActivityService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.module.library.service.DocumentLibraryPermissionService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文档动态记录控制器
 *
 * @since 2025-08-21
 */
@RestController
@RequestMapping(DocBaseConsts.API_PREFIX + "dc/document/activities")
@RequiredArgsConstructor
@Tag(name = "文档中心-文档库动态", description = "文档动态记录相关API")
public class DocumentActivityController {

  private final IDocumentService documentService;
  private final IDocumentActivityService documentActivityService;
  private final DocumentLibraryPermissionService documentLibraryPermissionService;
  private final ControlTemplate controlTemplate;

  @GetMapping("/documents/{documentId}")
  @Operation(summary = "查询文档动态记录", description = "查询指定文档的操作动态记录")
  public ResultVO<List<DocumentActivityDTO>> getDocumentActivities(
    @Parameter(description = "文档ID") @PathVariable String documentId,
    @Parameter(description = "限制条数", example = "20") @RequestParam(defaultValue = "20") Integer limit) {

    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    Assert.notNull(documentDTO, "文档不存在");

    Long userId = SessionUtil.getLoginInfo().getUserId();
    controlTemplate.checkNodePermission(documentDTO.getLibraryId(), userId, documentId,
      NodePermission.READ_NODE, DocumentPermConsts.ACCESS_DENIED_CALLBACK);

    List<DocumentActivityDTO> activities = documentActivityService.findByDocumentId(documentId, limit);
    return ResultVO.success(activities);
  }

  @GetMapping("/libraries/{libraryId}")
  @Operation(summary = "查询文档库动态记录", description = "查询指定文档库的操作动态记录，支持分页，默认按创建时间倒序")
  public ResultVO<PageInfo<DocumentActivityDTO>> getLibraryActivities(
    @Parameter(description = "文档库ID") @PathVariable String libraryId,
    DocumentActivityQueryParams queryParams) {

    Long userId = SessionUtil.getLoginInfo().getUserId();
    // 校验文档库权限
    boolean hasPermission = documentLibraryPermissionService.checkLibraryAccessPermission(libraryId, userId);
    if (!hasPermission) {
      throw new DocumentLibraryAccessDenyException();
    }
    // 设置文档库ID到查询参数中
    queryParams.setLibraryId(libraryId);
    PageInfo<DocumentActivityDTO> result = documentActivityService.findActivityPage(queryParams);
    return ResultVO.success(result);
  }
}
