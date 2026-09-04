package com.iwhalecloud.bote.controller.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.skill.SkillTextDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.service.skill.ISkillTextManageService;
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
 * 技能：文本 controller
 *
 * @author auto
 * @since 2024-09-16
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/skill/text", name = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor
@Tag(name = "技能：文本管理")
public class SkillTextManageController {
  private final ISkillTextManageService textManageService;

  @Operation(summary = "保存文本")
  @PostMapping("saveSkillText")
  public ResultVO<SkillTextDTO> saveSkillText(@RequestBody SkillTextDTO botSkillText) {
    return textManageService.saveSkillText(botSkillText);
  }

  @Operation(summary = "查询单个文本")
  @GetMapping("findSkillText")
  public ResultVO<SkillTextDTO> findSkillText(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam("textId") Long textId) {
    Assert.notNull(textId, "文本ID不能为空");
    return ResultVO.success(textManageService.findSkillText(tenantId, textId));
  }

  @Operation(summary = "查询文本列表")
  @PostMapping("querySkillTextList")
  public ResultVO<List<SkillTextDTO>> querySkillTextList(@RequestBody SkillQueryParams params) {
    return ResultVO.success(textManageService.querySkillTextList(params));
  }

  @Operation(summary = "分页查询文本")
  @PostMapping("querySkillTextPage")
  public ResultVO<PageInfo<SkillTextDTO>> querySkillTextPage(@RequestBody SkillQueryParams params) {
    return ResultVO.success(textManageService.querySkillTextPage(params));
  }

  @Operation(summary = "删除文本")
  @GetMapping("deleteSkillText")
  public ResultVO<Void> deleteSkillText(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam("textId") Long textId) {
    Assert.notNull(textId, "文本ID不能为空");
    return textManageService.deleteSkillText(tenantId, textId);
  }
}
