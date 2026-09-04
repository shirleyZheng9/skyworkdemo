package com.iwhalecloud.bote.controller.bot;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.bot.BotSceneRelDTO;
import com.iwhalecloud.bote.dto.bot.query.BotQueryParams;
import com.iwhalecloud.bote.service.bot.IBotRelaManageService;
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
 * 应用关联管理 controller
 *
 * @author chen.linfa
 * @since 2025-04-24
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/botRela", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "应用：关联管理")
public class BotRelaManageController {
  private final IBotRelaManageService botRelaManageService;

  @Operation(summary = "添加智能体")
  @PostMapping("addBotSceneRel")
  public ResultVO<Void> addBotSceneRel(@RequestBody BotQueryParams params) {
    Assert.notNull(params.getBotId(), "应用 ID 不能为空");
    Assert.notEmpty(params.getSceneIds(), "智能体不能为空");
    return botRelaManageService.addBotSceneRel(params.getTenantId(), params.getBotId(), params.getSceneIds());
  }

  @Operation(summary = "移除智能体")
  @GetMapping("removeBotSceneRel")
  public ResultVO<Void> removeBotSceneRel(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam("botId") Long botId, @RequestParam("sceneId") Long sceneId) {
    Assert.notNull(botId, "应用 ID 不能为空");
    Assert.notNull(sceneId, "智能体 ID 不能为空");
    return botRelaManageService.removeBotSceneRel(tenantId, botId, sceneId);
  }

  @Operation(summary = "设置默认智能体")
  @GetMapping("setDefaultBotSceneRel")
  public ResultVO<Void> setDefaultBotSceneRel(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam("botId") Long botId, @RequestParam("sceneId") Long sceneId) {
    Assert.notNull(botId, "应用 ID 不能为空");
    Assert.notNull(sceneId, "智能体 ID 不能为空");
    return botRelaManageService.setDefaultBotSceneRel(tenantId, botId, sceneId);
  }

  @Operation(summary = "取消默认智能体")
  @GetMapping("cancelDefaultBotSceneRel")
  public ResultVO<Void> cancelDefaultBotSceneRel(@RequestParam("tenantId") Long tenantId, @RequestParam("botId") Long botId) {
    Assert.notNull(botId, "应用 ID 不能为空");
    Assert.notNull(tenantId, "租户 ID 不能为空");
    return botRelaManageService.cancelDefaultBotSceneRel(tenantId, botId);
  }

  @Operation(summary = "查询应用关联的场景列表(分页)")
  @PostMapping("queryBotSceneRelPage")
  public ResultVO<PageInfo<BotSceneRelDTO>> queryBotSceneRelPage(@RequestBody BotQueryParams queryParams) {
    return ResultVO.success(botRelaManageService.queryBotSceneRelPage(queryParams));
  }

  @Operation(summary = "查询应用关联的场景列表")
  @PostMapping("queryBotSceneRelList")
  public ResultVO<List<BotSceneRelDTO>> queryBotSceneRelList(@RequestBody BotQueryParams queryParams) {
    return ResultVO.success(botRelaManageService.queryBotSceneRelList(queryParams));
  }

  @Operation(summary = "查询应用下的规划智能体")
  @GetMapping("queryPlanAgentList")
  public ResultVO<List<BotSceneRelDTO>> queryPlanAgentList(@RequestParam(value = "tenantId", required = false) Long tenantId) {
    return ResultVO.success(botRelaManageService.queryPlanAgentList(tenantId));
  }
}
