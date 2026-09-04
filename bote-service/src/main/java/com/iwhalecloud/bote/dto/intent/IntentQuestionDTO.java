package com.iwhalecloud.bote.dto.intent;

import com.iwhalecloud.bote.entity.intent.IntentQuestionEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 意图问句 DTO
 *
 * @author auto
 * @since 2024-12-18
 */
@Getter
@Setter
@ToString(callSuper = true)
public class IntentQuestionDTO extends IntentQuestionEntity {
  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "场景名称")
  private String sceneName;
}
