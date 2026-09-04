package com.iwhalecloud.bote.dto.app.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;


/**
 * AI助理查询参数
 *
 * @author wangtingyun
 * @since 2024-10-22
 */
@Getter
@Setter
@ToString(callSuper = true)
public class AiBotQueryParams extends PagingQueryParams {

  @Schema(description = "名称/编码模糊查询")
  private String searchContent;
  @Schema(description = "企业空间ID")
  private Long spaceId;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "是否过滤已关联的AI助手")
  private boolean filterRelated;
  @Schema(description = "当前自己关联的AI助手")
  private List<Long> selectedAiAppIds;

}
