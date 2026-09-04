package com.iwhalecloud.bote.dto.agent;

import com.iwhalecloud.bote.entity.agent.AiWorkspaceEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户级的提示词 DTO
 *
 * @author linmengfan
 * @since 2026-03-05
 */
@Getter
@Setter
@ToString(callSuper = true)
public class AiWorkspaceDTO extends AiWorkspaceEntity {
  @Schema(description = "描述")
  private String desc;
  @Schema(description = "文件中文名")
  private String fileChineseName;
}
