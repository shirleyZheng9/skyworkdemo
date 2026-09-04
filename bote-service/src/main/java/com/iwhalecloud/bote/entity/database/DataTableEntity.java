package com.iwhalecloud.bote.entity.database;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 业务数据表 entity
 *
 * @author wangtingyun
 * @since 2025-11-18
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_data_table")
public class DataTableEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long tableId;

  @DiffField(name = "TABLE_NAME")
  @Schema(description = "表名称")
  private String tableName;

  @DiffField(name = "TABLE_CODE")
  @Schema(description = "表编码")
  private String tableCode;

  @DiffField(name = "TABLE_DESC")
  @Schema(description = "表描述")
  private String tableDesc;

  @DiffField(name = "TABLE_ICON")
  @Schema(description = "表图标")
  private String tableIcon;

  @DiffField(name = "DATA_SOURCE_ID")
  @Schema(description = "归属数据源Id")
  private Long dataSourceId;

  @DiffField(name = "DATA_SOURCE_CHANNEL")
  @Schema(description = "数据库渠道类型（custom: 自定义数据库，platform: 平台数据库）")
  private String dataSourceChannel;

  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "所属分组ID")
  private Long catalogItemId;

  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;

}
