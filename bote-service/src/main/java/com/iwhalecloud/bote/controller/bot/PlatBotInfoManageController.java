package com.iwhalecloud.bote.controller.bot;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.annotation.RequestCacheable;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.HttpCacheUtil;
import com.iwhalecloud.bote.common.util.IconUtil;
import com.iwhalecloud.bote.dto.bot.PlatBotInfoDTO;
import com.iwhalecloud.bote.dto.bot.TemplateBotDTO;
import com.iwhalecloud.bote.dto.bot.query.PlatBotInfoQueryParams;
import com.iwhalecloud.bote.service.bot.IPlatBotInfoManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
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
 * 模板应用管理 controller
 *
 * @author auto
 * @since 2025-05-29
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/platBotInfo", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "模板应用管理")
public class PlatBotInfoManageController {

  private final IPlatBotInfoManageService platBotInfoManageService;

  @Operation(summary = "查询单个平台模板应用")
  @GetMapping("findPlatBotInfo")
  public ResultVO<TemplateBotDTO> findPlatBotInfo(@RequestParam(name = "platBotId") Long platBotId) {
    Assert.notNull(platBotId, "主键 ID 不能为空");
    return ResultVO.success(platBotInfoManageService.findPlatBotInfo(platBotId));
  }

  @Operation(summary = "根据类型查询应用欢迎页图标")
  @GetMapping("getPlatBotIcon")
  @RequestCacheable(sql = "SELECT updated_time FROM bt_plat_bot_info WHERE plat_bot_id = #{param1}", cacheOnNotFound = true)
  @IgnoreSign
  @IgnoreSession
  public void getPlatBotIcon(@Parameter(description = "平台应用 ID", required = true) @RequestParam("platBotId") Long platBotId,
    HttpServletResponse response) throws IOException {
    Assert.notNull(platBotId, "平台应用 ID 不能为空");
    TemplateBotDTO bot = platBotInfoManageService.findPlatBotInfo(platBotId);
    if (bot == null || StringUtils.isEmpty(bot.getBotIcon())) {
      HttpCacheUtil.sendError(response, "未配置图标");
      return;
    }
    IconUtil.sendBase64Icon(response, bot.getBotIcon());
  }

  @Operation(summary = "查询单个用户模板应用")
  @GetMapping("findUserBotInfo")
  public ResultVO<TemplateBotDTO> findUserBotInfo(@RequestParam(name = "platBotId") Long platBotId) {
    Assert.notNull(platBotId, "主键 ID 不能为空");
    return ResultVO.success(platBotInfoManageService.findUserBotInfo(platBotId));
  }

  @Operation(summary = "保存模板应用")
  @PostMapping("savePlatBotInfo")
  public ResultVO<PlatBotInfoDTO> savePlatBotInfo(@RequestBody PlatBotInfoDTO platBotInfo) {
    return platBotInfoManageService.savePlatBotInfo(platBotInfo);
  }

  @Operation(summary = "删除模板应用")
  @GetMapping("deletePlatBotInfo")
  public ResultVO<Void> deletePlatBotInfo(@RequestParam(name = "platBotId") Long platBotId) {
    Assert.notNull(platBotId, "主键 ID 不能为空");
    return platBotInfoManageService.deletePlatBotInfo(platBotId);
  }

  @Operation(summary = "查询模板应用列表")
  @PostMapping("queryPlatBotInfoList")
  public ResultVO<List<PlatBotInfoDTO>> queryPlatBotInfoList(@RequestBody PlatBotInfoQueryParams queryParams) {
    return ResultVO.success(platBotInfoManageService.queryPlatBotInfoList(queryParams));
  }

  @Operation(summary = "分页查询模板应用")
  @PostMapping("queryPlatBotInfoPage")
  public ResultVO<PageInfo<PlatBotInfoDTO>> queryPlatBotInfoPage(@RequestBody PlatBotInfoQueryParams queryParams) {
    return ResultVO.success(platBotInfoManageService.queryPlatBotInfoPage(queryParams));
  }

  @Operation(summary = "分页查询模板应用，查询平台和用户发布的应用模板")
  @PostMapping("queryPlatAndUserBotPage")
  public ResultVO<PageInfo<TemplateBotDTO>> queryPlatAndUserBotPage(@RequestBody PlatBotInfoQueryParams queryParams) {
    // 运行态中使用 spaceId 作为 tenantId
    if (queryParams.getSpaceId() != null) {
      queryParams.setTenantId(queryParams.getSpaceId());
    }
    return ResultVO.success(platBotInfoManageService.queryPlatAndUserBotPage(queryParams));
  }

  @Operation(summary = "上下架模板应用")
  @GetMapping("publishPlatBot")
  public ResultVO<Void> publishPlatBot(@RequestParam(name = "platBotId") Long platBotId, @RequestParam(name = "status") String status) {
    return platBotInfoManageService.publishPlatBot(platBotId, status);
  }

  @Operation(summary = "下架用户发布的应用模板")
  @GetMapping("removeBot")
  public ResultVO<Void> removeBot(@RequestParam(name = "botId") Long botId) {
    return platBotInfoManageService.removeBot(botId);
  }

  @Operation(summary = "应用广场：分页查询用户发布的应用")
  @PostMapping("queryUserAuthBotPage")
  public ResultVO<PageInfo<TemplateBotDTO>> queryUserAuthBotPage(@RequestBody PlatBotInfoQueryParams queryParams) {
    Assert.notNull(queryParams.getSpaceId(), "空间 ID 不能为空");
    // 运行态中使用 spaceId 作为 tenantId
    queryParams.setTenantId(queryParams.getSpaceId());
    return ResultVO.success(platBotInfoManageService.queryUserAuthBotPage(queryParams));
  }
}
