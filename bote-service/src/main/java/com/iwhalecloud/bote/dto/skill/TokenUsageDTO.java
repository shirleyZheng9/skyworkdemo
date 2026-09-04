package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 大模型 token 使用量
 *
 * @author bianjp
 * @since 2025-11-10
 */
@Getter
@Setter
@ToString
@Schema(description = "大模型 token 使用量")
public class TokenUsageDTO {
  @Schema(description = "总量")
  private Integer total;
  @Schema(description = "输入 token 量")
  private Integer prompt;
  @Schema(description = "输出 token 量")
  private Integer completion;
  
  @Schema(description = "各模型的 token 使用量")
  private Map<String, ModelTokenUsage> modelTokenUsages;

  @Getter
  @Setter
  @ToString
  public static class ModelTokenUsage {
    @Schema(description = "模型总量")
    private Integer total;
    @Schema(description = "模型输入 token 量")
    private Integer prompt;
    @Schema(description = "模型输出 token 量")
    private Integer completion;
  }
}