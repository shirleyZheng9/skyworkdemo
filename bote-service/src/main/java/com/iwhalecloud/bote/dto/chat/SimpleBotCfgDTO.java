package com.iwhalecloud.bote.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话应用配置
 *
 * @author chen.linfa
 * @since 2025-09-11
 */
@Getter
@Setter
@ToString
@Schema(description = "会话应用配置")
public class SimpleBotCfgDTO {
  @Schema(description = "是否置顶")
  private Boolean top;

  @Schema(description = "是否标记")
  private Boolean mark;

  @Schema(description = "是否完成")
  private Boolean close;

  public SimpleBotCfgDTO(Boolean top, Boolean mark, Boolean close) {
    this.top = top;
    this.mark = mark;
    this.close = close;
  }
}
