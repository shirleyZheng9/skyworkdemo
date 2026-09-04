package com.iwhalecloud.bote.dto.bot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 推荐场景信息
 *
 * <p>用于两处: 1. 聊天窗口的场景切换功能; 2. 意图识别不到场景时返回推荐场景</p>
 *
 * @author bianjp
 * @since 2025-04-08
 */
@Getter
@Setter
@ToString
public class RecommendedSceneDTO {
  @Schema(description = "应用 ID")
  private Long botId;
  @Schema(description = "智能体 ID")
  private Long sceneId;
  @Schema(description = "智能体名称")
  private String sceneName;
  @Schema(description = "智能体开场白")
  private String prologue;
}
