package com.iwhalecloud.bote.dto.bot.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户辅助信息查询参数
 *
 * @author auto
 * @since 2024-09-14
 */
@Getter
@Setter
@ToString(callSuper = true)
public class BotUserExperienceQryParams extends PagingQueryParams {
  @Schema(description = "机器人ID")
  private Long botId;
  @Schema(description = "类型")
  private String type;
  @Schema(description = "信息内容")
  private String content;
  @Schema(description = "场景ID")
  private Long sceneId;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "用户辅助信息ID")
  private Long experienceId;
}
