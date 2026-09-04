package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建评估目标参数
 * 对应Go: CreateEvalTargetParam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEvalTargetParam {

  /**
   * 源目标ID
   * 对应Go: SourceTargetID *string
   */
  private String sourceTargetId;

  /**
   * 源目标版本
   * 对应Go: SourceTargetVersion *string
   */
  private String sourceTargetVersion;

  /**
   * 评估目标类型
   * 对应Go: EvalTargetType *EvalTargetType
   */
  private EvalTargetType evalTargetType;

  /**
   * Bot信息类型
   * 对应Go: BotInfoType *CozeBotInfoType
   */
  private BotInfoType botInfoType;

  /**
   * Bot发布版本
   * 对应Go: BotPublishVersion *string
   */
  private String botPublishVersion;
}
