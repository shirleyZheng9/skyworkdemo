package com.iwhalecloud.bote.entity.intent;

import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 意图识别策略 Entity
 *
 * @author auto
 * @since 2025-02-17
 */
@Getter
@Setter
@ToString(callSuper = true)
public class IntentStrategyEntity extends BaseEntity {
  @Schema(description = "主键")
  private Long id;
  @Schema(description = "租户 ID")
  private Long tenantId;

  @Schema(description = "开启多机器人匹配策略")
  private String multiBotEnabled;

  @Schema(description = "开启向量化策略")
  private String embeddingEnabled;
  @Schema(description = "Embedding 模型 ID")
  private Long embeddingModelId;
  @Schema(description = "分数阈值")
  private Float embeddingScoreThreshold;

  @Schema(description = "开启小模型推理策略")
  private String slmEnabled;
}
