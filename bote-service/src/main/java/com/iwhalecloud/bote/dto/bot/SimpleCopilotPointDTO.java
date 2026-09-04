package com.iwhalecloud.bote.dto.bot;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单副驾指令 DTO
 *
 * @author auto
 * @since 2024-09-19
 */
@Getter
@Setter
@ToString
public class SimpleCopilotPointDTO {
  @Schema(description = "机器人 ID")
  private Long botId;
  @Schema(description = "场景 ID")
  private Long sceneId;
  @Schema(description = "场景名称")
  private String sceneName;
  @Schema(description = "指令编码")
  private String pointCode;
  @Schema(description = "业务参数")
  private Map<String, Object> params;
  @Schema(description = "会话ID")
  private Long sessionId;
}
