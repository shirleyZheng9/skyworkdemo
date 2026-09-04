package com.iwhalecloud.bote.dto.bot;

import com.iwhalecloud.bote.entity.bot.ClawEnvVariableEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * claw 环境变量关联 DTO
 *
 * @author chen.linfa
 * @since 2026-04-23
 */
@Getter
@Setter
@ToString(callSuper = true)
public class ClawEnvVariableDTO extends ClawEnvVariableEntity {
  @Schema(description = "变量名称")
  private String variableName;
  @Schema(description = "变量编码")
  private String variableCode;
  @Schema(description = "变量值")
  private String variableVal;
}
