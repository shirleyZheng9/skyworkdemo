package com.iwhalecloud.bote.dto.database.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * 业务数据表查询参数
 *
 * @author tingyun.wang
 * @since 2025-11-18
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "业务数据表查询参数")
public class DataTableQueryParams extends PagingQueryParams {

  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "表ID")
  private Long tableId;
  @Schema(description = "表名称（模糊搜索）")
  private String tableName;
  @Schema(description = "所属分组ID")
  private Long catalogItemId;
  @Schema(description = "数据库渠道类型（custom: 自定义数据库，platform: 平台数据库）")
  private String dataSourceChannel;
  @Schema(description = "归属数据源Id")
  private Long dataSourceId;

  @Schema(description = "查询字段")
  Map<String, Object> queryColumns;

}
