package com.iwhalecloud.bote.controller.chat;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.chat.ChatBotCfgDTO;
import com.iwhalecloud.bote.service.chat.IChatBotCfgService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对话应用配置管理 controller
 *
 * @author chen.linfa
 * @since 2025-09-09
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/chatBot", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "聊天：对话应用配置")
public class ChatBotCfgController {

  private final IChatBotCfgService chatBotCfgService;

  @Operation(summary = "新增配置")
  @PostMapping("addCfg")
  public ResultVO<Void> addCfg(@RequestBody ChatBotCfgDTO cfg) {
    return chatBotCfgService.add(cfg);
  }

  @Operation(summary = "取消配置")
  @PostMapping("deleteCfg")
  public ResultVO<Void> deleteCfg(@RequestBody ChatBotCfgDTO cfg) {
    return chatBotCfgService.delete(cfg);
  }
}
