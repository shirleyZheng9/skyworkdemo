package com.iwhalecloud.bote.dto.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.entity.chat.ChatThemeEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * 聊天主题 DTO
 *
 * @author tingyun.wang
 * @since 2025-07-23
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_chat_theme")
@JsonInclude(Include.NON_NULL)
public class ChatThemeDTO extends ChatThemeEntity {

  @Schema(description = "主题主色")
  private String primaryColor;

  @Schema(description = "更新人名称")
  private String updatorName;

  @Schema(description = "主题数据对象")
  private Map<String, Object> themeJsonMap;

}
