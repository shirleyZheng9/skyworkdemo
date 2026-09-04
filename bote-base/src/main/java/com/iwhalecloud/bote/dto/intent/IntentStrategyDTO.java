package com.iwhalecloud.bote.dto.intent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.dto.model.SimpleIntentFinetuneDTO;
import com.iwhalecloud.bote.entity.intent.IntentStrategyEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 意图识别策略 DTO
 *
 * @author auto
 * @since 2025-02-17
 */
@Getter
@Setter
@ToString(callSuper = true)
public class IntentStrategyDTO extends IntentStrategyEntity {

  @Schema(description = "模型推理 API")
  private String slmChatApi;

  @Schema(description = "启用的小模型")
  private SimpleIntentFinetuneDTO finetune;

  @Schema(description = "大模型提示词")
  private String prompt;

  /**
   * 是否开启多机器人匹配策略
   */
  @JsonIgnore
  public boolean isMultiBot() {
    return CommonConsts.TRUE.equals(getMultiBotEnabled());
  }

  /**
   * 是否开启向量化策略
   */
  @JsonIgnore
  public boolean isEmbedding() {
    return CommonConsts.TRUE.equals(getEmbeddingEnabled());
  }

  /**
   * 是否开启小模型推理策略
   */
  @JsonIgnore
  public boolean isSlm() {
    return CommonConsts.TRUE.equals(getSlmEnabled());
  }
}
