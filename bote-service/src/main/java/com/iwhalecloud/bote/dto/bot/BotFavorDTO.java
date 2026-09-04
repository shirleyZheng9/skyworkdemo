package com.iwhalecloud.bote.dto.bot;

import com.iwhalecloud.bote.entity.bot.BotFavorEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 机器人收藏
 *
 * @author auto
 * @since 2024-09-14
 */
@Getter
@Setter
@ToString(callSuper = true)
public class BotFavorDTO extends BotFavorEntity {
  @Schema(description = "机器人名称")
  private String botName;

  @Schema(description = "机器人用途")
  private String botUse;

  @Schema(description = "机器人图标")
  private String botIcon;
}
