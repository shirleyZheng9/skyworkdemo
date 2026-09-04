package com.iwhalecloud.bote.dto.bot;

import com.iwhalecloud.bote.entity.bot.CopilotPointEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 副驾指令 DTO
 *
 * @author auto
 * @since 2024-09-19
 */
@Getter
@Setter
@ToString(callSuper = true)
public class CopilotPointDTO extends CopilotPointEntity {
  @Schema(description = "场景名称")
  private String sceneName;
  @Schema(description = "场景状态")
  private String sceneStatus;
  @Schema(description = "智能应用名称")
  private String botName;
}
