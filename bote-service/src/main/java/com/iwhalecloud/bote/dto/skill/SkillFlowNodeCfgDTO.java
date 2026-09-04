package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bote.entity.skill.SkillFlowNodeCfgEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能流程节点配置 DTO
 *
 * @author lizuyin
 * @since 2025-12-26
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SkillFlowNodeCfgDTO extends SkillFlowNodeCfgEntity {
  @Schema(description = "节点 key（对应数据库的 code 字段）")
  private String key;
}

