package com.iwhalecloud.bote.dto.scene;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单场景提示词
 *
 * @author bianjp
 * @since 2024-08-05
 */
@Getter
@Setter
@ToString
public class SimpleScenePromptDTO {
  /** 提示词 ID */
  private Long promptId;
  /** 提示词类型 */
  private String promptType;
  /** 提示词内容 */
  private String scenePrompt;
}
