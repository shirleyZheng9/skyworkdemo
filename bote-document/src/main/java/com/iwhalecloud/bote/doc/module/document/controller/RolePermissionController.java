package com.iwhalecloud.bote.doc.module.document.controller;

import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.module.control.dto.RolePermissionConfigDTO;
import com.iwhalecloud.bote.doc.module.control.service.RolePermissionConfigService;
import com.iwhalecloud.bote.doc.module.control.vo.NodePermissionView;
import com.iwhalecloud.bote.dto.workspace.WorkspaceDTO;
import com.iwhalecloud.bote.service.workspace.helper.WorkspaceHelper;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色权限配置控制器
 *
 * <p>提供统一的角色权限配置接口</p>
 *
 * @author Aiqing
 * @since 2025-08-25
 */
@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "dc/role-permissions", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "文档中心-角色权限配置", description = "角色权限配置管理接口")
public class RolePermissionController {

  private final RolePermissionConfigService rolePermissionConfigService;
  private final WorkspaceHelper workSpaceHelper;

  @GetMapping("/config")
  @Operation(summary = "获取所有角色权限配置", description = "返回系统中所有角色的权限配置，前端可缓存此配置用于权限判断")
  public ResultVO<RolePermissionConfigDTO> getAllRolePermissions(
    @RequestParam(name = "platform", required = false) String platform, @RequestParam("spaceId") Long spaceId) {
    RolePermissionConfigDTO config = rolePermissionConfigService.getAllRolePermissions();
    //在这里抛一个事件到service里面进行初始化空间租户id的请求
    if (DocBaseConsts.AI_PORTAL.equals(platform)) {
      WorkspaceDTO workspaceDTO = workSpaceHelper.initAiTenantId(spaceId);
      if (workspaceDTO != null) {
        config.setSpaceTenantId(workspaceDTO.getSpaceTenantId());
      }
    }
    return ResultVO.success(config);
  }

  @GetMapping("/{roleTag}")
  @Operation(summary = "获取指定角色权限配置",
    description = "根据角色标识获取对应的权限配置")
  public ResultVO<NodePermissionView> getRolePermissions(
    @Parameter(description = "角色标识", example = "DOC_EDIT")
    @PathVariable String roleTag) {

    NodePermissionView permissions = rolePermissionConfigService.getRolePermissions(roleTag);
    if (permissions == null) {
      return ResultVO.fail("角色不存在: " + roleTag);
    }

    return ResultVO.success(permissions);
  }
}
