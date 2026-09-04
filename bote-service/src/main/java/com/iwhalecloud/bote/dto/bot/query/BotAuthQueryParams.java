package com.iwhalecloud.bote.dto.bot.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 机器人授权查询参数
 *
 * @author auto
 * @since 2025-03-04
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "机器人授权查询参数")
public class BotAuthQueryParams extends PagingQueryParams {
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "用户ID")
  private Long userId;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "机器人ID")
  private Long botId;
  @Schema(description = "租户ID列表")
  private List<Long> tenantIds;
  @Schema(description = "用户ID列表")
  private List<Long> userIds;
}
