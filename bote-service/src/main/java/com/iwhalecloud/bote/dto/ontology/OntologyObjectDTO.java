package com.iwhalecloud.bote.dto.ontology;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 本类对象的dto
 *
 * @author qian.sisheng
 * @since 2026-04-29
 */
@Getter
@Setter
@ToString
public class OntologyObjectDTO {
  /** 对象编码 */
  private String objTypeCode;
  /** 对象名称 */
  private String objTypeName;
  /** 对象ID */
  private Long objTypeId;
}
