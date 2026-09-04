package com.iwhalecloud.bote.controller.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.skill.SimpleSkillPluginDTO;
import com.iwhalecloud.bote.dto.skill.SkillPluginDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.service.skill.ISkillPluginManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * 技能：插件 controller
 *
 * @author auto
 * @since 2024-09-15
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/skill/plugin")
@RestController
@RequiredArgsConstructor
@Tag(name = "技能：插件管理")
public class SkillPluginManageController {

  private final ISkillPluginManageService pluginManageService;
  private final IRefreshCacheService refreshCacheService;

  @Operation(summary = "保存插件")
  @PostMapping("saveSkillPlugin")
  public ResultVO<SkillPluginDTO> saveSkillPlugin(@RequestBody @Valid SkillPluginDTO dto) {
    Assert.hasText(dto.getApiCode(), "API编码不能为空");
    Long apiId = dto.getApiId();
    ResultVO<SkillPluginDTO> result = pluginManageService.saveSkillPlugin(dto);
    if (apiId != null && result.isSuccess()) {
      String key = dto.getTenantId() + CacheConsts.COLON + apiId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_LLM_PLUGIN, key);
    }
    return result;
  }

  @Operation(summary = "查询单个插件")
  @GetMapping("getSkillPlugin")
  public ResultVO<SkillPluginDTO> getSkillPlugin(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam("apiId") Long apiId) {
    Assert.notNull(apiId, "apiId不能为空");
    return ResultVO.success(pluginManageService.getSkillPlugin(tenantId, apiId));
  }

  @Operation(summary = "查询插件列表")
  @PostMapping("querySkillPluginList")
  public ResultVO<List<SkillPluginDTO>> querySkillPluginList(@RequestBody SkillQueryParams params) {
    return ResultVO.success(pluginManageService.querySkillPluginList(params));
  }

  @Operation(summary = "分页查询插件")
  @PostMapping("querySkillPluginPage")
  public ResultVO<PageInfo<SkillPluginDTO>> querySkillPluginPage(@RequestBody SkillQueryParams params) {
    return ResultVO.success(pluginManageService.querySkillPluginPage(params));
  }

  @Operation(summary = "分页查询插件", description = "用于其他模块引用")
  @PostMapping("querySimpleSkillPluginPage")
  public ResultVO<PageInfo<SimpleSkillPluginDTO>> querySimpleSkillPluginPage(@RequestBody SkillQueryParams params) {
    return ResultVO.success(pluginManageService.querySimpleSkillPluginPage(params));
  }

  @Operation(summary = "删除插件")
  @GetMapping("deleteSkillPlugin")
  public ResultVO<Void> deleteSkillPlugin(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam("apiId") Long apiId) {
    Assert.notNull(apiId, "apiId不能为空");
    ResultVO<Void> result = pluginManageService.deleteSkillPlugin(tenantId, apiId);
    if (result.isSuccess()) {
      String key = tenantId + CacheConsts.COLON + apiId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_LLM_PLUGIN, key);
    }
    return result;
  }
}
