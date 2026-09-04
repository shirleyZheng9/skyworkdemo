package com.iwhalecloud.bote.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户级的环境变量 DTO
 *
 * @author linmengfan
 * @since 2026-03-05
 */
@Getter
@Setter
@ToString(callSuper = true)
public class AddAiEnvVariableDTO {
  @Schema(description = "环境变量列表")
  private List<AiEnvVariableDTO> btAiEnvVariableDTOList;

  @Schema(description = "删除的环境变量列表")
  private List<Long> delIds;

  @Schema(description = "空间ID")
  private Long spaceId;

  @Schema(description = "应用ID")
  private Long botId;
}
