package com.iwhalecloud.bote.controller.portal;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.util.DcPublicUtil;
import com.iwhalecloud.bote.dto.portal.ExternalPortalDTO;
import com.iwhalecloud.bote.dto.portal.query.ExternalPortalQueryParams;
import com.iwhalecloud.bote.service.portal.IExternalPortalManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
 * 门户管理
 *
 * @author bianjp
 * @since 2025-02-24
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/externalPortal", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "门户：门户对接")
public class ExternalPortalManageController {
  private final IExternalPortalManageService externalPortalManageService;

  private final IRefreshCacheService refreshCacheService;

  @GetMapping("getPortal")
  @Operation(summary = "获取门户详情")
  public ResultVO<ExternalPortalDTO> getPortal(@RequestParam("id") Long id) {
    Assert.notNull(id, "门户 ID 不能为空");
    return ResultVO.success(externalPortalManageService.getPortal(id));
  }

  @PostMapping("queryPortalPage")
  @Operation(summary = "分页查询门户")
  public ResultVO<PageInfo<ExternalPortalDTO>> queryPortalPage(@RequestBody ExternalPortalQueryParams queryParams) {
    return ResultVO.success(externalPortalManageService.queryPortalPage(queryParams));
  }

  @PostMapping("savePortal")
  @Operation(summary = "保存门户")
  public ResultVO<Long> savePortal(@RequestBody ExternalPortalDTO portal) {
    ExternalPortalDTO oldPortal = portal.getId() != null ? externalPortalManageService.getPortal(portal.getId()) : null;
    ResultVO<Long> result = externalPortalManageService.savePortal(portal);
    // 刷新缓存
    if (result.isSuccess()) {
      List<String> keys = Stream.of(oldPortal != null ? oldPortal.getPortalCode() : null, portal.getPortalCode()).filter(Objects::nonNull).distinct()
        .collect(Collectors.toList());
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_PORTAL_ADAPTER, keys);
    }
    return result;
  }

  @GetMapping("updatePortalStatus")
  @Operation(summary = "更新门户状态（启用/禁用）")
  public ResultVO<Void> updatePortalStatus(@RequestParam("id") Long id, @RequestParam("enabled") Boolean enabled) {
    Assert.notNull(id, "门户 ID 不能为空");
    Assert.notNull(enabled, "状态不能为空");
    ExternalPortalDTO portal = externalPortalManageService.getPortal(id);
    ResultVO<Void> result = externalPortalManageService.updatePortalStatus(id, enabled);
    // 刷新缓存
    if (result.isSuccess() && BaseConsts.STATUS_CD_VALID.equals(portal.getStatusCd())) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_PORTAL_ADAPTER, portal.getPortalCode());
    }
    return result;
  }

  @GetMapping("deletePortal")
  @Operation(summary = "删除门户")
  public ResultVO<Void> deletePortal(@RequestParam("id") Long id) {
    Assert.notNull(id, "门户 ID 不能为空");
    ExternalPortalDTO portal = externalPortalManageService.getPortal(id);
    ResultVO<Void> result = externalPortalManageService.deletePortal(id);
    // 刷新缓存
    if (result.isSuccess() && BaseConsts.STATUS_CD_VALID.equals(portal.getStatusCd())) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_PORTAL_ADAPTER, portal.getPortalCode());
    }
    return result;
  }

  @Operation(summary = "获取单点登录 groovy 脚本说明")
  @GetMapping("getSsoGroovyTip")
  public ResultVO<String> getSsoGroovyTip() {
    return ResultVO.success(DcPublicUtil.getRequiredCodea(DcPublicUtil.SSO_GROOVY_TIP));
  }
}
