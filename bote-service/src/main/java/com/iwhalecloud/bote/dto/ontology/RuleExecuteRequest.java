package com.iwhalecloud.bote.dto.ontology;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 本体平台规则执行入参
 *
 * @author chen.linfa
 * @since 2026-04-28
 */
@Getter
@Setter
@ToString
public class RuleExecuteRequest {
  /** 应用 ID */
  private String appId;
  /** 各个规则入参 */
  private java.util.List<Map<String, Object>> rules;
}
