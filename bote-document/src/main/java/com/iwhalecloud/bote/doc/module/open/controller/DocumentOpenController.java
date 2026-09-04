package com.iwhalecloud.bote.doc.module.open.controller;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.RequireOAuth2;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.common.tenant.annotation.IgnoreTenant;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRole;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Aiqing
 * @since 2025/12/30
 */
@RequestMapping(DocBaseConsts.API_PREFIX + "open/dc/document")
@IgnoreSession
@RequireOAuth2
@RestController
@IgnoreTenant
@RequiredArgsConstructor
@Tag(name = "开放接口：文档中心")
public class DocumentOpenController {

  private final IDocumentService documentService;
  private final ControlTemplate controlTemplate;

  @Operation(summary = "查询是否有文档权限")
  @GetMapping("checkHasDocumentPermission")
  public ResultVO<Boolean> checkHasDocumentPermission(@RequestParam Long userId, @RequestParam String documentId) {
    ControlRole controlRole = fetchDocumentRole(userId, documentId);
    if (controlRole == null) {
      return ResultVO.success(false);
    }
    return ResultVO.success(controlRole.hasPermission(NodePermission.READ_NODE));
  }

  private ControlRole fetchDocumentRole(Long userId, String documentId) {
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    if (documentDTO == null) {
      return null;
    }
    TenantContextHolder.setTenantId(documentDTO.getTenantId());
    TenantContextHolder.setIgnore(false);

    String libraryId = documentDTO.getLibraryId();

    return controlTemplate.fetchNodeRole(libraryId, userId, documentId);
  }

  @Operation(summary = "查询是否有文档的管理权限")
  @GetMapping("checkHasDocumentAdminPermission")
  public ResultVO<Boolean> checkHasDocumentAdminPermission(@RequestParam Long userId, @RequestParam String documentId) {
    ControlRole controlRole = fetchDocumentRole(userId, documentId);
    if (controlRole == null) {
      return ResultVO.success(false);
    }
    return ResultVO.success(controlRole.hasPermission(NodePermission.MANAGE_NODE));
  }


}
