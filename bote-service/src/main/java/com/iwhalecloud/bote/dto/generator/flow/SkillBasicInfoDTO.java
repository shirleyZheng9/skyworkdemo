package com.iwhalecloud.bote.dto.generator.flow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能基本信息
 *
 * @author bianjp
 * @since 2025-04-15
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
@Schema(description = "技能基本信息")
public class SkillBasicInfoDTO {
  @Schema(description = "技能 ID")
  private Long skillId;
  @Schema(description = "技能名称")
  private String skillName;
  @Schema(description = "技能编码")
  private String skillCode;
}
