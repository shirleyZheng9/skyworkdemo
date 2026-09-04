package com.iwhalecloud.bote.controller.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.skill.SkillPublishApplyDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillPublishApplyQueryParams;
import com.iwhalecloud.bote.service.skill.ISkillPublishApplyService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 技能发布申请 controller
 *
 * @author wangtingyun
 * @since 2026-04-03
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/skillPublishApply", name = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "技能发布申请管理")
public class SkillPublishApplyController {

  private final ISkillPublishApplyService skillPublishApplyManagerService;

  @Operation(summary = "保存技能发布申请")
  @PostMapping("saveSkillPublishApply")
  public ResultVO<SkillPublishApplyDTO> saveSkillPublishApply(@RequestBody SkillPublishApplyDTO dto) {
    Assert.notNull(dto.getSpaceId(), "空间 ID 不能为空");
    return skillPublishApplyManagerService.saveSkillPublishApply(dto);
  }

  @Operation(summary = "根据主键获取技能发布申请")
  @GetMapping("getSkillPublishApply")
  public ResultVO<SkillPublishApplyDTO> getSkillPublishApply(@RequestParam("applyId") Long applyId) {
    Assert.notNull(applyId, "主键不能为空");
    return ResultVO.success(skillPublishApplyManagerService.getSkillPublishApply(applyId));
  }

  @Operation(summary = "删除技能发布申请")
  @DeleteMapping("deleteSkillPublishApply")
  public ResultVO<Void> deleteSkillPublishApply(@RequestParam("applyId") Long applyId) {
    Assert.notNull(applyId, "主键不能为空");
    return skillPublishApplyManagerService.deleteSkillPublishApply(applyId);
  }

  @Operation(summary = "查询技能发布申请审核列表（管理端使用）")
  @PostMapping("querySkillPublishApplyAuditPage")
  public ResultVO<PageInfo<SkillPublishApplyDTO>> querySkillPublishApplyAuditPage(@RequestBody SkillPublishApplyQueryParams params) {
    return ResultVO.success(skillPublishApplyManagerService.getSkillPublishApplyAuditPage(params));
  }

  @Operation(summary = "查询用户发布的技能申请列表")
  @PostMapping("queryUserSkillPublishApplyPage")
  public ResultVO<PageInfo<SkillPublishApplyDTO>> queryUserSkillPublishApplyPage(@RequestBody SkillPublishApplyQueryParams params) {
    Assert.notNull(params.getSpaceId(), "空间 ID 不能为空");
    return ResultVO.success(skillPublishApplyManagerService.getUserSkillPublishApplyPage(params));
  }

  @Operation(summary = "审批技能发布申请")
  @PostMapping("auditSkillPublishApply")
  public ResultVO<Void> auditSkillPublishApply(@RequestBody SkillPublishApplyDTO applyDTO) {
    return skillPublishApplyManagerService.auditSkillPublishApply(applyDTO);
  }
}
