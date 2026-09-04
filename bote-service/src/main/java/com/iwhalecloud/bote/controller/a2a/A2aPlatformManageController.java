package com.iwhalecloud.bote.controller.a2a;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.a2a.A2aPlatformDTO;
import com.iwhalecloud.bote.dto.a2a.query.A2aPlatformQueryParams;
import com.iwhalecloud.bote.service.a2a.IA2aPlatformManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
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

/**
 * A2A 平台管理
 *
 * @author bianjp
 * @since 2025-09-08
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/a2aPlatform", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "A2A 平台管理")
public class A2aPlatformManageController {
  private final IA2aPlatformManageService a2aPlatformManageService;

  @Operation(summary = "查询单个 A2A 平台")
  @GetMapping("findA2aPlatform")
  public ResultVO<A2aPlatformDTO> findA2aPlatform(@RequestParam("tenantId") Long tenantId, @RequestParam("platformId") Long platformId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(platformId, "A2A 平台 ID 不能为空");
    A2aPlatformDTO platform = a2aPlatformManageService.findA2aPlatform(tenantId, platformId);
    platform.parseJsonConfig();
    return ResultVO.success(platform);
  }

  @Operation(summary = "保存 A2A 平台")
  @PostMapping("saveA2aPlatform")
  public ResultVO<A2aPlatformDTO> saveA2aPlatform(@RequestBody A2aPlatformDTO platform) {
    Assert.notNull(platform.getTenantId(), "租户 ID 不能为空");
    platform.saveJsonConfig();
    return a2aPlatformManageService.saveA2aPlatform(platform);
  }

  @Operation(summary = "重置发布密钥")
  @PostMapping("resetPublishKey")
  public ResultVO<String> resetPublishKey(@RequestParam("tenantId") Long tenantId, @RequestParam("platformId") Long platformId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(platformId, "A2A 平台 ID 不能为空");
    return ResultVO.success(a2aPlatformManageService.resetPublishKey(tenantId, platformId));
  }

  @Operation(summary = "删除 A2A 平台")
  @PostMapping("deleteA2aPlatform")
  public ResultVO<Void> deleteA2aPlatform(@RequestParam("tenantId") Long tenantId, @RequestParam("platformId") Long platformId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(platformId, "A2A 平台 ID 不能为空");
    return a2aPlatformManageService.deleteA2aPlatform(tenantId, platformId);
  }

  @Operation(summary = "查询 A2A 平台列表")
  @PostMapping("queryA2aPlatformList")
  public ResultVO<List<A2aPlatformDTO>> queryA2aPlatformList(@RequestBody A2aPlatformQueryParams queryParams) {
    return ResultVO.success(a2aPlatformManageService.queryA2aPlatformList(queryParams));
  }

  @Operation(summary = "分页查询 A2A 平台")
  @PostMapping("queryA2aPlatformPage")
  public ResultVO<PageInfo<A2aPlatformDTO>> queryA2aPlatformPage(@RequestBody A2aPlatformQueryParams queryParams) {
    return ResultVO.success(a2aPlatformManageService.queryA2aPlatformPage(queryParams));
  }
}
