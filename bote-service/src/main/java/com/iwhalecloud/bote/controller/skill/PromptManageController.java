package com.iwhalecloud.bote.controller.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.skill.PromptDTO;
import com.iwhalecloud.bote.dto.skill.SimplePromptDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.service.skill.IPromptManageService;
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
 * 提示语管理
 *
 * @author qian.sisheng
 * @since 2024/8/2
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/prompt", name = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor
@Tag(name = "技能：提示词管理")
public class PromptManageController {
  private final IPromptManageService botPromptManageService;

  @Operation(summary = "保存提示语")
  @PostMapping("savePrompt")
  public ResultVO<PromptDTO> savePrompt(@RequestBody PromptDTO prompt) {
    return botPromptManageService.savePrompt(prompt);
  }

  @Operation(summary = "查询单个提示词")
  @GetMapping("findPrompt")
  public ResultVO<PromptDTO> findPrompt(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam("promptId") Long promptId) {
    Assert.notNull(promptId, "提示词ID不能为空");
    return ResultVO.success(botPromptManageService.findPrompt(tenantId, promptId));
  }

  @Operation(summary = "分页查找提示语")
  @PostMapping("queryPromptPage")
  public ResultVO<PageInfo<PromptDTO>> queryPromptPage(@RequestBody SkillQueryParams params) {
    return ResultVO.success(botPromptManageService.queryPromptPage(params));
  }

  @Operation(summary = "删除提提示词")
  @GetMapping("deletePrompt")
  public ResultVO<Void> deletePrompt(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam("promptId") Long promptId) {
    Assert.notNull(promptId, "提示词ID不能为空");
    return botPromptManageService.deletePrompt(tenantId, promptId);
  }

  @Operation(summary = "查找提示词列表", description = "用于其他配置模块，获取提示词")
  @PostMapping("queryPromptList")
  public ResultVO<List<PromptDTO>> queryPromptList(@RequestBody SkillQueryParams params) {
    return ResultVO.success(botPromptManageService.queryPromptList(params));
  }

  @Operation(summary = "同步提示词")
  @PostMapping("syncPrompt")
  public ResultVO<Void> syncPrompt(@RequestBody SimplePromptDTO simplePrompt) {
    return botPromptManageService.syncPrompt(simplePrompt);
  }

  @Operation(summary = "校验提示词")
  @PostMapping("checkPrompt")
  public ResultVO<String> checkPrompt(@RequestBody SimplePromptDTO prompt) {
    return ResultVO.success(botPromptManageService.checkPrompt(prompt));
  }
}
