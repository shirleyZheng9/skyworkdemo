package com.iwhalecloud.bote.dto.ontology;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 本体规则 dto
 *
 * @author qian.sisheng
 * @since 2026-04-29
 */
@Getter
@Setter
@ToString
public class OntologyRuleDTO {
  /** 规则编码 */
  private String ruleCode;
  /** 规则名称 */
  private String ruleName;
  /** 规则ID */
  private String ruleId;
}
