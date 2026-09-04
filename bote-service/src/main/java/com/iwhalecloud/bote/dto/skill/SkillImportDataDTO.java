package com.iwhalecloud.bote.dto.skill;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能导入中间数据
 */
@Getter
@Setter
@ToString
public class SkillImportDataDTO {
  private byte[] metaJson;
  private byte[] skillContent;
  private byte[] packageBytes;
}
