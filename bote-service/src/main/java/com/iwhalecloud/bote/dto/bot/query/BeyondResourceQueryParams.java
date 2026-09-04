package com.iwhalecloud.bote.dto.bot.query;

import com.iwhalecloud.bote.dto.knowledge.access.platform.request.PlatPagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应平台资源查询参数（智能应用与智能体混合）
 *
 * @author lizuyin
 * @since 2025-11-27
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "百应平台资源查询参数")
public class BeyondResourceQueryParams extends PlatPagingQueryParams {
  @Schema(description = "关键词（可选，用于搜索名称和描述）")
  private String keyword;
  @Schema(description = "归属类型（可选，1=当前用户发布，0=非当前用户发布）")
  private Integer ownershipType;
  @Schema(description = "资源类型列表（可选，APPLICATION=智能应用，AGENT=智能体）")
  private List<String> resourceBizTypes;
  @Schema(description = "状态列表（可选，用于过滤上下架状态）")
  private List<String> status;
  @Schema(description = "租户 ID（可选）")
  private Long tenantId;
}

