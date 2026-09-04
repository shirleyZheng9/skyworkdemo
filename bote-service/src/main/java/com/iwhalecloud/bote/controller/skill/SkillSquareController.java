package com.iwhalecloud.bote.controller.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.skill.InstallRequest;
import com.iwhalecloud.bote.dto.skill.InstallResponse;
import com.iwhalecloud.bote.dto.skill.SkillSquareDetailVO;
import com.iwhalecloud.bote.dto.skill.SkillSquareItemVO;
import com.iwhalecloud.bote.dto.skill.query.SkillSquareQueryParams;
import com.iwhalecloud.bote.service.skill.ISkillSquareService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * SKILL广场控制器
 *
 * @author skill-square
 * @since 2026-03-18
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "skillSquare", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@RestController
@Tag(name = "SKILL广场-AI门户")
public class SkillSquareController {

  private final ISkillSquareService skillSquareService;

  @Operation(summary = "分页查询技能广场列表")
  @PostMapping("queryPage")
  public ResultVO<PageInfo<SkillSquareItemVO>> queryPage(@RequestBody SkillSquareQueryParams params) {
    return ResultVO.success(skillSquareService.queryPage(params));
  }

  @Operation(summary = "查询技能详情")
  @GetMapping("detail")
  public ResultVO<SkillSquareDetailVO> detail(@RequestParam("skillId") Long skillId) {
    Assert.notNull(skillId, "skillId 不能为空");
    SkillSquareDetailVO detail = skillSquareService.getDetail(skillId);
    if (detail == null) {
      return ResultVO.fail("技能不存在或已下架");
    }
    return ResultVO.success(detail);
  }

  @Operation(summary = "安装技能到智能体")
  @PostMapping("install")
  public ResultVO<InstallResponse> install(@Valid @RequestBody InstallRequest request) {
    return skillSquareService.install(request);
  }

  @Operation(summary = "按照广场技能类型统计数量")
  @GetMapping("getSkillTypeStatistics")
  public ResultVO<Map<String, Integer>> getSkillTypeStatistics(@RequestParam(value = "spaceId", required = false) Long spaceId,
    @RequestParam(value = "tenantId", required = false) Long tenantId) {
    return ResultVO.success(skillSquareService.getSkillTypeStatistics(spaceId, tenantId));
  }
}
