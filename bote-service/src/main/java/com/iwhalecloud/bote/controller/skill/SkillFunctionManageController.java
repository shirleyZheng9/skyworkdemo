package com.iwhalecloud.bote.controller.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.skill.FunctionTestParams;
import com.iwhalecloud.bote.dto.skill.SimpleSkillFunctionDTO;
import com.iwhalecloud.bote.dto.skill.SkillFunctionDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.service.skill.ISkillFunctionManageService;
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
 * 技能：服务函数 controller
 *
 * @author auto
 * @since 2024-09-15
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/skill/function", name = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@RestController
@Tag(name = "技能：函数管理")
public class SkillFunctionManageController {
  private final ISkillFunctionManageService functionManageService;

  private final IRefreshCacheService refreshCacheService;

  @Operation(summary = "保存函数")
  @PostMapping("saveSkillFunction")
  public ResultVO<SkillFunctionDTO> saveSkillFunction(@RequestBody @Valid SkillFunctionDTO botSkillFunction) {
    Long funcId = botSkillFunction.getFuncId();
    Assert.hasText(botSkillFunction.getFuncCode(), "函数名称不能为空");
    Assert.hasText(botSkillFunction.getFuncName(), "函数编码不能为空");
    ResultVO<SkillFunctionDTO> result = functionManageService.saveSkillFunction(botSkillFunction);
    if (funcId != null && result.isSuccess()) {
      String key = botSkillFunction.getTenantId() + CacheConsts.COLON + funcId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_TOOLBOX, key);
    }
    return result;
  }

  @Operation(summary = "查找单个函数")
  @GetMapping("findSkillFunction")
  public ResultVO<SkillFunctionDTO> findSkillFunction(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam("funcId") Long funcId) {
    Assert.notNull(funcId, "函数ID不能为空");
    return ResultVO.success(functionManageService.findSkillFunction(tenantId, funcId));
  }

  @Operation(summary = "查找函数列表", description = "用于其他模块引用")
  @PostMapping("querySkillFunctionList")
  public ResultVO<List<SimpleSkillFunctionDTO>> querySkillFunctionList(@RequestBody SkillQueryParams params) {
    return ResultVO.success(functionManageService.querySkillFunctionList(params));
  }

  @Operation(summary = "分页查找函数")
  @PostMapping("querySkillFunctionPage")
  public ResultVO<PageInfo<SkillFunctionDTO>> querySkillFunctionPage(@RequestBody SkillQueryParams params) {
    return ResultVO.success(functionManageService.querySkillFunctionPage(params));
  }

  @Operation(summary = "分页查找函数", description = "用于其他模块引用")
  @PostMapping("querySimpleSkillFunctionPage")
  public ResultVO<PageInfo<SimpleSkillFunctionDTO>> querySimpleSkillFunctionPage(@RequestBody SkillQueryParams params) {
    return ResultVO.success(functionManageService.querySimpleSkillFunctionPage(params));
  }

  @Operation(summary = "删除函数")
  @GetMapping("deleteSkillFunction")
  public ResultVO<Void> deleteSkillFunction(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam("funcId") Long funcId) {
    Assert.notNull(funcId, "函数ID不能为空");
    ResultVO<Void> result = functionManageService.deleteSkillFunction(tenantId, funcId);
    if (result.isSuccess()) {
      String key = tenantId + CacheConsts.COLON + funcId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_TOOLBOX, key);
    }
    return result;
  }

  @Operation(summary = "函数测试")
  @PostMapping("test")
  public ResultVO<Object> test(@RequestBody FunctionTestParams params) {
    Assert.hasText(params.getScriptJson(), "脚本不能为空");
    Assert.hasText(params.getFuncType(), "函数类型不能为空");
    return functionManageService.test(params);
  }
}
