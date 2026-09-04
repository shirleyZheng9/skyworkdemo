package com.iwhalecloud.bote.dto.chat;

import com.iwhalecloud.bote.entity.chat.ChatBotCfgEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 对话应用配置 DTO
 *
 * @author chen.linfa
 * @since 2025-09-09
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_chat_bot_cfg")
@Schema(description = "对话应用配置")
public class ChatBotCfgDTO extends ChatBotCfgEntity {

}
