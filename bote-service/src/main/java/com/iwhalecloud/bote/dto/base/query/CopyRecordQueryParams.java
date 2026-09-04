package com.iwhalecloud.bote.dto.base.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 复制记录查询参数
 *
 * @author chen.linfa
 * @since 2025-08-18
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "复制记录查询参数")
public class CopyRecordQueryParams extends PagingQueryParams {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "用户 ID")
  private Long userId;
}
