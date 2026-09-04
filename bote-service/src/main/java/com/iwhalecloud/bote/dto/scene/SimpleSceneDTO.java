package com.iwhalecloud.bote.dto.scene;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 智能体 DTO
 *
 * @author qian.sisheng
 * @since 2026/03/16
 */
@Getter
@Setter
@ToString
public class SimpleSceneDTO {
  /** 场景ID */
  private String sceneId;
  /** 场景名称 */
  private String sceneName;
  /** 场景描述 */
  private String sceneDesc;
  /** 场景类型 */
  private String sceneType;
  /** 场景状态 */
  private String sceneStatus;
}
