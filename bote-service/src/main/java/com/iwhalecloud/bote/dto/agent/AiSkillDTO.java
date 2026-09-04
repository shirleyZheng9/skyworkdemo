package com.iwhalecloud.bote.dto.agent;

import com.iwhalecloud.bote.entity.agent.AiSkillEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 新增表记录启用技能 DTO
 *
 * @author linmengfan
 * @since 2026-03-05
 */
@Getter
@Setter
@ToString(callSuper = true)
public class AiSkillDTO extends AiSkillEntity {
  @Schema(description = "智能应用名称")
  private String botName;
  @Schema(description = "智能应用用途")
  private String botUse;
  @Schema(description = "智能应用id列表")
  private List<Long> botIdList;
  @Schema(description = "技能名称")
  private String skillName;
  @Schema(description = "技能描述")
  private String skillDesc;
  @Schema(description = "是否为创建的技能")
  private Boolean isCreated;
}
