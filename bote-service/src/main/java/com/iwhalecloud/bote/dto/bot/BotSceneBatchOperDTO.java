package com.iwhalecloud.bote.dto.bot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 智能体批量操作DTO
 *
 * @author wang.tingyun
 * @since 2025-08-12
 */
@Getter
@Setter
@ToString
public class BotSceneBatchOperDTO {

  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "智能体ID列表")
  private List<Long> sceneIds;
  @Schema(description = "智能体状态：用于上下架")
  private String sceneStatus;
  @Schema(description = "智能体操作类型(publish:上下架，delete:删除，move:移动)")
  private String sceneOperType;
  @Schema(description = "智能体移动的目标目录ID")
  private Long targetCatalogItemId;
  @Schema(description = "操作人ID")
  private Long updatorId;

}
