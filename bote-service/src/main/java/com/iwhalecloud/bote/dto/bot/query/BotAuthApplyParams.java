package com.iwhalecloud.bote.dto.bot.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 智能应用授权申请请求参数
 *
 * @author wang.tingyun
 * @since 2025-08-28
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "智能应用授权申请请求参数")
public class BotAuthApplyParams extends PagingQueryParams {

  @Schema(description = "申请ID")
  private Long applyId;
  @Schema(description = "审核内容")
  private String auditContent;
  @Schema(description = "审核状态（0:待审核, 1:已通过, 2:未通过）")
  private Integer auditStatus;
  @Schema(description = "审核人ID")
  private Long auditUserId;
  @Schema(description = "申请列表搜索内容")
  private String searchContent;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "创建人ID")
  private Long creatorId;

}
