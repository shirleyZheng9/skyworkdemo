package com.iwhalecloud.bote.dto.bot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话辅助简单信息
 *
 * @author chen.linfa
 * @since 2024-08-02
 */
@Getter
@Setter
@ToString
public class SimpleBotUserExperienceDTO {
  @Schema(description = "信息内容")
  private String content;
  @Schema(description = "类型 模板：module 指令：point 常用问题：request 术语：term")
  private String type;
}
