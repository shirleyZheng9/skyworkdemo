package com.iwhalecloud.bote.dto.bot;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 逻辑视图属性 DTO
 *
 * @author chen.linfa
 * @since 2024-09-25
 */
@Getter
@Setter
@ToString
public class LogicViewAttrDTO {
  /** 主键 */
  private String key;
  /** 技能 ID */
  private String skillId;
  /** 技能名称 */
  private String skillName;
  /** 技能编码 */
  private String skillCode;
  /** 技能类型 */
  private String skillType;
  /** 文本信息 */
  private String content;
}
