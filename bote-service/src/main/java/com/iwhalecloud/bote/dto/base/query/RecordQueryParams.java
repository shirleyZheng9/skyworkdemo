package com.iwhalecloud.bote.dto.base.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 发布记录查询参数
 *
 * @author auto
 * @since 2024-10-31
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "发布记录查询参数")
public class RecordQueryParams extends PagingQueryParams {
  @Schema(description = "类型")
  private String publishType;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "状态")
  private Integer publishStatus;
  @Schema(description = "模糊查询")
  private String searchContent;
}
