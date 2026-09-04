package com.iwhalecloud.bote.dto.intent.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 意图标注查询参数
 *
 * @author auto
 * @since 2024-12-18
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "意图标注查询参数")
public class IntentQueryParams extends PagingQueryParams {
  @Schema(description = "智能体ID")
  private Long sceneId;
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "智能体ID集合")
  private List<Long> sceneIds;
}

