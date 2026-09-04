package com.iwhalecloud.bote.doc.module.open.controller;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.RequireOAuth2;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.common.space.SpaceContextHolder;
import com.iwhalecloud.bote.doc.common.space.annotation.IgnoreSpace;
import com.iwhalecloud.bote.doc.common.tenant.annotation.IgnoreTenant;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.module.open.dto.DcOrgUserListRequest;
import com.iwhalecloud.bote.doc.module.open.dto.DocumentSpaceUserSearchRequest;
import com.iwhalecloud.bote.doc.module.user.dto.OrgUserDTO;
import com.iwhalecloud.bote.doc.module.user.dto.OrgUserSearchResponse;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserManageService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Aiqing
 * @since 2025/12/31
 */
@RestController
@RequireOAuth2
@IgnoreTenant
@IgnoreSpace
@IgnoreSession
@RequiredArgsConstructor
@RequestMapping(DocBaseConsts.API_PREFIX + "open/dc/user")
@Tag(name = "开放接口：文档中心")
public class DocumentUserOpenController {

  private static final Logger logger = LoggerFactory.getLogger(DocumentUserOpenController.class);

  private final IDcUserManageService dcUserManageService;
  private final IDocumentService documentService;
  private final IDcUserService dcUserService;

  @Operation(summary = "根据关键词同时搜索组织和用户")
  @PostMapping("searchOrgUsers")
  public ResultVO<OrgUserSearchResponse> searchOrgUsers(@RequestBody @Validated DocumentSpaceUserSearchRequest request) {
    Long spaceId = resolveSpaceId(request.getDocumentId());
    if (spaceId == null) {
      return ResultVO.success(OrgUserSearchResponse.empty());
    }
    return ResultVO.success(dcUserManageService.searchOrgUsers(request.getKeyword(), spaceId));
  }

  @Operation(summary = "获取组织成员列表")
  @PostMapping("listUserByOrg")
  public ResultVO<OrgUserDTO> listUserByOrg(@RequestBody @Validated DcOrgUserListRequest request) {
    // 处理全局空间ID
    resolveSpaceId(request.getDocumentId());

    PortalUserDTO portalUserDTO = dcUserService.findUserById(request.getOptUserId());
    if (portalUserDTO == null) {
      return ResultVO.success(OrgUserDTO.empty());
    }
    OrgUserDTO result = dcUserManageService.listUserByOrgWithDefaultOrg(request.getOrgId(), () -> {
      OrgDTO maxLevelOrg = portalUserDTO.getMaxLevelOrg();
      return Optional.ofNullable(maxLevelOrg)
        .map(OrgDTO::getOrgId)
        .orElse(null);
    });
    return ResultVO.success(result);
  }


  @Operation(summary = "根据关键词搜索用户")
  @PostMapping("searchUsers")
  public ResultVO<List<PortalUserDTO>> searchUsers(@RequestBody @Validated DocumentSpaceUserSearchRequest request) {
    Long spaceId = resolveSpaceId(request.getDocumentId());
    if (spaceId == null) {
      return ResultVO.success(Collections.emptyList());
    }
    List<PortalUserDTO> portalUserDTOS = dcUserManageService.searchUsers(request.getKeyword(), spaceId);
    return ResultVO.success(portalUserDTOS);
  }

  private Long resolveSpaceId(String documentId) {
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    if (documentDTO == null) {
      logger.warn("未查询到文档信息， documentId:{}", documentId);
      return null;
    }
    Long spaceId = documentDTO.getSpaceId();
    SpaceContextHolder.setIgnore(false);
    SpaceContextHolder.setSpaceId(spaceId);
    return spaceId;
  }
}
