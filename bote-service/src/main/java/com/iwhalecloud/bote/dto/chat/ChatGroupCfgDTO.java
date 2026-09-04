package com.iwhalecloud.bote.dto.chat;

import com.iwhalecloud.bote.entity.chat.ChatGroupCfgEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 对话分组定义 DTO
 *
 * @author chen.linfa
 * @since 2025-09-08
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_chat_group_cfg")
@Schema(description = "对话分组定义")
public class ChatGroupCfgDTO extends ChatGroupCfgEntity {

  @Schema(description = "包括展示（show）、不展示（hide）数据")
  private Map<String, List<String>> setting;
}
