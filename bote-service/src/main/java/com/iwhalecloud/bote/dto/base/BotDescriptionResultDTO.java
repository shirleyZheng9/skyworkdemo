package com.iwhalecloud.bote.dto.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 智能体描述结果 DTO
 *
 * @author auto
 * @since 2026-01-09
 */
@Getter
@Setter
@ToString
@Schema(description = "智能体描述结果")
public class BotDescriptionResultDTO {
  @JsonProperty("coreCapabilities")
  @Schema(description = "核心能力")
  private String coreCapabilities;

  @JsonProperty("capabilityBoundary")
  @Schema(description = "能力边界")
  private String capabilityBoundary;

  @JsonProperty("exampleQuestions")
  @Schema(description = "示例问法")
  private String exampleQuestions;
}

