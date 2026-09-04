package com.iwhalecloud.bote.controller.plugin;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.plugin.PluginAuthDTO;
import com.iwhalecloud.bote.dto.plugin.params.PluginAuthQueryParams;
import com.iwhalecloud.bote.service.plugin.IPluginAuthManageService;
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
 * 插件鉴权管理 controller
 *
 * @author chen.linfa
 * @since 2025-12-09
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/pluginAuth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "插件鉴权管理")
public class PluginAuthManageController {

  private final IPluginAuthManageService pluginAuthManageService;

  @Operation(summary = "查询单个插件鉴权")
  @GetMapping("getPluginAuth")
  public ResultVO<PluginAuthDTO> getPluginAuth(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam(name = "authId") Long authId) {
    Assert.notNull(authId, "主键 ID 不能为空");
    return ResultVO.success(pluginAuthManageService.getPluginAuth(tenantId, authId));
  }

  @Operation(summary = "保存插件鉴权")
  @PostMapping("savePluginAuth")
  public ResultVO<PluginAuthDTO> savePluginAuth(@RequestBody PluginAuthDTO auth) {
    return pluginAuthManageService.savePluginAuth(auth);
  }

  @Operation(summary = "查询插件鉴权列表")
  @PostMapping("queryPluginAuthList")
  public ResultVO<List<PluginAuthDTO>> queryPluginAuthList(@RequestBody PluginAuthQueryParams queryParams) {
    return ResultVO.success(pluginAuthManageService.queryPluginAuthList(queryParams));
  }
}
