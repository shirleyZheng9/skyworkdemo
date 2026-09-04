package com.iwhalecloud.bote.dto.bot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 智能应用与智能体关联
 *
 * @author qian.sisheng
 * @since 2025-06-12
 */
@Getter
@Setter
@ToString
public class SimpleBotSceneRelDTO {
  @Schema(description = "主键")
  private Long relId;
  @Schema(description = "智能体 ID")
  private Long sceneId;
  @Schema(description = "智能体名称")
  private String sceneName;
  @Schema(description = "智能体类型")
  private String sceneType;
  @Schema(description = "应用 ID")
  private Long botId;
  @Schema(description = "是否默认智能体")
  private String isDefault;
}
