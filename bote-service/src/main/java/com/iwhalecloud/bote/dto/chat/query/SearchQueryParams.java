package com.iwhalecloud.bote.dto.chat.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 通用搜索查询参数
 *
 * @author chen.linfa
 * @since 2025-10-14
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "通用搜索查询参数")
public class SearchQueryParams extends PagingQueryParams {

  @Schema(description = "租户 ID")
  private Long tenantId;

  @Schema(description = "企业空间 ID")
  private Long spaceId;

  @Schema(description = "类型")
  private String type;

  @Schema(description = "模糊查询")
  private String searchContent;

  @Schema(description = "用户 ID")
  private Long userId;

  @Schema(description = "授权组织 ID 列表")
  private List<Long> orgIds;
}
