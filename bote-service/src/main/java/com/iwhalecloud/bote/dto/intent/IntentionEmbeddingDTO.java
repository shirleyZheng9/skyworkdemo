package com.iwhalecloud.bote.dto.intent;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 意图标注向量
 *
 * @author bianjp
 * @since 2024-12-23
 */
@Getter
@Setter
@ToString
public class IntentionEmbeddingDTO {
  /** ID */
  private Long id;
  /** 租户 ID */
  private Long tenantId;
  /** 智能体 ID */
  private Long sceneId;
  /** 意图问句 ID */
  private Long questionId;
  /** 问题 */
  private float[] question;
}
