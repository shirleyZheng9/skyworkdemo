package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target;

import com.iwhalecloud.bote.loop.client.evaluation.domain.common.BaseInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bot数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Bot数据传输对象")
public class BotDTO {

  @Schema(description = "Bot ID")
  private Long botId;

  @Schema(description = "Bot版本")
  private String botVersion;

  @Schema(description = "Bot信息类型")
  private BotInfoTypeDTO botInfoType;

  @Schema(description = "模型信息")
  private ModelInfoDTO modelInfo;

  @Schema(description = "Bot名称")
  private String botName;

  @Schema(description = "头像URL")
  private String avatarUrl;

  @Schema(description = "描述")
  private String description;

  @Schema(description = "发布版本")
  private String publishVersion;

  @Schema(description = "基础信息")
  private BaseInfoDTO baseInfo;
}

