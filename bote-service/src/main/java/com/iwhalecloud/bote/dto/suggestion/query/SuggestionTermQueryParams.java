package com.iwhalecloud.bote.dto.suggestion.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 联想术语查询参数
 *
 * @author lizuyin
 * @since 2025-06-09
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "联想术语查询参数")
public class SuggestionTermQueryParams extends PagingQueryParams {
  @Schema(description = "智能体ID")
  private Long botId;

  @Schema(description = "智能应用ID")
  private Long sceneId;

  @Schema(description = "租户ID")
  private Long tenantId;

  @Schema(description = "联想话术类型")
  private String termType;

  @Schema(description = "归属者类型")
  private String ownerType;

  @Schema(description = "模糊查询")
  private String searchContent;
}
