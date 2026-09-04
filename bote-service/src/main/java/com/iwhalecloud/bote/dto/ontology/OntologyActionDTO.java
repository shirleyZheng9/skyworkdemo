package com.iwhalecloud.bote.dto.ontology;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 本类动作的dto
 *
 * @author qian.sisheng
 * @since 2026-04-29
 */
@Getter
@Setter
@ToString
public class OntologyActionDTO {
  /** 动作编码 */
  private String actionCode;
  /** 动作名称 */
  private String actionName;
  /** 动作ID */
  private Long actionId;
}
