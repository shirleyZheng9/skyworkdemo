package com.iwhalecloud.bote.dto.portal.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 权限查询参数
 *
 * @author auto
 * @since 2024-10-14
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "权限查询参数")
public class PrivQueryParams extends PagingQueryParams {
  @Schema(description = "角色类型")
  private String privType;
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "目录ID列表", hidden = true)
  private List<Long> catalogItemList;
  @Schema(description = "主键ID列表", hidden = true)
  private List<Long> privIds;
}
