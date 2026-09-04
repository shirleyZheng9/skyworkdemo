package com.iwhalecloud.bote.dto.bot;

import lombok.Getter;
import lombok.Setter;

/**
 * 广场技能在多个 bot 上的安装版本（批量查询用）
 */
@Getter
@Setter
public class SquareBotSkillRowDTO {
  private Long botId;
  private String skillVersion;
}
