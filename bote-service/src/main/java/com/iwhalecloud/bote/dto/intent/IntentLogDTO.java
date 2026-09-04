package com.iwhalecloud.bote.dto.intent;

import com.iwhalecloud.bote.entity.intent.IntentLogEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 意图识别日志 DTO
 *
 * @author auto
 * @since 2024-12-18
 */
@Getter
@Setter
@ToString(callSuper = true)
public class IntentLogDTO extends IntentLogEntity {
  @Schema(description = "扩展参数")
  private String attribute;
  @Schema(description = "场景名称")
  private String sceneName;
  @Schema(description = "机器人名称")
  private String botName;
}
