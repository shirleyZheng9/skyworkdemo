package com.iwhalecloud.bote.controller.chat;

import com.iwhalecloud.bote.common.annotation.RequestCacheable;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.chat.ChatGroupCfgDTO;
import com.iwhalecloud.bote.service.chat.IChatGroupCfgService;
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
 * 对话分组管理 controller
 *
 * @author chen.linfa
 * @since 2025-09-08
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/chatGroup", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "聊天：对话分组")
public class ChatGroupCfgController {

  private final IChatGroupCfgService chatGroupCfgService;

  @Operation(summary = "查询分组定义")
  @GetMapping("getGroupCfg")
  public ResultVO<ChatGroupCfgDTO> getGroupCfg(@RequestParam("spaceId") Long spaceId) {
    Assert.notNull(spaceId, "空间 ID 不能为空");
    return ResultVO.success(chatGroupCfgService.getGroupCfg(spaceId, SessionUtil.getLoginInfo().getUserId()));
  }

  @Operation(summary = "保存分组定义")
  @PostMapping("saveGroupCfg")
  public ResultVO<ChatGroupCfgDTO> saveGroupCfg(@RequestBody ChatGroupCfgDTO group) {
    Assert.notNull(group.getSpaceId(), "空间 ID 不能为空");
    return chatGroupCfgService.saveGroupCfg(group);
  }

  @Operation(summary = "查询对话分组")
  @GetMapping("getChatGroup")
  @RequestCacheable(sql = "SELECT updated_time FROM bt_chat_group_cfg WHERE space_id = #{param1} AND user_id = #{param2}", cacheOnNotFound = true)
  public ResultVO<List<String>> getChatGroup(@RequestParam("spaceId") Long spaceId, @RequestParam("userId") Long userId) {
    Assert.notNull(spaceId, "空间 ID 不能为空");
    Assert.notNull(userId, "用户 ID 不能为空");
     ChatGroupCfgDTO group = chatGroupCfgService.getGroupCfg(spaceId, userId);
    return ResultVO.success(group.getSetting().get("show"));
  }
}
