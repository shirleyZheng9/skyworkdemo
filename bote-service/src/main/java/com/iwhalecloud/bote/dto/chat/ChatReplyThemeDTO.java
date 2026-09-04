package com.iwhalecloud.bote.dto.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.entity.chat.ChatReplyThemeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 回复消息主题 DTO
 *
 * @author qian.sisheng
 * @since 2025-12-02
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class ChatReplyThemeDTO extends ChatReplyThemeEntity {
  @Schema(description = "创建人名称")
  private String updatorName;
}
