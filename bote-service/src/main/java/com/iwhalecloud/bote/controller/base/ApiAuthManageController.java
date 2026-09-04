package com.iwhalecloud.bote.controller.base;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.base.ApiAuthDTO;
import com.iwhalecloud.bote.dto.base.ApiDTO;
import com.iwhalecloud.bote.dto.base.AppPublishDTO;
import com.iwhalecloud.bote.dto.base.CatalogTree;
import com.iwhalecloud.bote.dto.base.query.ApiAuthQueryParams;
import com.iwhalecloud.bote.service.base.IApiAuthManageService;
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

/**
 * API 鉴权管理 controller
 *
 * @author chen.linfa
 * @since 2025-01-20
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/apiAuth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "基础：API 鉴权管理")
public class ApiAuthManageController {
  private final IApiAuthManageService apiAuthManageService;
  private final IRefreshCacheService refreshCacheService;

  @Operation(summary = "查询单个鉴权")
  @GetMapping("getApiAuth")
  public ResultVO<ApiAuthDTO> getApiAuth(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam(name = "authId") Long authId) {
    Assert.notNull(authId, "主键 ID 不能为空");
    return ResultVO.success(apiAuthManageService.getApiAuth(tenantId, authId));
  }

  @Operation(summary = "保存鉴权")
  @PostMapping("saveApiAuth")
  public ResultVO<ApiAuthDTO> saveApiAuth(@RequestBody ApiAuthDTO apiAuth) {
    boolean isUpdate = apiAuth.getAuthId() != null;
    ResultVO<ApiAuthDTO> result = apiAuthManageService.saveApiAuth(apiAuth);
    if (isUpdate && result.isSuccess()) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_API_AUTH, apiAuth.getSignature());
    }
    return result;
  }

  @Operation(summary = "删除鉴权")
  @GetMapping("deleteApiAuth")
  public ResultVO<Void> deleteApiAuth(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam(name = "authId") Long authId) {
    Assert.notNull(authId, "主键 ID 不能为空");
    ApiAuthDTO apiAuth = apiAuthManageService.getApiAuth(tenantId, authId);
    if (apiAuth == null) {
      return ResultVO.success();
    }
    ResultVO<Void> result = apiAuthManageService.deleteApiAuth(tenantId, authId);
    if (result.isSuccess()) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_API_AUTH, apiAuth.getSignature());
    }
    return result;
  }

  @Operation(summary = "查询鉴权列表")
  @PostMapping("queryApiAuthList")
  public ResultVO<List<ApiAuthDTO>> queryApiAuthList(@RequestBody ApiAuthQueryParams queryParams) {
    return ResultVO.success(apiAuthManageService.queryApiAuthList(queryParams));
  }

  @Operation(summary = "查询 API 列表")
  @GetMapping("selectApiList")
  public ResultVO<List<CatalogTree<ApiDTO>>> selectApiList() {
    return ResultVO.success(apiAuthManageService.selectApiList());
  }

  @Operation(summary = "保存智能体发布信息")
  @PostMapping("saveAppPublish")
  public ResultVO<AppPublishDTO> saveAppPublish(@RequestBody AppPublishDTO publish) {
    Assert.notNull(publish.getTenantId(), "租户 ID 不能为空");
    Assert.hasText(publish.getModeType(), "模式不能为空");
    Assert.hasText(publish.getUrlPrefix(), "地址前缀不能为空");
    Assert.isTrue(!(publish.getBotId() == null && BaseConsts.CHAT_MODE_TYPE_SINGLE.equals(publish.getModeType())), "单智能体模式应用 ID 不能为空");
    return apiAuthManageService.saveAppPublish(publish);
  }

  @Operation(summary = "删除智能体发布信息")
  @GetMapping("deleteAppPublish")
  public ResultVO<Void> deleteAppPublish(@RequestParam(name = "publishId") Long publishId) {
    Assert.notNull(publishId, "主键 ID 不能为空");
    return apiAuthManageService.deleteAppPublish(publishId);
  }

  @Operation(summary = "查询智能体发布列表")
  @GetMapping("queryAppPublishList")
  public ResultVO<List<AppPublishDTO>> queryAppPublishList(@RequestParam(name = "tenantId") Long tenantId,
    @RequestParam(value = "botName", required = false) String botName, @RequestParam(value = "botId", required = false) Long botId) {
    return ResultVO.success(apiAuthManageService.queryAppPublishList(tenantId, botName, botId));
  }
}
