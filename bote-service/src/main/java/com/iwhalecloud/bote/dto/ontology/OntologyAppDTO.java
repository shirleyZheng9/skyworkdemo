package com.iwhalecloud.bote.dto.ontology;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 本体应用 DTO
 *
 * @author qian.sisheng
 * @since 2026-04-29
 */
@Getter
@Setter
@ToString
public class OntologyAppDTO {
  /** 应用ID */
  private Long appId;
  /** 应用名称 */
  private String appName;
}
