package com.iwhalecloud.bote.dto.intent;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 意图匹配项 + 关联的场景信息
 *
 * @author bianjp
 * @since 2024-12-24
 */
@Getter
@Setter
@ToString
public class IntentionMatchItemWithSceneDTO {
  /** 意图问句 ID */
  private Long questionId;
  /** 场景 ID */
  private Long sceneId;
  /** 场景名称 */
  private String sceneName;
  /** 问题内容 */
  private String question;
  /** 问题扩展参数 */
  private String attribute;
  /** 评分(范围为 0~1, 值越大表示匹配度越高) */
  private Float score;
}
