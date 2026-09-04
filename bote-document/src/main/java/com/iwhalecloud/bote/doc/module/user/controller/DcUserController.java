package com.iwhalecloud.bote.doc.module.user.controller;

import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.module.user.dto.ListUserByOrgRequest;
import com.iwhalecloud.bote.doc.module.user.dto.OrgUserDTO;
import com.iwhalecloud.bote.doc.module.user.dto.OrgUserSearchResponse;
import com.iwhalecloud.bote.doc.module.user.dto.UserSearchRequest;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserManageService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Aiqing
 * @since 2025/9/4
 */
@Tag(name = "文档中心-门户用户")
@RestController
@RequestMapping(DocBaseConsts.API_PREFIX + "dc/portal")
@RequiredArgsConstructor
public class DcUserController {

  private final IDcUserManageService dcUserManageService;
  private final IDcUserService dcUserService;

  @Operation(summary = "获取组织成员列表")
  @PostMapping("listUserByOrg")
  public ResultVO<OrgUserDTO> listUserByOrg(@RequestBody ListUserByOrgRequest request) {
    PortalUserDTO currentUser = dcUserService.getCurrentSessionUser();
    OrgUserDTO result = dcUserManageService.listUserByOrgWithDefaultOrg(request.getOrgId(), () -> {
      if (currentUser != null) {
        OrgDTO maxLevelOrg = currentUser.getMaxLevelOrg();
        return maxLevelOrg == null ? null : maxLevelOrg.getOrgId();
      }
      return null;
    });
    return ResultVO.success(result);
  }

  @Operation(summary = "根据关键词搜索用户")
  @PostMapping("searchUsers")
  public ResultVO<List<PortalUserDTO>> searchUsers(@RequestBody UserSearchRequest request) {
    return ResultVO.success(dcUserManageService.searchUsers(request.getKeyword(), request.getSpaceId()));
  }

  @Operation(summary = "根据关键词同时搜索组织和用户")
  @PostMapping("searchOrgUsers")
  public ResultVO<OrgUserSearchResponse> searchOrgUsers(@RequestBody UserSearchRequest request) {
    return ResultVO.success(dcUserManageService.searchOrgUsers(request.getKeyword(), request.getSpaceId()));
  }

  @Operation(summary = "查询当前登录用户信息")
  @GetMapping("logged")
  public ResultVO<LoginInfo> logged() {
    // 无使用场景
    return ResultVO.success(SessionUtil.getLoginInfo());
  }

  @Operation(summary = "根据用户ID查询用户")
  @GetMapping("queryUserById")
  public ResultVO<PortalUserDTO> queryById(@RequestParam Long userId) {
    PortalUserDTO portalUserDTO = dcUserService.findUserById(userId);
    return ResultVO.success(portalUserDTO);
  }
}
