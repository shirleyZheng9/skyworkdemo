package com.iwhalecloud.bote.dto.bot;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 租户的推荐 BOT 定义
 *
 * @author chen.linfa
 * @since 2025-04-17
 */
@Getter
@Setter
@ToString
public class SimpleRecommendBotDTO {
  private Boolean display;

  private List<BotInfo> list;

  /**
   * BOT 信息
   */
  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BotInfo {
    /** 机器人 ID */
    private String botId;

    /** 颜色 */
    private String className;
  }
}
