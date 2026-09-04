package com.iwhalecloud.bote.dto.bot.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 模板智能体查询参数
 *
 * @author auto
 * @since 2025-06-21
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "模板智能体查询参数")
public class PlatSceneInfoQueryParams extends PagingQueryParams {

  @Schema(description = "搜索关键词（场景名称）")
  private String searchContent;

  @Schema(description = "租户ID")
  private Long tenantId;

  @Schema(description = "场景分类ID")
  private Long catalogItemId;

  @Schema(description = "智能体状态")
  private String sceneStatus;

  @Schema(description = "智能体类型")
  private String sceneType;

  @Schema(description = "智能体ID")
  private Long sceneId;
}
