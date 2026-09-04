package com.iwhalecloud.bote.controller.chat;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.chat.ChatReplyThemeDTO;
import com.iwhalecloud.bote.dto.chat.query.ChatReplyThemeQueryParams;
import com.iwhalecloud.bote.service.chat.IChatReplyThemeManageService;
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
import org.springframework.web.multipart.MultipartFile;

/**
 * 回复消息主题管理 controller
 *
 * @author qian.sisheng
 * @since 2025-12-02
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/chatReplyTheme", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "回复消息主题管理")
public class ChatReplyThemeManageController {

  private final IChatReplyThemeManageService chatReplyThemeManageService;

  @Operation(summary = "查询单个回复消息主题")
  @GetMapping("findChatReplyTheme")
  public ResultVO<ChatReplyThemeDTO> findChatReplyTheme(@RequestParam(name = "replyThemeId") Long replyThemeId, @RequestParam(name = "tenantId") Long tenantId) {
    Assert.notNull(replyThemeId, "主键 ID 不能为空");
    return ResultVO.success(chatReplyThemeManageService.findChatReplyTheme(replyThemeId, tenantId));
  }

  @Operation(summary = "保存回复消息主题")
  @PostMapping("saveChatReplyTheme")
  public ResultVO<ChatReplyThemeDTO> saveChatReplyTheme(@RequestBody ChatReplyThemeDTO chatReplyTheme) {
    return chatReplyThemeManageService.saveChatReplyTheme(chatReplyTheme);
  }

  @Operation(summary = "删除回复消息主题")
  @GetMapping("deleteChatReplyTheme")
  public ResultVO<Void> deleteChatReplyTheme(@RequestParam(name = "replyThemeId") Long replyThemeId, @RequestParam(name = "tenantId") Long tenantId) {
    Assert.notNull(replyThemeId, "主键 ID 不能为空");
    return chatReplyThemeManageService.deleteChatReplyTheme(replyThemeId, tenantId);
  }

  @Operation(summary = "查询回复消息主题列表")
  @PostMapping("queryChatReplyThemeList")
  public ResultVO<List<ChatReplyThemeDTO>> queryChatReplyThemeList(@RequestBody ChatReplyThemeQueryParams queryParams) {
    return ResultVO.success(chatReplyThemeManageService.queryChatReplyThemeList(queryParams));
  }

  @Operation(summary = "分页查询回复消息主题")
  @PostMapping("queryChatReplyThemePage")
  public ResultVO<PageInfo<ChatReplyThemeDTO>> queryChatReplyThemePage(@RequestBody ChatReplyThemeQueryParams queryParams) {
    return ResultVO.success(chatReplyThemeManageService.queryChatReplyThemePage(queryParams));
  }

  @Operation(summary = "导入回复消息主题")
  @PostMapping("importReplyTheme")
  public ResultVO<Void> importReplyTheme(@RequestParam("file") MultipartFile file, @RequestParam("tenantId") Long tenantId,
    @RequestParam("replyThemeName") String replyThemeName) {
    return chatReplyThemeManageService.importReplyTheme(file, tenantId, replyThemeName);
  }
}
