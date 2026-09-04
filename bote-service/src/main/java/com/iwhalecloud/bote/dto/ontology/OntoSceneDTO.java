package com.iwhalecloud.bote.dto.ontology;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 本体场景 DTO
 *
 * @author qian.sisheng
 * @since 2026-04-29
 */
@Getter
@Setter
@ToString
public class OntoSceneDTO {
  /** 场景 ID */
  private Long sceneId;
  /** 场景名称 */
  private String sceneName;
  /** 步骤内容 */
  private String stepContent;
}
