package com.iwhalecloud.bote.controller.chat;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.chat.ChatThemeDTO;
import com.iwhalecloud.bote.dto.chat.query.ChatThemeQueryParams;
import com.iwhalecloud.bote.service.chat.IChatThemeService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 聊天主题管理 controller
 *
 * @author tingyun.wang
 * @since 2025-07-23
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/chatTheme", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "聊天：主题管理")
public class ChatThemeController {

  private final IChatThemeService chatThemeService;

  @Operation(summary = "新增主题")
  @PostMapping("addTheme")
  public ResultVO<ChatThemeDTO> addTheme(@RequestBody ChatThemeDTO chatThemeDTO) {
    Assert.notNull(chatThemeDTO.getBotId(), "智能应用ID不能为空");
    Assert.hasText(chatThemeDTO.getThemeName(), "主题名称不能为空");
    Assert.notNull(chatThemeDTO.getTenantId(), "租户ID不能为空");
    return chatThemeService.addTheme(chatThemeDTO);
  }

  @Operation(summary = "修改主题")
  @PostMapping("updateTheme")
  public ResultVO<ChatThemeDTO> updateTheme(@RequestBody ChatThemeDTO chatThemeDTO) {
    Assert.notNull(chatThemeDTO.getBotId(), "智能应用ID不能为空");
    Assert.notNull(chatThemeDTO.getThemeId(), "主题ID不能为空");
    Assert.notNull(chatThemeDTO.getTenantId(), "租户ID不能为空");
    return chatThemeService.updateTheme(chatThemeDTO);
  }

  @Operation(summary = "重命名主题")
  @PostMapping("renameTheme")
  public ResultVO<ChatThemeDTO> renameTheme(@RequestBody ChatThemeDTO chatThemeDTO) {
    Assert.notNull(chatThemeDTO.getBotId(), "智能应用ID不能为空");
    Assert.notNull(chatThemeDTO.getThemeId(), "主题ID不能为空");
    Assert.notNull(chatThemeDTO.getTenantId(), "租户ID不能为空");
    chatThemeService.renameTheme(chatThemeDTO);
    return ResultVO.success();
  }

  @Operation(summary = "主题列表查询")
  @GetMapping("getThemeList")
  public ResultVO<List<ChatThemeDTO>> getThemeList(@RequestParam("botId") Long botId, @RequestParam("tenantId") Long tenantId) {
    Assert.notNull(botId, "智能应用ID不能为空");
    Assert.notNull(tenantId, "租户ID不能为空");
    return ResultVO.success(chatThemeService.getThemeList(botId, tenantId));
  }

  @Operation(summary = "主题详情")
  @GetMapping("getThemeDetail")
  public ResultVO<ChatThemeDTO> getThemeDetail(@RequestParam("botId") Long botId, @RequestParam("themeId") Long themeId,
                                               @RequestParam("tenantId") Long tenantId) {
    Assert.notNull(botId, "智能应用ID不能为空");
    Assert.notNull(themeId, "主题ID不能为空");
    Assert.notNull(tenantId, "租户ID不能为空");
    return ResultVO.success(chatThemeService.getThemeDetail(botId, themeId, tenantId));
  }

  @Operation(summary = "切换主题")
  @PostMapping("switchTheme")
  public ResultVO<Void> switchTheme(@RequestBody ChatThemeDTO chatThemeDTO) {
    Assert.notNull(chatThemeDTO.getBotId(), "智能应用ID不能为空");
    Assert.notNull(chatThemeDTO.getThemeId(), "主题ID不能为空");
    Assert.notNull(chatThemeDTO.getTenantId(), "租户ID不能为空");
    return chatThemeService.switchTheme(chatThemeDTO);
  }

  @Operation(summary = "查询聊天主题列表（分页）")
  @PostMapping("queryChatThemePage")
  public ResultVO<PageInfo<ChatThemeDTO>> queryChatThemePage(@RequestBody ChatThemeQueryParams queryParams) {
    Assert.notNull(queryParams.getTenantId(), "租户ID不能为空");
    return ResultVO.success(chatThemeService.queryChatThemePage(queryParams));
  }

  @Operation(summary = "删除聊天主题")
  @PostMapping("deleteChatTheme")
  public ResultVO<Void> deleteChatTheme(@RequestBody ChatThemeDTO chatThemeDTO) {
    Assert.notNull(chatThemeDTO.getBotId(), "智能应用ID不能为空");
    Assert.notNull(chatThemeDTO.getThemeId(), "主题ID不能为空");
    Assert.notNull(chatThemeDTO.getTenantId(), "租户ID不能为空");
    return chatThemeService.deleteChatTheme(chatThemeDTO);
  }

  @Operation(summary = "获取正在使用的主题")
  @GetMapping("getUsedTheme")
  public ResultVO<ChatThemeDTO> getUsedTheme(@RequestParam("botId") Long botId, @RequestParam("tenantId") Long tenantId) {
    Assert.notNull(botId, "智能应用ID不能为空");
    Assert.notNull(tenantId, "租户ID不能为空");
    return ResultVO.success(chatThemeService.getUsedTheme(botId, tenantId));
  }

}
