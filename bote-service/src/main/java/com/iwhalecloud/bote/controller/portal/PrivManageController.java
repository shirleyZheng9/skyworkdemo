package com.iwhalecloud.bote.controller.portal;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.portal.PortalDirDTO;
import com.iwhalecloud.bote.dto.portal.PortalDirMenuDTO;
import com.iwhalecloud.bote.dto.portal.PrivDTO;
import com.iwhalecloud.bote.dto.portal.SimplePortalMenuDTO;
import com.iwhalecloud.bote.dto.portal.SimpleRolePrivDTO;
import com.iwhalecloud.bote.dto.portal.query.PortalDirParams;
import com.iwhalecloud.bote.dto.portal.query.PrivQueryParams;
import com.iwhalecloud.bote.dto.portal.query.RolePrivQueryParams;
import com.iwhalecloud.bote.service.portal.IMenuManageService;
import com.iwhalecloud.bote.service.portal.IPrivManageService;
import com.iwhalecloud.bote.service.portal.IRolePrivManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限管理 controller
 *
 * @author auto
 * @since 2024-10-14
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/priv", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "门户：权限管理")
public class PrivManageController {

  private final IPrivManageService privManageService;
  private final IMenuManageService menuManageService;
  private final IRolePrivManageService rolePrivManageService;
  private final IRefreshCacheService refreshCacheService;

  @Operation(summary = "查询单个菜单组件")
  @GetMapping("getPriv")
  public ResultVO<PrivDTO> getPriv(@RequestParam(name = "privId") Long privId) {
    Assert.notNull(privId, "主键 ID 不能为空");
    return ResultVO.success(privManageService.getPriv(privId));
  }

  @Operation(summary = "保存菜单组件")
  @PostMapping("savePriv")
  public ResultVO<PrivDTO> savePriv(@RequestBody PrivDTO priv) {
    ResultVO<PrivDTO> result = privManageService.savePriv(priv);
    if (result.isSuccess()) {
      refreshCacheService.refreshAll(CacheConsts.CACHE_NAME_ROLE_PRIV);
    }
    return result;
  }

  @Operation(summary = "删除菜单组件")
  @GetMapping("deletePriv")
  public ResultVO<Void> deletePriv(@RequestParam(name = "privId") Long privId) {
    Assert.notNull(privId, "主键 ID 不能为空");
    ResultVO<Void> result = privManageService.deletePriv(privId);
    if (result.isSuccess()) {
      refreshCacheService.refreshAll(CacheConsts.CACHE_NAME_ROLE_PRIV);
    }
    return result;
  }

  @Operation(summary = "查询菜单组件列表")
  @PostMapping("queryPrivList")
  public ResultVO<List<PrivDTO>> queryPrivList(@RequestBody PrivQueryParams queryParams) {
    return ResultVO.success(privManageService.queryPrivList(queryParams));
  }

  @Operation(summary = "分页查询菜单组件")
  @PostMapping("queryPrivPage")
  public ResultVO<PageInfo<PrivDTO>> queryPrivPage(@RequestBody PrivQueryParams queryParams) {
    return ResultVO.success(privManageService.queryPrivPage(queryParams));
  }

  @Operation(summary = "查询单个门户目录")
  @GetMapping("getPortalDir")
  public ResultVO<PortalDirDTO> getPortalDir(@RequestParam(name = "dirId") Long dirId) {
    Assert.notNull(dirId, "主键 ID 不能为空");
    return ResultVO.success(menuManageService.getPortalDir(dirId));
  }

  @Operation(summary = "保存门户目录")
  @PostMapping("savePortalDir")
  public ResultVO<PortalDirDTO> savePortalDir(@RequestBody PortalDirDTO dir) {
    return menuManageService.savePortalDir(dir);
  }

  @Operation(summary = "删除门户目录")
  @GetMapping("deletePortalDir")
  public ResultVO<Void> deletePortalDir(@RequestParam(name = "dirId") Long dirId) {
    Assert.notNull(dirId, "主键 ID 不能为空");
    return menuManageService.deletePortalDir(dirId);
  }

  @Operation(summary = "查询单个目录菜单")
  @GetMapping("getDirMenu")
  public ResultVO<PortalDirMenuDTO> getDirMenu(@RequestParam(name = "relId") Long relId) {
    Assert.notNull(relId, "主键 ID 不能为空");
    return ResultVO.success(menuManageService.getDirMenu(relId));
  }

  @Operation(summary = "保存目录菜单")
  @PostMapping("saveDirMenu")
  public ResultVO<PortalDirMenuDTO> saveDirMenu(@RequestBody PortalDirMenuDTO menu) {
    return menuManageService.saveDirMenu(menu);
  }

  @Operation(summary = "批量保存目录下的菜单")
  @PostMapping("batchSaveDirMenu")
  public ResultVO<Void> batchSaveDirMenu(@RequestBody PortalDirParams params) {
    Assert.notNull(params.getDirId(), "门户目录 ID 不能为空");
    Assert.notEmpty(params.getMenuIds(), "菜单 ID 不能为空");
    return menuManageService.batchSaveDirMenu(params);
  }

  @Operation(summary = "删除目录菜单")
  @GetMapping("deleteDirMenu")
  public ResultVO<Void> deleteDirMenu(@RequestParam(name = "relId") Long relId) {
    Assert.notNull(relId, "主键 ID 不能为空");
    return menuManageService.deleteDirMenu(relId);
  }

  @Operation(summary = "查询门户目录菜单树型数据")
  @GetMapping("queryMenuTree")
  public ResultVO<List<SimplePortalMenuDTO>> queryMenuTree() {
    return ResultVO.success(menuManageService.querySimpleMenuTree(Collections.emptyList()));
  }

  @Operation(summary = "保存角色权限")
  @PostMapping("saveRolePriv")
  public ResultVO<Void> saveRolePriv(@RequestBody RolePrivQueryParams params) {
    Assert.hasText(params.getRoleCode(), "角色编码不能为空");
    Assert.notEmpty(params.getPrivIds(), "权限 ID 集合不能为空");
    ResultVO<Void> result = rolePrivManageService.saveRolePriv(params);
    if (result.isSuccess()) {
      refreshCacheService.refreshAll(CacheConsts.CACHE_NAME_ROLE_PRIV);
    }
    return result;
  }

  @Operation(summary = "删除角色权限")
  @PostMapping("deleteRolePriv")
  public ResultVO<Void> deleteRolePriv(@RequestBody RolePrivQueryParams params) {
    Assert.hasText(params.getRoleCode(), "角色编码不能为空");
    Assert.notEmpty(params.getPrivIds(), "权限 ID 集合不能为空");
    ResultVO<Void> result = rolePrivManageService.deleteRolePriv(params);
    if (result.isSuccess()) {
      refreshCacheService.refreshAll(CacheConsts.CACHE_NAME_ROLE_PRIV);
    }
    return result;
  }

  @Operation(summary = "查询角色权限列表")
  @PostMapping("queryRolePrivList")
  public ResultVO<List<SimpleRolePrivDTO>> queryRolePrivList(@RequestBody RolePrivQueryParams queryParams) {
    return ResultVO.success(rolePrivManageService.queryRolePrivList(queryParams));
  }

  @Operation(summary = "查询当前用户可访问的门户菜单")
  @GetMapping("queryPortal")
  public ResultVO<Map<String, Object>> queryPortal(@RequestParam(name = "tenantId", required = false) Long tenantId,
    @RequestParam(name = "spaceId") Long spaceId) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    return ResultVO.success(rolePrivManageService.queryPortal(spaceId, tenantId));
  }
}
