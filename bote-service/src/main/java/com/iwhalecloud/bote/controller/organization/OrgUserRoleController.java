package com.iwhalecloud.bote.controller.organization;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.organization.OrgUserRoleDTO;
import com.iwhalecloud.bote.dto.organization.query.OrgUserRoleQuery;
import com.iwhalecloud.bote.service.organization.IOrgUserRoleService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * 组织用户角色 Controller
 *
 * @author wangtingyun
 * @since 2025-10-30
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/organization/role", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "组织用户角色")
public class OrgUserRoleController {

  private final IOrgUserRoleService orgUserRoleService;

  @Operation(summary = "分页查询组织用户角色列表")
  @PostMapping("getOrgUserRolePage")
  public ResultVO<PageInfo<OrgUserRoleDTO>> getOrgUserRolePage(@RequestBody OrgUserRoleQuery query) {
    return ResultVO.success(orgUserRoleService.queryOrgUserRolePage(query));
  }

  @Operation(summary = "统计组织用户角色数量")
  @PostMapping("countOrgUserRole")
  public ResultVO<Integer> countOrgUserRole(@RequestBody OrgUserRoleQuery query) {
    return ResultVO.success(orgUserRoleService.countOrgUserRole(query));
  }

  @Operation(summary = "批量添加组织用户角色")
  @PostMapping("batchAddOrgUserRole")
  public ResultVO<Void> batchAddOrgUserRole(@RequestBody OrgUserRoleDTO roleDTO) {
    orgUserRoleService.batchAddOrgUserRole(roleDTO);
    return ResultVO.success();
  }

  @Operation(summary = "移除单个组织用户角色")
  @GetMapping("removeOrgUserRole")
  public ResultVO<Void> removeOrgUserRole(@RequestParam("orgRoleId") Long orgRoleId) {
    orgUserRoleService.delOrgUserRoleById(orgRoleId);
    return ResultVO.success();
  }

  @Operation(summary = "批量移除组织用户角色")
  @PostMapping("batchRemoveOrgUserRole")
  public ResultVO<Void> batchRemoveOrgUserRole(@RequestBody OrgUserRoleDTO roleDTO) {
    orgUserRoleService.batchDelOrgUserRole(roleDTO.getSpaceId(), roleDTO.getRemoveRoleUserIds());
    return ResultVO.success();
  }

  @Operation(summary = "修改用户的组织成员角色")
  @PostMapping("modifyUserOrgRole")
  public ResultVO<Void> modifyUserOrgRole(@RequestBody OrgUserRoleDTO roleDTO) {
    orgUserRoleService.modifyUserOrgRole(roleDTO);
    return ResultVO.success();
  }

}
