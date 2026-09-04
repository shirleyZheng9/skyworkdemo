package com.iwhalecloud.bote.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 修改会话标题
 *
 * @author Admin
 */
@Getter
@Setter
@ToString
@Schema(description = "修改会话标题")
public class SessionTitleUpdateDTO {
  @Schema(description = "会话id")
  private Long sessionId;
  @Schema(description = "会话标题")
  private String sessionTitle;
}
