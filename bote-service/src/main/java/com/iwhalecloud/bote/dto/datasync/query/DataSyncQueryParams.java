package com.iwhalecloud.bote.dto.datasync.query;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 数据同步数据查询参数
 *
 * @author chen.linfa
 * @since 2025-04-11
 */
@Getter
@Setter
@ToString
public class DataSyncQueryParams {
  @Schema(description = "租户 ID")
  private Long tenantId;

  @Schema(description = "起始日期")
  private Date startDate;

  @Schema(description = "节点编码")
  private String code;

  @Schema(description = "节点下选中的数据")
  private List<Long> values;
}
