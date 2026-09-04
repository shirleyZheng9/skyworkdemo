package com.iwhalecloud.bote.controller.portal;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.dto.portal.WeKnoraAccountSettingDTO;
import com.iwhalecloud.bote.dto.portal.AccountDTO;
import com.iwhalecloud.bote.dto.portal.KnowledgeGraphAccountSettingDTO;
import com.iwhalecloud.bote.dto.portal.TenantSettingInfoDTO;
import com.iwhalecloud.bote.dto.portal.query.TenantQueryParams;
import com.iwhalecloud.bote.service.portal.ITenantSettingInfoManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * 租户设置信息管理 controller
 *
 * @author auto
 * @since 2024-12-18
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/tenantSettingInfo", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "门户：租户设置信息管理")
public class TenantSettingInfoManageController {

  private final ITenantSettingInfoManageService service;

  private final IRefreshCacheService refreshCacheService;

  @Operation(summary = "根据租户 ID、机器人 ID，收集设置信息", description = "用于对话窗口个性化渲染，优先使用机器人的定义")
  @GetMapping("findSettingInfo")
  public ResultVO<Map<String, String>> findSettingInfo(@RequestParam(name = "tenantId") Long tenantId,
    @RequestParam(name = "botId", required = false) Long botId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    return ResultVO.success(service.findSettingInfo(tenantId, botId));
  }

  @Operation(summary = "查询单个租户设置信息")
  @GetMapping("findTenantSettingInfo")
  public ResultVO<TenantSettingInfoDTO> findTenantSettingInfo(@RequestParam(name = "tenantId") Long tenantId,
    @RequestParam(name = "funcType") String funcType) {
    Assert.hasText(funcType, "功能类型 不能为空");
    return ResultVO.success(service.findTenantSettingInfo(tenantId, funcType));
  }

  @Operation(summary = "保存意租户设置信息")
  @PostMapping("saveTenantSettingInfo")
  public ResultVO<TenantSettingInfoDTO> saveTenantSettingInfo(@RequestBody TenantSettingInfoDTO setting) {
    ResultVO<TenantSettingInfoDTO> result = service.saveTenantSettingInfo(setting);
    if (result.isSuccess()) {
      String tenantId = setting.getTenantId().toString();
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_TENANT_SETTING, tenantId);
      // 知识图谱配置变更时，同步失效 SSO 登录令牌缓存
      if (CommonConsts.FUNC_TYPE_KNOWLEDGE_GRAPH.equals(setting.getFuncType())) {
        refreshCacheService.refresh(CacheConsts.CACHE_NAME_KNOWLEDGE_GRAPH_LOGIN_INFO, tenantId);
      }
    }
    return result;
  }

  @Operation(summary = "查询租户设置信息列表")
  @GetMapping("queryTenantSettingInfoList")
  public ResultVO<List<TenantSettingInfoDTO>> queryTenantSettingInfoList(@RequestParam(name = "tenantId") Long tenantId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    return ResultVO.success(service.queryTenantSettingInfoList(tenantId));
  }

  @Operation(summary = "批量查询租户设置信息")
  @PostMapping("batchGetTenantSettingInfo")
  public ResultVO<Map<String, TenantSettingInfoDTO>> batchGetTenantSettingInfo(@RequestBody TenantQueryParams queryParams) {
    return service.batchGetTenantSettingInfo(queryParams);
  }

  @Operation(summary = "测试DocChain账户")
  @PostMapping("testDocChainAccount")
  public ResultVO<Void> testDocChainAccount(@RequestBody AccountDTO account) {
    Assert.hasText(account.getUsername(), "用户名 不能为空");
    Assert.hasText(account.getToken(), "密码 不能为空");
    return service.testDocChainAccount(account);
  }

  @Operation(summary = "同步DocChain账户")
  @PostMapping("syncDocChainAccount")
  public ResultVO<String> syncDocChainAccount(@RequestBody AccountDTO account) {
    Assert.hasText(account.getUsername(), "用户名 不能为空");
    Assert.hasText(account.getToken(), "密码 不能为空");
    ResultVO<String> result = service.syncDocChainAccount(account);
    if (result.isSuccess()) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_TENANT_SETTING, account.getTenantId().toString());
    }
    return result;
  }

  @Operation(summary = "测试Weknora账户")
  @PostMapping("testWeknoraAccount")
  public ResultVO<Void> testWeknoraAccount(@RequestBody WeKnoraAccountSettingDTO account) {
    Assert.hasText(account.getEmail(), "邮箱 不能为空");
    Assert.hasText(account.getPassword(), "密码 不能为空");
    return service.testWeknoraAccount(account);
  }

  @Operation(summary = "注册Weknora账户")
  @PostMapping("registerWeknoraAccount")
  public ResultVO<Void> registerWeknoraAccount(@RequestBody WeKnoraAccountSettingDTO account) {
    Assert.notNull(account.getTenantId(), "租户 不能为空");
    Assert.hasText(account.getUserName(), "用户名 不能为空");
    Assert.hasText(account.getEmail(), "邮箱 不能为空");
    Assert.hasText(account.getPassword(), "密码 不能为空");
    return service.registerWeknoraAccount(account);
  }

  @Operation(summary = "修改Weknora账号密码")
  @PostMapping("updateWeknoraAccountPw")
  public ResultVO<Void> updateWeknoraAccountPw(@RequestBody WeKnoraAccountSettingDTO account) {
    Assert.notNull(account.getTenantId(), "租户 不能为空");
    Assert.hasText(account.getPassword(), "密码 不能为空");
    return service.updateWeknoraAccountPw(account);
  }

  @Operation(summary = "测试knowledgeGraph登录")
  @PostMapping("testKnowledgeGraphAccount")
  public ResultVO<Void> testKnowledgeGraphAccount(@RequestBody KnowledgeGraphAccountSettingDTO account) {
    Assert.notNull(account.getTenantId(), "租户 不能为空");
    Assert.hasText(account.getProjectId(), "projectId 不能为空");
    Assert.hasText(account.getProjectUserId(), "projectUserId 不能为空");
    Assert.hasText(account.getProjectUserName(), "projectUserName 不能为空");
    return service.testKnowledgeGraphAccount(account);
  }
}
