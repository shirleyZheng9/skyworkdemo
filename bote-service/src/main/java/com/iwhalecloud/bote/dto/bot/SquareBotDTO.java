package com.iwhalecloud.bote.dto.bot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 技能广场里面查询通用智能体list
 *
 * @author huang.yunming
 * @since 2026-03-19
 */
@Getter
@Setter
@Schema(description = "技能广场里面查询通用智能体list")
@JsonInclude(Include.NON_NULL)
public class SquareBotDTO {
  @Schema(description = "机器人ID")
  private Long botId;
  @Schema(description = "机器人名称")
  private String botName;
  @Schema(description = "机器人用途")
  private String botUse;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "机器人状态: 0：未上架  1：已上架 2：已下架")
  private String botStatus;
  @Schema(description = "创建时间")
  private Date createdTime;
  @Schema(description = "数据来源（10A：运行态创建的智能体）")
  private String dataFrom;
  @Schema(description = "技能是否已经安装")
  private Boolean skillInstalled;
  @Schema(description = "已安装技能版本是否落后于广场当前版本（未传 skillId、未安装或广场无记录时为 null）")
  private Boolean skillVersionHasUpdate;
}
