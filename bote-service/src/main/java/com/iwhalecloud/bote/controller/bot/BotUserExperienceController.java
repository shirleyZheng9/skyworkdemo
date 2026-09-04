package com.iwhalecloud.bote.controller.bot;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.dto.bot.BotUserExperienceDTO;
import com.iwhalecloud.bote.dto.bot.query.BotUserExperienceQryParams;
import com.iwhalecloud.bote.service.bot.IBotUserExperienceManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * 用户会话辅助信息管理 controller
 *
 * @author qian.sisheng
 * @since 2024/7/30
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/experience", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor
@Tag(name = "应用：用户会话辅助信息管理")
public class BotUserExperienceController {

  private final IBotUserExperienceManageService botUserExperienceService;

  @PostMapping("saveBotUserExperience")
  @Operation(description = "保存用户会话辅助信息")
  public ResultVO<BotUserExperienceDTO> saveBotUserExperience(@RequestBody @Valid BotUserExperienceDTO botUserExperience) {
    // pointAction 为 changeScene 时，content允许为空
    if ("insertMessage".equals(botUserExperience.getPointAction())) {
      Assert.hasText(botUserExperience.getContent(), "信息内容不能为空");
    }
    if ("openLink".equals(botUserExperience.getPointAction())) {
      Assert.hasText(botUserExperience.getOpenLinkType(), "链接打开方式不能为空");
    }
    return botUserExperienceService.saveBotUserExperience(botUserExperience);
  }

  @GetMapping("getBotUserExperience")
  @Operation(description = "查找单个用户会话辅助信息")
  public ResultVO<BotUserExperienceDTO> getBotUserExperience(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam("experienceId") Long experienceId) {
    Assert.notNull(experienceId, "信息ID不能为空");
    return ResultVO.success(botUserExperienceService.getBotUserExperience(tenantId, experienceId));
  }

  @PostMapping("queryBotUserExperienceList")
  @Operation(description = "查找用户会话辅助信息列表，用于会话")
  public ResultVO<List<BotUserExperienceDTO>> queryBotUserExperienceList(@RequestBody BotUserExperienceQryParams params) {
    Assert.notNull(params.getBotId(), "应用ID不能为空");
    if (params.getTenantId() == null) {
      params.setTenantId(TenantIdUtil.getTenantId());
    }
    return ResultVO.success(botUserExperienceService.queryBotUserExperienceList(params));
  }

  @GetMapping("deleteBotUserExperience")
  @Operation(description = "删除用户会话辅助信息")
  public ResultVO<Void> deleteBotUserExperience(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam("experienceId") Long experienceId) {
    Assert.notNull(experienceId, "信息ID不能为空");
    botUserExperienceService.deleteBotUserExperience(tenantId, experienceId);
    return ResultVO.success();
  }

  @PostMapping("queryBotUserExperiencePage")
  @Operation(description = "查询用户会话辅助信息(分页)")
  public ResultVO<PageInfo<BotUserExperienceDTO>> queryBotUserExperiencePage(@RequestBody BotUserExperienceQryParams params) {
    return ResultVO.success(botUserExperienceService.queryBotUserExperiencePage(params));
  }

  @PostMapping("queryBotPointList")
  @Operation(description = "查询场景关联的指令列表")
  public ResultVO<List<BotUserExperienceDTO>> queryBotPointList(BotUserExperienceQryParams params) {
    return ResultVO.success(botUserExperienceService.queryBotPointList(params));
  }
}
