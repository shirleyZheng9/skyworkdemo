package com.iwhalecloud.bote.dto.bot.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 指令查询参数
 *
 * @author chen.linfa
 * @since 2025-01-20
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "指令查询参数")
public class PointQueryParams extends PagingQueryParams {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "名称/编码模糊查询")
  private String searchContent;
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "目录ID列表", hidden = true)
  private List<Long> catalogItemList;
  @Schema(description = "场景ID")
  private Long sceneId;
}
