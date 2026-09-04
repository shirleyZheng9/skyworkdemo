package com.iwhalecloud.bote.doc.module.document.controller;

import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.consts.PermissionActionEnum;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPermissionBatchUpdateRO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPermissionDetailDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPermissionUpdateRO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPermissionUpdateResultDTO;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentPermissionService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文档权限配置
 *
 * @author Aiqing
 * @since 2025/9/5
 */
@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "dc/document/permission",
  produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "文档中心：文档库文档")
public class DocumentPermissionController {

  private final IDocumentPermissionService documentPermissionService;

  @Operation(summary = "更新文档授权")
  @PostMapping("/{documentId}/update")
  public ResultVO<Void> updatePermission(@RequestBody @Valid DocumentPermissionUpdateRO permissionUpdateRO,
                                         @PathVariable String documentId) {
    String action = permissionUpdateRO.getAction();
    PermissionActionEnum permissionAction;
    try {
      permissionAction = PermissionActionEnum.valueOf(action);
    }
    catch (IllegalArgumentException e) {
      return ResultVO.fail("无效的权限操作类型");
    }

    documentPermissionService.updatePermission(permissionAction, permissionUpdateRO.getData(), documentId);
    return ResultVO.success();
  }

  @Operation(summary = "批量更新文档授权")
  @PostMapping("/{documentId}/batchUpdate")
  public ResultVO<DocumentPermissionUpdateResultDTO> updatePermissionBatch(@RequestBody @Valid DocumentPermissionBatchUpdateRO batchUpdateRO,
                                                                           @PathVariable String documentId) {
    String action = batchUpdateRO.getAction();
    PermissionActionEnum permissionAction;
    try {
      permissionAction = PermissionActionEnum.valueOf(action);
    }
    catch (IllegalArgumentException e) {
      return ResultVO.fail("无效的权限操作类型");
    }
    DocumentPermissionUpdateResultDTO updateResultDTO =
      documentPermissionService.updatePermissionBatch(permissionAction, batchUpdateRO.getMembers(), documentId);
    return ResultVO.success(updateResultDTO);
  }

  @Operation(summary = "查询已有权限配置")
  @GetMapping("permissionSets")
  public ResultVO<DocumentPermissionDetailDTO> queryPermissionSet(@RequestParam String documentId) {
    DocumentPermissionDetailDTO dtoList = documentPermissionService.queryDocumentPermissionSetWithInherit(documentId);
    return ResultVO.success(dtoList);
  }
}
