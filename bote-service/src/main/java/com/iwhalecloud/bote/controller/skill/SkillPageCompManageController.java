package com.iwhalecloud.bote.controller.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.annotation.RequestCacheable;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.HttpCacheUtil;
import com.iwhalecloud.bote.common.util.IconUtil;
import com.iwhalecloud.bote.dto.skill.SkillPageCompDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageCompQueryParams;
import com.iwhalecloud.bote.service.skill.ISkillPageCompManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 技能：页面组件 Controller
 *
 * @author lizuyin
 * @since 2026-01-14
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/skill/pageComp", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "技能：页面组件管理")
public class SkillPageCompManageController {

  private final ISkillPageCompManageService skillPageCompManageService;

  @Operation(summary = "查询单个页面组件")
  @GetMapping("findSkillPageComp")
  public ResultVO<SkillPageCompDTO> findSkillPageComp(
    @Parameter(description = "租户ID") @RequestParam(value = "tenantId", required = false) Long tenantId,
    @Parameter(description = "页面组件ID") @RequestParam("pageCompId") Long pageCompId) {
    Assert.notNull(pageCompId, "页面组件ID不能为空");
    return ResultVO.success(skillPageCompManageService.findSkillPageComp(tenantId, pageCompId));
  }

  @Operation(summary = "保存页面组件（新增或更新）")
  @PostMapping("saveSkillPageComp")
  public ResultVO<SkillPageCompDTO> saveSkillPageComp(@RequestBody SkillPageCompDTO dto) {
    return skillPageCompManageService.saveSkillPageComp(dto);
  }

  @Operation(summary = "删除页面组件")
  @GetMapping("deleteSkillPageComp")
  public ResultVO<Void> deleteSkillPageComp(
    @Parameter(description = "租户ID") @RequestParam(value = "tenantId", required = false) Long tenantId,
    @Parameter(description = "页面组件ID") @RequestParam("pageCompId") Long pageCompId) {
    Assert.notNull(pageCompId, "页面组件ID不能为空");
    return skillPageCompManageService.deleteSkillPageComp(tenantId, pageCompId);
  }

  @Operation(summary = "分页查询页面组件")
  @PostMapping("querySkillPageCompPage")
  public ResultVO<PageInfo<SkillPageCompDTO>> querySkillPageCompPage(@RequestBody SkillPageCompQueryParams queryParams) {
    return ResultVO.success(skillPageCompManageService.querySkillPageCompPage(queryParams));
  }

  @Operation(summary = "获取页面组件图标")
  @GetMapping("getCompIcon")
  @RequestCacheable(sql = "SELECT updated_time FROM bt_skill_page_comp WHERE page_comp_id = #{param1}", cacheOnNotFound = true)
  @IgnoreSign
  @IgnoreSession
  public void getCompIcon(@Parameter(description = "页面组件ID", required = true) @RequestParam("pageCompId") Long pageCompId,
    @Parameter(description = "租户ID") @RequestParam(value = "tenantId", required = false) Long tenantId,
    HttpServletResponse response) throws IOException {
    Assert.notNull(pageCompId, "页面组件ID不能为空");
    SkillPageCompDTO comp = skillPageCompManageService.findSkillPageComp(tenantId, pageCompId);
    if (comp == null || StringUtils.isEmpty(comp.getCompIcon())) {
      HttpCacheUtil.sendError(response, "未配置图标");
      return;
    }
    IconUtil.sendBase64Icon(response, comp.getCompIcon());
  }
}

