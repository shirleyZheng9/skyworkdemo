package com.iwhalecloud.bote.controller.bot;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.bot.BotFavorDTO;
import com.iwhalecloud.bote.service.bot.IBotFavorManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 应用收藏 controller
 *
 * @author chen.linfa
 * @since 2024-08-02
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/favor", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "应用：收藏")
public class BotFavorManageController {

  private final IBotFavorManageService botFavorService;

  @GetMapping("addBotFavor")
  @Operation(summary = "添加收藏")
  public ResultVO<Void> addBotFavor(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @Parameter(description = "应用 ID", required = true) @RequestParam("botId") Long botId) {
    Assert.notNull(botId, "应用 ID 不能为空");
    return botFavorService.addBotFavor(tenantId, botId);
  }

  @GetMapping("cancelBotFavor")
  @Operation(summary = "取消收藏")
  public ResultVO<Void> cancelBotFavor(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @Parameter(description = "应用 ID", required = true) @RequestParam("botId") Long botId) {
    Assert.notNull(botId, "应用 ID 不能为空");
    return botFavorService.cancelBotFavor(tenantId, botId);
  }

  @GetMapping("queryBotFavors")
  @Operation(summary = "查询收藏列表")
  public ResultVO<List<BotFavorDTO>> queryBotFavorList(@Parameter(description = "租户 ID", required = true) @RequestParam("tenantId") Long tenantId,
    @Parameter(description = "用户 ID", required = true) @RequestParam("userId") Long userId) {
    Assert.notNull(userId, "用户 ID 不能为空");
    Assert.notNull(tenantId, "租户 ID 不能为空");
    return ResultVO.success(botFavorService.queryBotFavorList(tenantId, userId));
  }
}
