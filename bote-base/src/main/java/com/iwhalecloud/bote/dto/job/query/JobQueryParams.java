package com.iwhalecloud.bote.dto.job.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 定时任务查询参数
 *
 * @author qian.sisheng
 * @since 2025-11-06
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "定时任务查询参数")
public class JobQueryParams extends PagingQueryParams {
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "状态")
  private String state;
  @Schema(description = "空间ID")
  private Long spaceId;
  @Schema(description = "应用ID")
  private Long botId;
  @Schema(description = "创建者ID")
  private Long creatorId;
}
