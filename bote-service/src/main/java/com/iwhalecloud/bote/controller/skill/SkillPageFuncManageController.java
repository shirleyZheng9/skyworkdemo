package com.iwhalecloud.bote.controller.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.skill.SimpleSkillPageFuncDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageFuncDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.service.skill.ISkillPageFuncManageService;
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
 * 技能：页面函数 controller
 *
 * @author auto
 * @since 2024-09-15
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/skill/pageFunc", name = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor
@Tag(name = "技能：页面函数管理")
public class SkillPageFuncManageController {
  private final ISkillPageFuncManageService pageFuncManageService;

  private final IRefreshCacheService refreshCacheService;

  @Operation(summary = "保存页面函数")
  @PostMapping("saveSkillPageFunc")
  public ResultVO<SkillPageFuncDTO> saveSkillPageFunc(@RequestBody @Valid SkillPageFuncDTO pageFunc) {
    Long pageFuncId = pageFunc.getPageFuncId();
    ResultVO<SkillPageFuncDTO> result = pageFuncManageService.saveSkillPageFunc(pageFunc);
    if (pageFuncId != null && result.isSuccess()) {
      String key = pageFunc.getTenantId() + CacheConsts.COLON + pageFuncId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_PAGE_FUNC, key);
    }
    return result;
  }

  @Operation(summary = "查询单个页面函数")
  @GetMapping("findSkillPageFunc")
  public ResultVO<SkillPageFuncDTO> findSkillPageFunc(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam("funcId") Long funcId) {
    Assert.notNull(funcId, "页面函数ID不能为空");
    return ResultVO.success(pageFuncManageService.findSkillPageFunc(tenantId, funcId));
  }

  @Operation(summary = "查找页面函数列表", description = "用于其他模块引用")
  @PostMapping("querySkillPageFuncList")
  public ResultVO<List<SimpleSkillPageFuncDTO>> querySkillPageFuncList(@RequestBody SkillQueryParams params) {
    return ResultVO.success(pageFuncManageService.querySkillPageFuncList(params));
  }

  @Operation(summary = "分页查询页面函数")
  @PostMapping("querySkillPageFuncPage")
  public ResultVO<PageInfo<SkillPageFuncDTO>> querySkillPageFuncPage(@RequestBody SkillQueryParams params) {
    return ResultVO.success(pageFuncManageService.querySkillPageFuncPage(params));
  }

  @Operation(summary = "分页查询页面函数", description = "用于其他模块引用")
  @PostMapping("querySimpleSkillPageFuncPage")
  public ResultVO<PageInfo<SimpleSkillPageFuncDTO>> querySimpleSkillPageFuncPage(@RequestBody SkillQueryParams params) {
    return ResultVO.success(pageFuncManageService.querySimpleSkillPageFuncPage(params));
  }

  @Operation(summary = "删除页面函数")
  @GetMapping("deleteSkillPageFunc")
  public ResultVO<Void> deleteSkillPageFunc(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam("pageFuncId") Long pageFuncId) {
    Assert.notNull(pageFuncId, "页面函数ID不能为空");
    ResultVO<Void> result = pageFuncManageService.deleteSkillPageFunc(tenantId, pageFuncId);
    if (result.isSuccess()) {
      String key = tenantId + CacheConsts.COLON + pageFuncId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_PAGE_FUNC, key);
    }
    return result;
  }
}
