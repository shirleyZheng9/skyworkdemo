package com.iwhalecloud.bote.dto.agent;

import com.iwhalecloud.bote.entity.agent.AiModelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 新增表记录启用模型 DTO
 *
 * @author linmengfan
 * @since 2026-03-05
 */
@Getter
@Setter
@ToString(callSuper = true)
public class AiModelDTO extends AiModelEntity {
  @Schema(description = "模型名称")
  private String modelName;
  @Schema(description = "模型图标")
  private String modelIcon;
}
