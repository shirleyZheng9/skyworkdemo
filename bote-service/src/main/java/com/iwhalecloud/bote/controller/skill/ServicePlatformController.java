package com.iwhalecloud.bote.controller.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.skill.ServicePlatformDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.service.skill.IServicePlatformManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * 技能：API 平台 controller
 *
 * @author auto
 * @since 2024-09-17
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/servicePlatform", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor
@Tag(name = "技能：API 平台管理")
public class ServicePlatformController {

  private final IServicePlatformManageService platformManageService;

  private final IRefreshCacheService refreshCacheService;

  @PostMapping("saveServicePlatform")
  @Operation(summary = "保存 API 平台")
  public ResultVO<ServicePlatformDTO> saveServicePlatform(@RequestBody @Valid ServicePlatformDTO platform) {
    Assert.notNull(platform.getPlatformCode(), "平台编码不能为空");
    Long platformId = platform.getPlatformId();
    ResultVO<ServicePlatformDTO> result = platformManageService.saveServicePlatform(platform);
    // 修改平台时自动刷新缓存
    if (platformId != null && result.isSuccess()) {
      String key = platform.getTenantId() + CacheConsts.COLON + platformId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_GATEWAY, key);
    }
    return result;
  }

  @PostMapping("queryServicePlatformList")
  @Operation(summary = "查询 API 平台列表")
  public ResultVO<List<ServicePlatformDTO>> queryServicePlatformList(@RequestBody SkillQueryParams params) {
    return ResultVO.success(platformManageService.queryServicePlatformList(params));
  }

  @GetMapping("findServicePlatform")
  @Operation(summary = "查询单个 API 平台")
  public ResultVO<ServicePlatformDTO> findServicePlatform(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam("platformId") Long platformId) {
    Assert.notNull(platformId, "平台 ID 不能为空");
    return ResultVO.success(platformManageService.findServicePlatform(tenantId, platformId));
  }

  @GetMapping("deleteServicePlatform")
  @Operation(summary = "删除 API 平台")
  public ResultVO<Void> deleteServicePlatform(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam("platformId") Long platformId) {
    Assert.notNull(platformId, "平台 ID 不能为空");
    ResultVO<Void> result = platformManageService.deleteServicePlatform(tenantId, platformId);
    if (result.isSuccess()) {
      String key = tenantId + CacheConsts.COLON + platformId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_GATEWAY, key);
    }
    return result;
  }

  @PostMapping("queryServicePlatformPage")
  @Operation(summary = "分页查询 API 平台")
  public ResultVO<PageInfo<ServicePlatformDTO>> queryServicePlatformPage(@RequestBody SkillQueryParams params) {
    return ResultVO.success(platformManageService.queryServicePlatformPage(params));
  }
}
