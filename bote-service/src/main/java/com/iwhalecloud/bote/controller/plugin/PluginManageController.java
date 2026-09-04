package com.iwhalecloud.bote.controller.plugin;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.cache.PluginIconCache;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.dto.plugin.PluginDTO;
import com.iwhalecloud.bote.dto.plugin.request.QueryCatalogRequest;
import com.iwhalecloud.bote.dto.plugin.request.QueryPluginRequest;
import com.iwhalecloud.bote.dto.plugin.request.SubscribePluginRequest;
import com.iwhalecloud.bote.dto.plugin.request.TestPluginToolRequest;
import com.iwhalecloud.bote.dto.plugin.response.PluginDefinition;
import com.iwhalecloud.bote.dto.plugin.response.PluginToolSpec;
import com.iwhalecloud.bote.dto.skill.query.PluginQueryParams;
import com.iwhalecloud.bote.service.plugin.IPluginManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
 * 插件管理 controller
 *
 * @author auto
 * @since 2025-04-01
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/plugin", name = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "插件管理")
public class PluginManageController {

  private final IPluginManageService pluginManageService;
  private final PluginIconCache pluginIconCache;
  private final IRefreshCacheService refreshCacheService;

  @Operation(summary = "查询单个插件（旧）")
  @GetMapping("findPlugin")
  public ResultVO<PluginDTO> findPlugin(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam(name = "pluginId") Long pluginId) {
    Assert.notNull(pluginId, "主键 ID 不能为空");
    return ResultVO.success(pluginManageService.findPlugin(tenantId, pluginId));
  }

  @Operation(summary = "保存插件（旧）")
  @PostMapping("savePlugin")
  public ResultVO<PluginDTO> savePlugin(@RequestBody PluginDTO plugin) {
    return pluginManageService.savePlugin(plugin);
  }

  @Operation(summary = "删除插件（旧）")
  @GetMapping("deletePlugin")
  public ResultVO<Void> deletePlugin(@RequestParam(name = "pluginId") Long pluginId) {
    Assert.notNull(pluginId, "主键 ID 不能为空");
    return pluginManageService.deletePlugin(pluginId);
  }

  @Operation(summary = "查询插件列表（旧）")
  @PostMapping("queryPluginList")
  public ResultVO<List<PluginDTO>> queryPluginList(@RequestBody PluginQueryParams queryParams) {
    return ResultVO.success(pluginManageService.queryPluginList(queryParams));
  }

  @Operation(summary = "分页查询插件（旧）")
  @PostMapping("queryPluginPage")
  public ResultVO<PageInfo<PluginDTO>> queryPluginPage(@RequestBody PluginQueryParams queryParams) {
    return ResultVO.success(pluginManageService.queryPluginPage(queryParams));
  }

  @Operation(summary = "插件上下架（旧）")
  @GetMapping("publishPlugin")
  public ResultVO<Void> publishPlugin(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam(name = "pluginId") Long pluginId, @RequestParam(name = "status") String status) {
    return pluginManageService.publishPlugin(tenantId, pluginId, status);
  }

  //------ 插件市场 ------//
  @Operation(summary = "同步门户开发者信息")
  @GetMapping("syncPortalUserInfo")
  public ResultVO<Void> syncPortalUserInfo(@RequestParam("tenantId") Long tenantId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    pluginManageService.syncPortalUserInfo(tenantId);
    return ResultVO.success();
  }

  @Operation(summary = "查询插件目录")
  @PostMapping("queryPluginCatalog")
  public ResultVO<List<CatalogDTO>> queryPluginCatalog(@RequestBody QueryCatalogRequest request) {
    Assert.notNull(request.getTenantId(), "租户 ID 不能为空");
    request.setCatalogType(CatalogConsts.TYPE_PLUGIN);
    return ResultVO.success(pluginManageService.queryPluginCatalog(request));
  }

  @Operation(summary = "查询授权的插件列表（分页）")
  @PostMapping("queryAuthPluginPage")
  public ResultVO<PageInfo<PluginDefinition>> queryAuthPluginPage(@RequestBody QueryPluginRequest request) {
    Assert.notNull(request.getTenantId(), "租户 ID 不能为空");
    return ResultVO.success(pluginManageService.queryAuthPluginPage(request));
  }

  @Operation(summary = "根据插件分类统计插件数量")
  @PostMapping("countAuthPluginByCatalog")
  public ResultVO<Map<String, Long>> countAuthPluginByCatalog(@RequestBody QueryPluginRequest request) {
    Assert.notNull(request.getTenantId(), "租户 ID 不能为空");
    return ResultVO.success(pluginManageService.countAuthPluginByCatalog(request));
  }

  @Operation(summary = "查询插件定义")
  @GetMapping("getPluginDefinition")
  public ResultVO<PluginDefinition> getPluginDefinition(@RequestParam(name = "tenantId") Long tenantId,
    @RequestParam(name = "pluginId") Long pluginId, @RequestParam(name = "includeTools", required = false) Boolean includeTool) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(pluginId, "插件 ID 不能为空");
    return pluginManageService.getPluginDefinition(tenantId, pluginId, includeTool);
  }

  @Operation(summary = "查询插件的工具列表")
  @GetMapping("getPluginTools")
  public ResultVO<List<PluginToolSpec>> getPluginTools(@RequestParam(name = "tenantId") Long tenantId,
    @RequestParam(name = "pluginId") Long pluginId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(pluginId, "插件 ID 不能为空");
    return pluginManageService.getPluginTools(tenantId, pluginId);
  }

  @Operation(summary = "测试调用插件工具")
  @PostMapping("callPluginTool")
  public ResultVO<Object> callPluginTool(@RequestBody TestPluginToolRequest request) {
    Assert.notNull(request.getTenantId(), "租户 ID 不能为空");
    Assert.notNull(request.getPluginId(), "插件 ID 不能为空");
    Assert.hasLength(request.getToolName(), "工具名称不能为空");
    return pluginManageService.callPluginTool(request);
  }

  @Operation(summary = "订阅插件")
  @PostMapping("subscribePlugin")
  public ResultVO<Void> subscribePlugin(@RequestBody SubscribePluginRequest request) {
    Assert.notNull(request.getTenantId(), "租户 ID 不能为空");
    Assert.notNull(request.getPluginId(), "插件 ID 不能为空");
    return pluginManageService.subscribePlugin(request);
  }

  @Operation(summary = "取消订阅插件")
  @GetMapping("unsubscribePlugin")
  public ResultVO<Void> unsubscribePlugin(@RequestParam(name = "tenantId") Long tenantId, @RequestParam(name = "pluginId") Long pluginId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(pluginId, "插件 ID 不能为空");
    ResultVO<Void> voidResultVO = pluginManageService.unsubscribePlugin(tenantId, pluginId);
    if (voidResultVO.isSuccess()) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_PLUGIN_HUB, List.of(tenantId + CacheConsts.COLON + pluginId));
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_PLUGIN_HUB_MCP, List.of(tenantId + ""));
    }
    return voidResultVO;
  }

  @IgnoreSign
  @IgnoreSession
  @Operation(summary = "查询插件图标")
  @GetMapping(value = "queryPluginIcon", produces = MediaType.ALL_VALUE)
  public void queryPluginIcon(@RequestParam(name = "tenantId") Long tenantId, @RequestParam(name = "pluginId") Long pluginId,
    HttpServletRequest request, HttpServletResponse response) throws IOException {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(pluginId, "插件 ID 不能为空");
    pluginIconCache.sendIcon(tenantId, pluginId, request, response);
  }

}
