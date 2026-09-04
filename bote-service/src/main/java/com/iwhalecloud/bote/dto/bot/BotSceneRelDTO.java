package com.iwhalecloud.bote.dto.bot;

import com.iwhalecloud.bote.entity.bot.BotSceneRelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 机器人关联场景 DTO
 *
 * @author chen.linfa
 * @since 2025-04-23
 */
@Getter
@Setter
@ToString(callSuper = true)
public class BotSceneRelDTO extends BotSceneRelEntity {
  @Schema(description = "修改人名称")
  private String updatorName;
  @Schema(description = "修改人图标")
  private String updatorIcon;
  @Schema(description = "智能体名称")
  private String sceneName;
  @Schema(description = "智能体状态")
  private String sceneStatus;
  @Schema(description = "智能体类型")
  private String sceneType;
  @Schema(description = "智能体描述")
  private String sceneDesc;
  @Schema(description = "智能体图标")
  private String sceneIcon;
  @Schema(description = "智能体开场白")
  private String prologue;
}
