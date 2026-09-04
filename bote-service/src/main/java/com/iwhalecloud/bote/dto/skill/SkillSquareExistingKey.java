package com.iwhalecloud.bote.dto.skill;

import lombok.Data;

/**
 * 批量导入前预加载的已存在技能主键（减少逐条 selectByCode）
 */
@Data
public class SkillSquareExistingKey {
  private String skillCode;
  private Long skillId;
  private Long fileInfoId;
}
